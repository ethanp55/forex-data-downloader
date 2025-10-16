package request

case class CandlesDownloadRequest(
    currencyPair: CurrencyPair,
    granularity: Granularity,
    pricingComponent: PricingComponent,
    fromDate: String,
    toDate: String
)
