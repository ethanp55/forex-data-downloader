import { Injectable, signal, WritableSignal } from "@angular/core";
import { HttpClient, HttpHeaders } from "@angular/common/http";
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
        });
        this.http
            .post<any>(this.backendUrl, JSON.stringify(candlesDownloadRequest), { headers })
            .pipe(
                catchError((error) => {
                    console.error(error);
                    const serverError = error.error || null;
                    const code = serverError?.code ?? "UNKNOWN_ERROR";
                    const message = serverError?.message ?? "(message not provided)";
                    const status = error.status || 520;
                    this.errorMessageSignal.set(
                        `status = ${status}; code = ${code}; message = ${message}`
                    );
                    this.candlesSignal.set([]);
                    return of([]);
                })
            )
            .subscribe((response) => {
                if (Array.isArray(response)) {
                    const candles = this.parseCandles(response);
                    this.candlesSignal.set(candles);

                    if (response.length > 0) {
                        this.errorMessageSignal.set(null);
                    }
                } else {
                    console.error(`Unknown data type in response: ${response}`);
                    this.errorMessageSignal.set(
                        `Unknown data type (expected an array): ${response}`
                    );
                    this.candlesSignal.set([]);
                }
            });
    }

    protected parseCandles(data: any[]): Candle[] {
        return data.map((item) => {
            return Candle.fromJSON(item);
        });
    }
}
