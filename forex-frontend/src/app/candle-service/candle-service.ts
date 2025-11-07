import { Injectable, signal, WritableSignal } from "@angular/core";
import { HttpClient, HttpEventType, HttpHeaders } from "@angular/common/http";
import { Candle } from "../downloader/response/candle";
import { CandlesDownloadRequest } from "../downloader/request/candles-download-request";
import { catchError, of } from "rxjs";

@Injectable({
    providedIn: "root",
})
/**
 * Service for sending candle download requests to the backend server.  Makes use of
 * Angular's HttpClient.
 */
export class CandleService {
    // Endpoint
    private backendUrl = "http://localhost:9000/downloadCandles";

    // Signals for candles and errors (used in the data downloader component)
    public candlesSignal: WritableSignal<Candle[]> = signal([]);
    public errorMessageSignal: WritableSignal<string | null> = signal(null);

    /**
     * Constructor.  Only needed to receive an HttpClient injection.
     *
     * @param http Angular's HttpClient for making requests.
     */
    constructor(private http: HttpClient) {}

    /**
     * Main method of the service.  This method takes a download request, sends it to the
     * server, and updates the candle and error message signals based on what the server
     * sends back.
     *
     * @param candlesDownloadRequest Request for downloading historical forex candle data.
     */
    public downloadCandles(candlesDownloadRequest: CandlesDownloadRequest): void {
        // Headers
        const headers = new HttpHeaders({
            "Content-Type": "application/json",
            Accept: "application/json",
        });

        // Send a post request (post is needed to send the JSONified request in the body)
        this.http
            .post(this.backendUrl, JSON.stringify(candlesDownloadRequest), {
                headers: headers,
                responseType: "text",
                observe: "events",
            })
            // Handle any errors
            .pipe(
                catchError((error) => {
                    console.error(error);
                    const serverError = error.error || null;
                    const serverErrorJson = (() => {
                        try {
                            return JSON.parse(serverError);
                        } catch {
                            return null;
                        }
                    })();
                    const code = serverErrorJson?.code ?? "UNKNOWN_ERROR";
                    const message = serverErrorJson?.message ?? "(message not provided)";
                    const status = error.status || 520;
                    // If there was an error, we should update the error and candle signals to
                    // indicate what happened
                    this.errorMessageSignal.set(
                        `status = ${status}; code = ${code}; message = ${message}`
                    );
                    this.candlesSignal.set([]);
                    return of([]);
                })
            )
            // Process the results
            .subscribe((response) => {
                // The only time the response should be an array is if an error was caught and of([])
                // was returned.  The server also sends a stream of JSON, so we know the server is done
                // when response is an HttpEventType.Response.
                if (!Array.isArray(response) && response.type === HttpEventType.Response) {
                    const candlesBody = response.body!;
                    // The server sends batches of JSON arrays, so use regex to parse them.
                    const candleArrays = candlesBody.match(/\[.*?\]/g);
                    const candles = candleArrays
                        ?.map((batch) => this.parseCandles(JSON.parse(batch)))
                        .reduce((acc, currBatch) => acc.concat(currBatch));

                    if (candles) {
                        this.candlesSignal.set(candles);

                        if (candles.length > 0) {
                            this.errorMessageSignal.set(null);
                        }
                    } else {
                        console.error(`Unknown data type in response body: ${candlesBody}`);
                        this.errorMessageSignal.set(
                            `Unknown body data type (expected an array of candles): ${candlesBody}`
                        );
                        this.candlesSignal.set([]);
                    }
                }
            });
    }

    /**
     * Helper method for parsing JSONified candles sent by the server.
     *
     * @param data JSON to parse as Candle objects.
     *
     * @returns An array of Candle objects.
     */
    private parseCandles(data: any[]): Candle[] {
        return data.map((item) => {
            return Candle.fromJSON(item);
        });
    }
}
