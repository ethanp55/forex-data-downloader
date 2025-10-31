import { Injectable } from "@angular/core";
import { HttpClient, HttpHeaders } from "@angular/common/http";

@Injectable({
    providedIn: "root",
})
export class CandleService {
    constructor(private http: HttpClient) {}

    private backendUrl = "http://localhost:8000/downloadCandles";
}

// TODO:
// - Create service to send requests and receive responses from backend (including JSONifying requests and parsing candles)
//      Might help: https://medium.com/@sehban.alam/http-calls-in-angular-the-right-way-a3752abf4496

// TODO (next week):
// - Parse candles as csv
// - Add table to data downloader component that displays the candles (be careful about loading every candle into the table right away)
