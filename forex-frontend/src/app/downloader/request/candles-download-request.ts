import { CurrencyPair } from "./currency-pair.enums";
import { Granularity } from "./granularity.enums";
import { PricingComponent } from "./pricing-component.enums";

export class CandlesDownloadRequest {
    constructor(
        public currencyPair: CurrencyPair,
        public granularity: Granularity,
        public pricingComponents: PricingComponent[],
        public fromDate: string,
        public toDate: string,
    ) {}
}
