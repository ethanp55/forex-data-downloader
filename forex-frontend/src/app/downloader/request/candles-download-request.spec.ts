import { TestBed } from "@angular/core/testing";
import { CandlesDownloadRequest } from "./candles-download-request";
import { CurrencyPair } from "./currency-pair.enums";
import { Granularity } from "./granularity.enums";
import { PricingComponent } from "./pricing-component.enums";

describe("CandlesDownloadRequest", () => {
    beforeEach(() => {
        TestBed.configureTestingModule({});
    });

    it("should serialize to JSON correctly", () => {
        const candlesDownloadRequest = new CandlesDownloadRequest(
            CurrencyPair.AUD_JPY,
            Granularity.H8,
            [PricingComponent.Bid, PricingComponent.Ask],
            "2025-09-07T00:00:00Z",
            "2025-10-07T00:00:00Z"
        );
        const expectedJson =
            '{"currencyPair":"AUD_JPY","granularity":"H8","pricingComponents":["B","A"],"fromDate":"2025-09-07T00:00:00Z","toDate":"2025-10-07T00:00:00Z"}';
        const jsonifiedRequest = JSON.stringify(candlesDownloadRequest);

        expect(jsonifiedRequest).toBe(expectedJson);
    });
});
