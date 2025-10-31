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

    public downloadCandles(candlesDownloadRequest: CandlesDownloadRequest): void {
        const headers = new HttpHeaders({
            "Content-Type": "application/json",
        });
        this.http
            .post<any>(this.backendUrl, JSON.stringify(candlesDownloadRequest), { headers })
            .pipe(
                catchError((error) => {
                    console.log(error);
                    return of({ code: error.status, message: "Failed to download candles" });
                }),
            )
            .subscribe((response) => {
                if (Array.isArray(response)) {
                    const candles = this.parseCandles(response);
                    this.candlesSignal.set(candles);
                } else {
                    console.error(response);
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

// TODO:
// - Handle error messages returned from the server
// - Parse candles as csv
// - Add table to data downloader component that displays the candles (be careful about loading every candle into the table right away)
