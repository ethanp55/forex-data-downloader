import { CurrencyPair } from "./currency-pair.enums";
import { Granularity } from "./granularity.enums";
import { PricingComponent } from "./pricing-component.enums";

/**
 * Simple class that holds data for a candles download request.
 */
export class CandlesDownloadRequest {
    /**
     * Class constructor.  Nothing special is needed since we just use the class
     * to store data.
     *
     * @param currencyPair Currency pair to download data for.
     * @param granularity Time frame/granularity of the data that the user wants.
     * @param pricingComponents Which price components to include (bid, mid, and/or ask).
     * @param fromDate Starting date to download data.
     * @param toDate End/final date to download data.
     */
    constructor(
        public currencyPair: CurrencyPair,
        public granularity: Granularity,
        public pricingComponents: PricingComponent[],
        public fromDate: string,
        public toDate: string
    ) {}
}
