import { Component } from "@angular/core";
import { CurrencyPair } from "./request/currency-pair.enums";
import { Granularity } from "./request/granularity.enums";
import { PricingComponent } from "./request/pricing-component.enums";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { CommonModule } from "@angular/common";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatFormFieldModule } from "@angular/material/form-field";

@Component({
    selector: "app-downloader",
    imports: [
        FormsModule,
        ReactiveFormsModule,
        CommonModule,
        MatDatepickerModule,
        MatFormFieldModule,
    ],
    templateUrl: "./downloader.html",
    styleUrl: "./downloader.css",
})
export class Downloader {
    // Options
    protected readonly currencyPairOptions = CurrencyPair;
    protected readonly granularityOptions = Granularity;
    protected readonly pricingComponentOptions = PricingComponent;

    // Selected values
    protected currencyPair?: CurrencyPair = undefined;
    protected granularity?: Granularity = undefined;
    protected pricingComponents: PricingComponent[] = [];
    protected startDate?: Date = undefined;
    protected endDate?: Date = undefined;

    public togglePricingOption(option: PricingComponent): void {
        const index = this.pricingComponents.indexOf(option);

        if (index > -1) {
            this.pricingComponents.splice(index, 1);
        } else {
            this.pricingComponents.push(option);
        }
    }
}

// TODO:
// - Add validation
// - Add submit button
// - Only enable submit button when the data is clean and ready to go
// - Change tooltip to only show on hover
// - Create service to send requests and receive responses from backend
