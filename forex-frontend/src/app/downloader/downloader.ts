import { Component, effect, WritableSignal } from "@angular/core";
import { CurrencyPair } from "./request/currency-pair.enums";
import { Granularity, getMinutes } from "./request/granularity.enums";
import { PricingComponent } from "./request/pricing-component.enums";
import {
    FormBuilder,
    Validators,
    FormsModule,
    ReactiveFormsModule,
    FormGroup,
    AbstractControl,
} from "@angular/forms";
import { CommonModule } from "@angular/common";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatTooltipModule } from "@angular/material/tooltip";
import { CandlesDownloadRequest } from "./request/candles-download-request";
import { CandleService } from "../candle-service/candle-service";
import { Candle } from "./response/candle";

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

    constructor(
        private formBuilder: FormBuilder,
        private candleService: CandleService,
    ) {
        // Set up the various inputs, with checks
        this.form = this.formBuilder.group({
            currencyPair: [null, Validators.required],
            granularity: [null, Validators.required],
            pricingComponents: [[], Validators.required],
            startDate: [
                null,
                [
                    Validators.required,
                    this.dateNotInFutureValidator,
                    this.startDateBeforeEndDateValidator,
                ],
            ],
            endDate: [
                null,
                [
                    Validators.required,
                    this.dateNotInFutureValidator,
                    this.startDateBeforeEndDateValidator,
                ],
            ],
        });

        this.candlesSignal = this.candleService.candlesSignal;
        this.errorMessageSignal = this.candleService.errorMessageSignal;

        // Download candles whenever they're updated
        effect(() => this.downloadDataAsCSV(this.candlesSignal()));
    }

    private startDateBeforeEndDateValidator(
        control: AbstractControl,
    ): { [key: string]: any } | null {
        const startDateField = control.get("startDate");
        const endDateField = control.get("endDate");

        if (startDateField?.value && endDateField?.value) {
            return startDateField.value > endDateField ? { startDateBeforeEndDate: false } : null;
        }

        return null;
    }

    private dateNotInFutureValidator(control: AbstractControl): { [key: string]: any } | null {
        const date = control.value as Date;
        const today = new Date();

        return date > today ? { dateNotInFuture: false } : null;
    }

    // Options that users can choose from
    protected readonly currencyPairOptions = CurrencyPair;
    protected readonly granularityOptions = Granularity;
    protected readonly pricingComponentOptions = PricingComponent;

    // Flag for checking if the pricingComponents array has been touched
    private pricingComponentsChanged = false;

    // Flag for checking if a user tried to submit
    private submitted = false;

    // Estimate for how many years in the past Oanda typically supports
    protected readonly numYearsBack = 20;

    // Max number of candles Oanda returns in a single request
    protected readonly oandaCandlesLimitation = 5000;

    // Signals for reading candles and/or error messages from the server
    protected readonly candlesSignal: WritableSignal<Candle[]>;
    protected readonly errorMessageSignal: WritableSignal<string | null>;

    // Used for updating the array of price options (bid, mid, and/or ask)
    protected togglePricingOption(option: PricingComponent): void {
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

        this.pricingComponentsChanged = true;
    }

    // Determines if the UI should display errors around required fields
    protected showError(fieldName: string): boolean | undefined {
        const field = this.form.get(fieldName);

        if (fieldName === "pricingComponents") {
            return field?.value.length === 0 && (this.pricingComponentsChanged || this.submitted);
        }

        return field?.invalid && (field?.dirty || field?.touched || this.submitted);
    }

    protected yearsTooFarBack(): boolean {
        const startDateField = this.form.get("startDate");
        const endDateField = this.form.get("endDate");

        if (startDateField?.valid && endDateField?.valid) {
            const startDate = startDateField.value as Date;
            const startDateYear = startDate.getFullYear();
            const currentYear = new Date().getFullYear();

            if (Math.abs(currentYear - startDateYear) >= this.numYearsBack) {
                return true;
            }
        }

        return false;
    }

    // Estimates how many candles will be downloaded -> send a warning if more than oandaCandlesLimitation (5000) will be downloaded
    protected aLotOfCandles(): boolean {
        const granularityField = this.form.get("granularity");
        const startDateField = this.form.get("startDate");
        const endDateField = this.form.get("endDate");

        if (granularityField?.valid && startDateField?.valid && endDateField?.valid) {
            const minuteIncrements = getMinutes(granularityField.value as Granularity);
            const millisecondIncrements = minuteIncrements * 60 * 1000;
            let numCandles = 0;
            let currDate = new Date(startDateField.value as Date);
            let endDate = new Date(endDateField.value as Date);

            while (currDate <= endDate) {
                currDate = new Date(currDate.getTime() + millisecondIncrements);
                numCandles++;

                // Once we hit this condition we can stop
                if (numCandles > this.oandaCandlesLimitation) {
                    break;
                }
            }

            return numCandles > this.oandaCandlesLimitation;
        }

        return false;
    }

    // Wrapper that is called through the UI and, if everything has been validated, sends the request
    protected onSubmit(): void {
        this.submitted = true;

        if (this.form.valid) {
            this.downloadCandles();
        }
    }

    // Create and send the download request
    private downloadCandles(): void {
        function _formatDate(date: Date): string {
            const _pad = (n: number) => (n < 10 ? "0" + n : n);

            const year = date.getFullYear();
            const month = _pad(date.getMonth() + 1);
            const day = _pad(date.getDate());
            const hours = _pad(date.getHours());
            const minutes = _pad(date.getMinutes());
            const seconds = _pad(date.getSeconds());

            return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
        }

        const currencyPair = this.form.get("currencyPair")?.value as CurrencyPair;
        const granularity = this.form.get("granularity")?.value as Granularity;
        const pricingComponents = this.form.get("pricingComponents")?.value as PricingComponent[];
        const startDate = this.form.get("startDate")?.value as Date;
        const endDate = this.form.get("endDate")?.value as Date;
        const fromDate = _formatDate(startDate);
        const toDate = _formatDate(endDate);

        const candlesDownloadRequest = new CandlesDownloadRequest(
            currencyPair,
            granularity,
            pricingComponents,
            fromDate,
            toDate,
        );

        this.candleService.downloadCandles(candlesDownloadRequest);
    }

    private downloadDataAsCSV(candles: Candle[]) {
        // Prevent downloads on empty data (first effect call from the constructor and any errors from the server)
        if (candles.length > 0) {
            const candlesCSV = Candle.generateCSV(candles);
            const blob = new Blob([candlesCSV], {
                type: "text/csv;charset=utf-8;",
            });
            const link = document.createElement("a");
            const url = URL.createObjectURL(blob);
            link.setAttribute("href", url);
            link.setAttribute("download", "candles.csv");
            link.style.visibility = "hidden";
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        }
    }
}
