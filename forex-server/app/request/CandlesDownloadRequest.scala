package request

case class CandlesDownloadRequest(
    currencyPair: CurrencyPair,
    granularity: Granularity,
    pricingComponents: Seq[PricingComponent],
    fromDate: String,
    toDate: String
)
