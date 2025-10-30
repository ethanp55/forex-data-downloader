import { Component } from "@angular/core";
import { CurrencyPair } from "./request/currency-pair.enums";
import { Granularity } from "./request/granularity.enums";
import { PricingComponent } from "./request/pricing-component.enums";
import {
    FormBuilder,
    Validators,
    FormsModule,
    ReactiveFormsModule,
    FormGroup,
} from "@angular/forms";
import { CommonModule } from "@angular/common";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatTooltipModule } from "@angular/material/tooltip";

@Component({
    selector: "app-downloader",
    imports: [
        FormsModule,
        ReactiveFormsModule,
        CommonModule,
        MatDatepickerModule,
        MatFormFieldModule,
        MatTooltipModule,
    ],
    templateUrl: "./downloader.html",
    styleUrl: "./downloader.css",
})
export class Downloader {
    form: FormGroup;

    constructor(private formBuilder: FormBuilder) {
        // Set up the various inputs, with checks
        this.form = this.formBuilder.group(
            {
                currencyPair: [null, Validators.required],
                granularity: [null, Validators.required],
                pricingComponents: [[], Validators.required],
                startDate: [null, Validators.required],
                endDate: [null, Validators.required],
            },
            { validators: this.dateRangeValidator },
        );
    }

    // Options that users can choose from
    protected readonly currencyPairOptions = CurrencyPair;
    protected readonly granularityOptions = Granularity;
    protected readonly pricingComponentOptions = PricingComponent;

    // Used for updating the array of price options (bid, mid, and/or ask)
    public togglePricingOption(option: PricingComponent): void {
        const pricingComponents = this.form.get("pricingComponents")?.value;

        if (pricingComponents.includes(option)) {
            this.form.patchValue({
                pricingComponents: pricingComponents.filter(
                    (item: PricingComponent) => item !== option,
                ),
            });
        } else {
            pricingComponents.push(option);
            this.form.patchValue({ pricingComponents: pricingComponents });
        }
    }

    // Make sure the start date is before the end date (and that they're both defined)
    private dateRangeValidator(formGroup: FormGroup): { [key: string]: boolean } | null {
        const startDate = formGroup.get("startDate")?.value;
        const endDate = formGroup.get("endDate")?.value;

        return startDate && endDate && startDate > endDate ? { dateMismatch: true } : null;
    }

    // Create and send the download request
    downloadCandles() {
        if (this.form.valid) {
            console.log("submit");
        }
    }
}

// TODO:
// - Create service to send requests and receive responses from backend
