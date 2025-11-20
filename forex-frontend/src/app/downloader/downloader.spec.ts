import { ComponentFixture, TestBed } from "@angular/core/testing";
import { Downloader } from "./downloader";
import { CandleService } from "../candle-service/candle-service";
import { FormBuilder } from "@angular/forms";
import { provideNativeDateAdapter } from "@angular/material/core";
import { DownloaderHarness } from "./downloader.harness";
import { TestbedHarnessEnvironment } from "@angular/cdk/testing/testbed";
import { Candle, Price } from "./response/candle";

describe("Downloader", () => {
    let component: Downloader;
    let componentHarness: DownloaderHarness;
    let fixture: ComponentFixture<Downloader>;
    let candleServiceSpy: jasmine.SpyObj<CandleService>;

    beforeEach(async () => {
        candleServiceSpy = jasmine.createSpyObj("CandleService", [
            "candlesSignal",
            "errorMessageSignal",
            "downloadCandles",
            "errorMessageSignal.set",
        ]);
        candleServiceSpy.candlesSignal.and.returnValue([]);
        candleServiceSpy.errorMessageSignal.and.returnValue(null);

        await TestBed.configureTestingModule({
            imports: [Downloader],
            providers: [
                FormBuilder,
                provideNativeDateAdapter(),
                { provide: CandleService, useValue: candleServiceSpy },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(Downloader);
        componentHarness = await TestbedHarnessEnvironment.harnessForFixture(
            fixture,
            DownloaderHarness
        );
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should be created", () => {
        expect(component).toBeTruthy();
    });

    describe("on errors", () => {
        describe("from the form", () => {
            it("should not be shown when the component is first loaded", async () => {
                const noErrorMessages = await componentHarness.noErrorMessagesExist();
                expect(noErrorMessages).toBeTrue();
            });

            it("should be shown after a form input is clicked on but not provided a value", async () => {
                // Currency pair
                let formControl = await componentHarness.currencyPairFormInput();
                await formControl.blur();
                let errorMessageShown = await componentHarness.currencyPairErrorMessageExists();
                expect(errorMessageShown).toBeTrue();

                // Time frame
                formControl = await componentHarness.timeFrameFormInput();
                await formControl.blur();
                errorMessageShown = await componentHarness.timeFrameErrorMessageExists();
                expect(errorMessageShown).toBeTrue();

                // Granularity
                formControl = await componentHarness.pricingComponentsFormInput();
                await formControl.click();
                await formControl.click();
                errorMessageShown = await componentHarness.pricingComponentsErrorMessageExists();
                expect(errorMessageShown).toBeTrue();

                // Date range
                await componentHarness.openCalendar();
                await componentHarness.closeCalendar();
                errorMessageShown = await componentHarness.dateRangeErrorMessageExists();
                expect(errorMessageShown).toBeTrue();
            });

            it("should all be shown when no values are specified but a user clicks on the download button", async () => {
                await componentHarness.clickDownloadButton();
                const allErrorMessages = await componentHarness.allErrorMessagesExist();
                expect(allErrorMessages).toBeTrue();
            });

            it("should show a date error if the start date is after the end date", async () => {
                await componentHarness.setDateRange("10/31/2025", "10/1/2025");
                const errorMessageShown = await componentHarness.dateRangeErrorMessageExists();
                expect(errorMessageShown).toBeTrue();
            });

            it("should show a date error if the dates are in the future", async () => {
                // Set the end date to a year in the future and make sure there's an error
                const currDate = new Date(Date.now());
                const currDateString = `${currDate.getMonth()}/${currDate.getDate()}/${currDate.getFullYear()}`;
                currDate.setFullYear(currDate.getFullYear() + 1);
                const futureDateString = `${currDate.getMonth()}/${currDate.getDate()}/${currDate.getFullYear()}`;
                currDate.setFullYear(currDate.getFullYear() - 2);
                const startDateString = `${currDate.getMonth()}/${currDate.getDate()}/${currDate.getFullYear()}`;
                await componentHarness.setDateRange(startDateString, futureDateString);
                let errorMessageShown = await componentHarness.dateRangeErrorMessageExists();
                expect(errorMessageShown).toBeTrue();

                // Set the end date back to the current date and make sure the error is gone
                await await componentHarness.setDateRange(startDateString, currDateString);
                errorMessageShown = await componentHarness.dateRangeErrorMessageExists();
                expect(errorMessageShown).toBeFalse();
            });
        });

        describe("from the server", () => {
            it("should be displayed properly", async () => {
                candleServiceSpy.errorMessageSignal.and.returnValue("foo bar server error");
                fixture.detectChanges();
                const errorMessageShown = await componentHarness.serverErrorMessageExists();
                expect(errorMessageShown).toBeTrue();
                const errorMessageText = await componentHarness.serverErrorMessageText();
                expect(errorMessageText).toBe("Error: foo bar server error");
            });
        });
    });

    describe("warning messages", () => {
        it("should be displayed when downloading more than 5000 candles", async () => {
            // Set the start and end dates
            const currDate = new Date(Date.now());
            const endDateString = `${currDate.getMonth()}/${currDate.getDate()}/${currDate.getFullYear()}`;
            currDate.setMonth(currDate.getMonth() - 1);
            const startDateString = `${currDate.getMonth()}/${currDate.getDate()}/${currDate.getFullYear()}`;
            await componentHarness.setDateRange(startDateString, endDateString);

            // Set the time frame to one minute (index 8 in the dropdown select menu)
            const granularityInput = await componentHarness.timeFrameFormInput();
            await granularityInput.selectOptions(8);
            const warningMessageShown = await componentHarness.tooManyCandlesWarningMessageExists();

            expect(warningMessageShown).toBeTrue();
        });

        it("should be displayed when downloading data that is several years old", async () => {
            await componentHarness.setDateRange("10/1/1975", "10/31/1975");
            const warningMessageShown = await componentHarness.dataTooOldWarningMessageExists();
            expect(warningMessageShown).toBeTrue();
        });
    });

    describe("UI", () => {
        it("should disable the download button before every required field is specified", async () => {
            // Should be disabled by default (when the component first loads)
            let isDisabled = await componentHarness.downloadIsDisabled();
            expect(isDisabled).toBeTrue();

            // Should still be disabled after specifying some fields but not all
            let formInput = await componentHarness.currencyPairFormInput();
            formInput.selectOptions(0);
            formInput = await componentHarness.timeFrameFormInput();
            formInput.selectOptions(0);
            formInput = await componentHarness.pricingComponentsFormInput();
            await formInput.click();
            isDisabled = await componentHarness.downloadIsDisabled();
            expect(isDisabled).toBeTrue();

            // Should be enabled once every field is specified
            await componentHarness.setDateRange("10/1/2025", "10/31/2025");
            isDisabled = await componentHarness.downloadIsDisabled();
            expect(isDisabled).toBeFalse();
        });

        it("should disable the download button and show a spinner once the button is clicked and we're waiting for a server response", async () => {
            // Specify the form inputs
            let formInput = await componentHarness.currencyPairFormInput();
            formInput.selectOptions(0);
            formInput = await componentHarness.timeFrameFormInput();
            formInput.selectOptions(0);
            formInput = await componentHarness.pricingComponentsFormInput();
            formInput.click();
            await componentHarness.setDateRange("10/1/2025", "10/31/2025");

            // Click the button and check its properties
            await componentHarness.clickDownloadButton();
            const isDisabled = await componentHarness.downloadIsDisabled();
            expect(isDisabled).toBeTrue();
            const hasSpinner = await componentHarness.downloadHasSpinner();
            expect(hasSpinner).toBeTrue();
        });

        it("should only enable the save as csv button when there is candle data", async () => {
            // The component starts with no data, so the button should be disabled
            let isDisabled = await componentHarness.csvIsDisabled();
            expect(isDisabled).toBeTrue();

            // Add some data and make sure the button is no longer disabled
            const candle = new Candle(true, 1000, new Date(Date.now()));
            candleServiceSpy.candlesSignal.and.returnValue([candle]);
            fixture.detectChanges();
            isDisabled = await componentHarness.csvIsDisabled();
            expect(isDisabled).toBeFalse();

            // Remove the data and make sure the button is disabled again
            candleServiceSpy.candlesSignal.and.returnValue([]);
            fixture.detectChanges();
            isDisabled = await componentHarness.csvIsDisabled();
            expect(isDisabled).toBeTrue();
        });

        it("should show the proper table headers", async () => {
            const tableRows = await componentHarness.getTableRows();
            const headersText = await tableRows[0].text();
            const expectedHeadersString =
                "CompleteVolumeTimeBid OpenBid HighBid LowBid CloseMid OpenMid HighMid LowMid CloseAsk OpenAsk HighAsk LowAsk Close";
            expect(headersText).toBe(expectedHeadersString);
        });
    });

    describe("on successful downloads", () => {
        it("should display candles in the table", async () => {
            const candle = new Candle(true, 1000, new Date(Date.now()));
            candleServiceSpy.candlesSignal.and.returnValue(Array(50).fill(candle));
            fixture.detectChanges();
            const numRows = await componentHarness.numRowsInTable();
            expect(numRows).toBe(50);
        });

        it("should format candles in the table properly", async () => {
            const candle = new Candle(
                true,
                1000,
                new Date(Date.now()),
                new Price(1.5, 1.501, 1.499, 1.5005)
            );
            candleServiceSpy.candlesSignal.and.returnValue([candle]);
            const tableRows = await componentHarness.getTableRows();
            const rowText = await tableRows[1].text();
            const expectedDataString = `${candle.complete}${
                candle.volume
            }${candle.time.toISOString()}${candle.bid?.o ?? ""}${candle.bid?.h ?? ""}${
                candle.bid?.l ?? ""
            }${candle.bid?.c ?? ""}${candle.mid?.o ?? ""}${candle.mid?.h ?? ""}${
                candle.mid?.l ?? ""
            }${candle.mid?.c ?? ""}${candle.ask?.o ?? ""}${candle.ask?.h ?? ""}${
                candle.ask?.l ?? ""
            }${candle.ask?.c ?? ""}`;
            expect(rowText).toBe(expectedDataString);
        });

        it("should display only up to 5000 candles in the table", async () => {
            const candle = new Candle(true, 1000, new Date(Date.now()));
            candleServiceSpy.candlesSignal.and.returnValue(Array(6000).fill(candle));
            fixture.detectChanges();
            const numRows = await componentHarness.numRowsInTable();
            expect(numRows).toBe(5000);
        });
    });
});
