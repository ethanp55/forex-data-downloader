import { ComponentHarness } from "@angular/cdk/testing";
import { MatDateRangeInputHarness } from "@angular/material/datepicker/testing";

export class DownloaderHarness extends ComponentHarness {
    static hostSelector = "app-downloader";

    // Form controls
    private getCurrencyPairFormInput = this.locatorFor('[id="currencyPair"]');
    private getTimeFrameFormInput = this.locatorFor('[id="granularity"]');
    private getPricingComponenetsFormInput = this.locatorFor('[id="pricingComponents"]');
    private getDateRangeFormInput = this.locatorFor(MatDateRangeInputHarness);

    async currencyPairFormInput() {
        return await this.getCurrencyPairFormInput();
    }

    async timeFrameFormInput() {
        return await this.getTimeFrameFormInput();
    }

    async pricingComponentsFormInput() {
        return await this.getPricingComponenetsFormInput();
    }

    async openCalendar() {
        const dateRangeHarness = await this.getDateRangeFormInput();
        await dateRangeHarness.openCalendar();
    }

    async closeCalendar() {
        const dateRangeHarness = await this.getDateRangeFormInput();
        await dateRangeHarness.closeCalendar();
    }

    async setDateRange(startDate: string, endDate: string) {
        const dateHarness = await this.getDateRangeFormInput();
        const startInput = await dateHarness.getStartInput();
        const endInput = await dateHarness.getEndInput();
        startInput.setValue(startDate);
        await endInput.setValue(endDate);
    }

    // Error messages
    private currencyPairErrorMessage = this.locatorForOptional(
        '[testid="currency-pair-error-message"]'
    );
    private timeFrameErrorMessage = this.locatorForOptional('[testid="granularity-error-message"]');
    private pricingComponentsErrorMessage = this.locatorForOptional(
        '[testid="pricing-component-error-message"]'
    );
    private dateRangeErrorMessage = this.locatorForOptional('[testid="date-range-error-message"]');
    private serverErrorMessage = this.locatorForOptional('[testid="server-error-message"]');

    async currencyPairErrorMessageExists() {
        const errorMessage = await this.currencyPairErrorMessage();
        return !!errorMessage;
    }

    async timeFrameErrorMessageExists() {
        const errorMessage = await this.timeFrameErrorMessage();
        return !!errorMessage;
    }

    async pricingComponentsErrorMessageExists() {
        const errorMessage = await this.pricingComponentsErrorMessage();
        return !!errorMessage;
    }

    async dateRangeErrorMessageExists() {
        const errorMessage = await this.dateRangeErrorMessage();
        return !!errorMessage;
    }

    async serverErrorMessageExists() {
        const errorMessage = await this.serverErrorMessage();
        return !!errorMessage;
    }

    async serverErrorMessageText() {
        const errorMessage = await this.serverErrorMessage();
        return errorMessage?.text() ?? "";
    }

    async noErrorMessagesExist() {
        const currencyPairErrorMessageExists = await this.currencyPairErrorMessageExists();
        const timeFrameErrorMessageExists = await this.timeFrameErrorMessageExists();
        const pricingComponentsErrorMessageExists =
            await this.pricingComponentsErrorMessageExists();
        const dateRangeErrorMessageExists = await this.dateRangeErrorMessageExists();

        return (
            !currencyPairErrorMessageExists &&
            !timeFrameErrorMessageExists &&
            !pricingComponentsErrorMessageExists &&
            !dateRangeErrorMessageExists
        );
    }

    async allErrorMessagesExist() {
        const currencyPairErrorMessageExists = await this.currencyPairErrorMessageExists();
        const timeFrameErrorMessageExists = await this.timeFrameErrorMessageExists();
        const pricingComponentsErrorMessageExists =
            await this.pricingComponentsErrorMessageExists();
        const dateRangeErrorMessageExists = await this.dateRangeErrorMessageExists();

        return (
            currencyPairErrorMessageExists &&
            timeFrameErrorMessageExists &&
            pricingComponentsErrorMessageExists &&
            dateRangeErrorMessageExists
        );
    }

    // Warning messages
    private dataTooOldWarningMessage = this.locatorForOptional('[testid="data-too-old-warning"]');
    private tooManyCandlesWarningMessage = this.locatorForOptional(
        '[testid="too-many-candles-warning"]'
    );

    async dataTooOldWarningMessageExists() {
        const warningMessage = await this.dataTooOldWarningMessage();
        return !!warningMessage;
    }

    async tooManyCandlesWarningMessageExists() {
        const warningMessage = await this.tooManyCandlesWarningMessage();
        return !!warningMessage;
    }

    // Buttons
    private downloadButton = this.locatorFor('[testid="download-button"]');
    private saveAsCsvButton = this.locatorFor('[testid="csv-button"]');

    async downloadIsDisabled() {
        const downloadButton = await this.downloadButton();
        const isDisabled = await downloadButton.hasClass("disabled");
        return isDisabled;
    }

    async downloadHasSpinner() {
        const downloadSpinner = await this.locatorForOptional('[testid="download-spinner"]')();
        return !!downloadSpinner;
    }

    async clickDownloadButton() {
        const button = await this.downloadButton();
        await button.click();
    }

    async csvIsDisabled() {
        const csvButton = await this.saveAsCsvButton();
        const isDisabled = await csvButton.hasClass("disabled");
        return isDisabled;
    }

    // Candle table
    private candlesTableRows = this.locatorForAll('table[testid="candles-table"] tr');

    async getTableRows() {
        const rows = await this.candlesTableRows();
        return rows;
    }

    async numRowsInTable() {
        const rows = await this.candlesTableRows();

        return rows.length - 1; // Subtract 1 to ignore the first row of column headers
    }
}
