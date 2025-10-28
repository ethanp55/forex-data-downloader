import { CurrencyPair } from "./currency-pair";
import { Granularity } from "./granularity";
import { PricingComponent } from "./pricing-component";

export class CandlesDownloadRequest {
  constructor(
    public currencyPair: CurrencyPair,
    public granularity: Granularity,
    public pricingComponents: PricingComponent[],
    public fromDate: string,
    public toDate: string
  ) {
  }
}
