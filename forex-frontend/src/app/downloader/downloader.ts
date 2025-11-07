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
/**
 * Main component of the application.  This component takes inputs from the user, sends download
 * requests, and displays erorr messages or candles when responses come back from the server.
 */
export class Downloader {
    protected readonly form: FormGroup;

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

    // Flag for indicating that we're waiting for the server to download candles
    protected waitingForServer = false;

    /**
     * Constructor.  Used to set up the form and signals.
     *
     * @param formBuilder An Angular FormBuilder, used to create a group of form controls.
     * This is useful for checking/validating data sent by the user.
     *
     * @param candleService A custom service that is used to send download requests to a backend
     * server and parse the results.  The UI is updated with any errors or successful data.
     */
    constructor(private formBuilder: FormBuilder, private candleService: CandleService) {
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

        effect(() => {
            if (this.candlesSignal().length >= 0) {
                this.waitingForServer = false;
            }
        });
    }

    /**
     * Helper function for checking if the start date entered by user occurs before the end date.
     *
     * @param control Form control that contains the start and end date entered by the user.
     *
     * @returns If the start date is not before the end date, the function returns an object with a key-value
     * pair indicating that the check failed.  Otherwise, null is returned (indicating that there are no issues).
     */
    private startDateBeforeEndDateValidator(
        control: AbstractControl
    ): { [key: string]: any } | null {
        const startDateField = control.get("startDate");
        const endDateField = control.get("endDate");

        if (startDateField?.value && endDateField?.value) {
            return startDateField.value > endDateField ? { startDateBeforeEndDate: false } : null;
        }

        return null;
    }

    /**
     * Helper function for checking if an entered date is in the future.
     *
     * @param control Form control that contains the date entered by the user.
     *
     * @returns If the date is in the future, the function returns an object with a key-value pair indicating that
     * the check failed.  Otherwise, null is returned (indicating that there are no issues).
     */
    private dateNotInFutureValidator(control: AbstractControl): { [key: string]: any } | null {
        const date = control.value as Date;
        const today = new Date();

        return date > today ? { dateNotInFuture: false } : null;
    }

    /**
     * Function called in the UI for toggling price options (bid, mid, and/or ask)
     *
     * @param option The pricing option clicked on by the user in the UI
     */
    protected togglePricingOption(option: PricingComponent): void {
        // Grab the current pricing options specified by the user
        const pricingComponents = this.form.get("pricingComponents")?.value;

        // If the clicked-on option is in the specified options, remove it; otherwise, add it
        if (pricingComponents.includes(option)) {
            this.form.patchValue({
                pricingComponents: pricingComponents.filter(
                    (item: PricingComponent) => item !== option
                ),
            });
        } else {
            pricingComponents.push(option);
            this.form.patchValue({ pricingComponents: pricingComponents });
        }

        this.pricingComponentsChanged = true;
    }

    /**
     * Function called in the UI to determine if an error message should be displayed, indicating that the user
     * needs to specify the field before they can request to download candle data
     *
     * @param fieldName The field that is being checked
     * @returns A boolean or undefined.  Undefined is only returned if the field currently hasn't been specified by
     * the user (in that case, an error shouldn't be shown yet).  A boolean is returned if the field has been specified;
     * if the field is invalid and has been modified and/or the user tried to submit, the UI should display an error.
     */
    protected showError(fieldName: string): boolean | undefined {
        const field = this.form.get(fieldName);

        // Since pricingComponents is an array, we need a special check for it
        if (fieldName === "pricingComponents") {
            return field?.value.length === 0 && (this.pricingComponentsChanged || this.submitted);
        }

        return field?.invalid && (field?.dirty || field?.touched || this.submitted);
    }

    /**
     * Function called in the UI to see if a warning message should be displayed.  If one or both of the dates given
     * by the user are several years in the past, we should warn them that might get little to no data because Oanda
     * doesn't typically provide really old data.
     *
     * @returns Boolean indicating whether a warning message should be displayed.
     */
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

    /**
     * Function called in the UI to see if a warning message should be displayed.  If more than 5000 candles
     * are estimated to be returned from the server, warn the user because their request might take a longer time
     * to complete and might not even be able to complete due to timeouts or excessive data usage.
     *
     * @returns Boolean indicating whether a warning message should be displayed.
     */
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

    /**
     * Wrapper function called in the UI to try to download candles from the server.
     */
    protected onSubmit(): void {
        this.submitted = true;

        // Make sure the form is valid before trying to download candles
        if (this.form.valid) {
            this.downloadCandles();
        }
    }

    /**
     * Actual function that sends download requests to the server.
     */
    private downloadCandles(): void {
        // Helper function for converting dates entered by the user into the "yyyy-MM-dd HH:mm:ss"
        // format (expected by the server)
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

        // Create the request
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
            toDate
        );

        // Before downloading new candles, clear any existing error messages
        this.errorMessageSignal.set(null);

        // Indicate that we're waiting for the server (to show the loading indicator)
        this.waitingForServer = true;

        // Send the request to the server
        this.candleService.downloadCandles(candlesDownloadRequest);
    }

    /**
     * Function called in the UI to download the candles as a CSV file
     */
    protected downloadDataAsCSV(): void {
        const candles = this.candlesSignal();

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
