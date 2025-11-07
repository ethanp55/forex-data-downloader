import { Injectable, signal, WritableSignal } from "@angular/core";
import { HttpClient, HttpEventType, HttpHeaders } from "@angular/common/http";
import { Candle } from "../downloader/response/candle";
import { CandlesDownloadRequest } from "../downloader/request/candles-download-request";
import { catchError, of } from "rxjs";

@Injectable({
    providedIn: "root",
})
export class CandleService {
    constructor(private http: HttpClient) {}

    private backendUrl = "http://localhost:9000/downloadCandles";

    public candlesSignal: WritableSignal<Candle[]> = signal([]);
    public errorMessageSignal: WritableSignal<string | null> = signal(null);

    public downloadCandles(candlesDownloadRequest: CandlesDownloadRequest): void {
        const headers = new HttpHeaders({
            "Content-Type": "application/json",
            Accept: "application/json",
        });
        this.http
            .post(this.backendUrl, JSON.stringify(candlesDownloadRequest), {
                headers: headers,
                responseType: "text",
                observe: "events",
            })
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
                    this.errorMessageSignal.set(
                        `status = ${status}; code = ${code}; message = ${message}`
                    );
                    this.candlesSignal.set([]);
                    return of([]);
                })
            )
            .subscribe((response) => {
                if (!Array.isArray(response) && response.type === HttpEventType.Response) {
                    const candlesBody = response.body!;
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
                            `Unknown body data type (expected an array): ${candlesBody}`
                        );
                        this.candlesSignal.set([]);
                    }
                }
            });
    }

    protected parseCandles(data: any[]): Candle[] {
        return data.map((item) => {
            return Candle.fromJSON(item);
        });
    }
}
