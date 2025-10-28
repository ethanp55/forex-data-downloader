import { Component } from '@angular/core';
import { CurrencyPair } from './request/currency-pair.enums';
import { Granularity } from './request/granularity.enums';
import { PricingComponent } from './request/pricing-component.enums';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-downloader',
  imports: [FormsModule, CommonModule],
  templateUrl: './downloader.html',
  styleUrl: './downloader.css',
})
export class Downloader {
    // Options
    protected readonly currencyPairOptions = CurrencyPair;
    protected readonly granularityOptions = Granularity;
    protected readonly pricingComponentOptions = PricingComponent;

    // Selected values
    protected currencyPair: CurrencyPair = CurrencyPair.EUR_USD;
    protected granularity: Granularity = Granularity.H1;
    protected pricingComponents: PricingComponent[] = [];

    public togglePricingOption(option: PricingComponent): void {
      const index = this.pricingComponents.indexOf(option);

      if (index > -1) {
        this.pricingComponents.splice(index, 1);
      } else {
        this.pricingComponents.push(option);
      }
  }



    // public submitEnabled(): boolean {
    //   return this.currencyPair !== undefined && this.granularity !== undefined && this.pricingComponents !== undefined && this.pricingComponents.length > 0;
    // }
}

// TODO: 
// - Add option to select start and end dates
// - Add submit button
// - Only enable submit button when the data is clean and ready to go
// - Create service to send requests and receive responses from backend
