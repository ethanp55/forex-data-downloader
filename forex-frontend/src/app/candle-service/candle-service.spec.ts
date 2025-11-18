import { TestBed } from "@angular/core/testing";
import { CandleService } from "./candle-service";
import { HttpClient, HttpResponse } from "@angular/common/http";
import { CandlesDownloadRequest } from "../downloader/request/candles-download-request";
import { of, throwError } from "rxjs";
import { CurrencyPair } from "../downloader/request/currency-pair.enums";
import { Granularity } from "../downloader/request/granularity.enums";
import { PricingComponent } from "../downloader/request/pricing-component.enums";
import { Candle } from "../downloader/response/candle";

describe("CandleService", () => {
    let httpClientSpy: jasmine.SpyObj<HttpClient>;
    let service: CandleService;
    let mockDownloadRequest: CandlesDownloadRequest;

    beforeEach(() => {
        httpClientSpy = jasmine.createSpyObj("HttpClient", ["post"]);
        TestBed.configureTestingModule({
            providers: [CandleService, { provide: HttpClient, useValue: httpClientSpy }],
        });
        service = TestBed.inject(CandleService);
        mockDownloadRequest = new CandlesDownloadRequest(
            CurrencyPair.AUD_JPY,
            Granularity.H8,
            [PricingComponent.Bid, PricingComponent.Ask],
            "2025-09-07T00:00:00Z",
            "2025-10-07T00:00:00Z"
        );
    });

    it("should be created", () => {
        expect(service).toBeTruthy();
    });

    describe("should properly handle errors", () => {
        it("with no missing fields", () => {
            const errorResponse = {
                status: 500,
                error: JSON.stringify({ code: "SERVER_ERROR", message: "Internal Server Error" }),
            };
            httpClientSpy.post.and.returnValue(throwError(() => errorResponse));

            service.downloadCandles(mockDownloadRequest);

            const candles = service.candlesSignal();
            expect(candles.length).toBe(0);

            const errorMessage = service.errorMessageSignal();
            expect(errorMessage).toBeDefined();
            expect(errorMessage).toBe(
                "status = 500; code = SERVER_ERROR; message = Internal Server Error"
            );
        });

        it("with a missing error field", () => {
            const errorResponse = { status: 500 };
            httpClientSpy.post.and.returnValue(throwError(() => errorResponse));

            service.downloadCandles(mockDownloadRequest);

            const candles = service.candlesSignal();
            expect(candles.length).toBe(0);

            const errorMessage = service.errorMessageSignal();
            expect(errorMessage).toBeDefined();
            expect(errorMessage).toBe(
                "status = 500; code = UNKNOWN_ERROR; message = (message not provided)"
            );
        });

        it("with a missing status field", () => {
            const errorResponse = {
                error: JSON.stringify({ code: "SERVER_ERROR", message: "Internal Server Error" }),
            };
            httpClientSpy.post.and.returnValue(throwError(() => errorResponse));

            service.downloadCandles(mockDownloadRequest);

            const candles = service.candlesSignal();
            expect(candles.length).toBe(0);

            const errorMessage = service.errorMessageSignal();
            expect(errorMessage).toBeDefined();
            expect(errorMessage).toBe(
                "status = 520; code = SERVER_ERROR; message = Internal Server Error"
            );
        });

        it("with missing status and error fields", () => {
            const errorResponse = {};
            httpClientSpy.post.and.returnValue(throwError(() => errorResponse));

            service.downloadCandles(mockDownloadRequest);

            const candles = service.candlesSignal();
            expect(candles.length).toBe(0);

            const errorMessage = service.errorMessageSignal();
            expect(errorMessage).toBeDefined();
            expect(errorMessage).toBe(
                "status = 520; code = UNKNOWN_ERROR; message = (message not provided)"
            );
        });
    });

    it("should properly handle cases with no request errors, but where unexpected JSON was received from the server", () => {
        const mockResponse = "foo bar baz wtf";
        httpClientSpy.post.and.returnValue(of(new HttpResponse({ body: mockResponse })));

        service.downloadCandles(mockDownloadRequest);

        const candles = service.candlesSignal();
        expect(candles.length).toBe(0);

        const errorMessage = service.errorMessageSignal();
        expect(errorMessage).toBeDefined();
        expect(errorMessage).toBe(
            "Unknown body data type (expected an array of candles): foo bar baz wtf"
        );
    });

    it("should clear the candles signal when an error occurs", () => {
        // Populate the candles signal with initial data
        const candle = new Candle(true, 1000, new Date(Date.now()));
        service.candlesSignal.set(Array(50).fill(candle));
        let candles = service.candlesSignal();
        expect(candles.length).toBe(50);

        // Mock an error response and check that the candles signal is empty
        const errorResponse = {};
        httpClientSpy.post.and.returnValue(throwError(() => errorResponse));
        service.downloadCandles(mockDownloadRequest);
        candles = service.candlesSignal();
        expect(candles.length).toBe(0);
    });

    describe("should properly download candles", () => {
        it("when there is a single candle array returned from the server", () => {
            const mockResponse =
                '[{"complete": true, "volume": 100, "time": "1631136000", "bid": { "o": "1.1", "h": "1.2", "l": "1.0", "c": "1.15" }, "mid": { "o": "1.05", "h": "1.1", "l": "1.0", "c": "1.05" }, "ask": { "o": "1.2", "h": "1.25", "l": "1.15", "c": "1.2" }}]';
            httpClientSpy.post.and.returnValue(of(new HttpResponse({ body: mockResponse })));

            service.downloadCandles(mockDownloadRequest);

            const errorMessage = service.errorMessageSignal();
            expect(errorMessage).toBeNull();

            const candles = service.candlesSignal();
            expect(candles.length).toBe(1);
            const expectedCandle = Candle.fromJSON(JSON.parse(mockResponse)[0]);
            expect(candles[0]).toEqual(expectedCandle);
        });

        it("when there are multiple candle arrays returned from the server", () => {
            const mockResponse =
                '[{"complete": true, "volume": 100, "time": "1631136000", "bid": { "o": "1.1", "h": "1.2", "l": "1.0", "c": "1.15" }, "mid": { "o": "1.05", "h": "1.1", "l": "1.0", "c": "1.05" }, "ask": { "o": "1.2", "h": "1.25", "l": "1.15", "c": "1.2" }}],[{"complete": true, "volume": 100, "time": "1631136000", "bid": { "o": "1.1", "h": "1.2", "l": "1.0", "c": "1.15" }, "mid": { "o": "1.05", "h": "1.1", "l": "1.0", "c": "1.05" }, "ask": { "o": "1.2", "h": "1.25", "l": "1.15", "c": "1.2" }}]';
            httpClientSpy.post.and.returnValue(of(new HttpResponse({ body: mockResponse })));

            service.downloadCandles(mockDownloadRequest);

            const errorMessage = service.errorMessageSignal();
            expect(errorMessage).toBeNull();

            const candles = service.candlesSignal();
            expect(candles.length).toBe(2);
        });

        it("when there are multiple candle arrays returned from the server, each with different numbers of candles", () => {
            const mockResponse =
                '[{"complete": true, "volume": 100, "time": "1631136000", "bid": { "o": "1.1", "h": "1.2", "l": "1.0", "c": "1.15" }, "mid": { "o": "1.05", "h": "1.1", "l": "1.0", "c": "1.05" }, "ask": { "o": "1.2", "h": "1.25", "l": "1.15", "c": "1.2" }},{"complete": true, "volume": 100, "time": "1631136000", "bid": { "o": "1.1", "h": "1.2", "l": "1.0", "c": "1.15" }, "mid": { "o": "1.05", "h": "1.1", "l": "1.0", "c": "1.05" }, "ask": { "o": "1.2", "h": "1.25", "l": "1.15", "c": "1.2" }}],[{"complete": true, "volume": 100, "time": "1631136000", "bid": { "o": "1.1", "h": "1.2", "l": "1.0", "c": "1.15" }, "mid": { "o": "1.05", "h": "1.1", "l": "1.0", "c": "1.05" }, "ask": { "o": "1.2", "h": "1.25", "l": "1.15", "c": "1.2" }}]';
            httpClientSpy.post.and.returnValue(of(new HttpResponse({ body: mockResponse })));

            service.downloadCandles(mockDownloadRequest);

            const errorMessage = service.errorMessageSignal();
            expect(errorMessage).toBeNull();

            const candles = service.candlesSignal();
            expect(candles.length).toBe(3);
        });
    });
});
