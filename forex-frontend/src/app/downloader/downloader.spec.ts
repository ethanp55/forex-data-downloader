import { ComponentFixture, TestBed } from "@angular/core/testing";
import { Downloader } from "./downloader";
import { CandleService } from "../candle-service/candle-service";
import { FormBuilder } from "@angular/forms";
import { provideNativeDateAdapter } from "@angular/material/core";
import { DownloaderHarness } from "./downloader.harness";
import { TestbedHarnessEnvironment } from "@angular/cdk/testing/testbed";
import { Candle } from "./response/candle";

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

    describe("error messages", () => {
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
                const dateHarness = await componentHarness.dateRangeFormInput();
                await dateHarness.openCalendar();
                await dateHarness.closeCalendar();
                errorMessageShown = await componentHarness.dateRangeErrorMessageExists();
                expect(errorMessageShown).toBeTrue();
            });

            it("should all be shown when no values are specified but a user clicks on the download button", async () => {
                await componentHarness.clickDownloadButton();
                const allErrorMessages = await componentHarness.allErrorMessagesExist();
                expect(allErrorMessages).toBeTrue();
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

            // it("should clear the candle table if data was previously in the table", async () => {
            //     // Add candles to the table
            //     const candle = new Candle(true, 1000, new Date(Date.now()));
            //     candleServiceSpy.candlesSignal.and.returnValue(Array(50).fill(candle));
            //     fixture.detectChanges();
            //     let numRows = await componentHarness.numRowsInTable();
            //     expect(numRows).toBe(50);

            //     // Set an error message and remove the candles
            //     candleServiceSpy.candlesSignal.and.returnValue([]);
            //     candleServiceSpy.errorMessageSignal.and.returnValue("foo bar server error");
            //     fixture.detectChanges();
            //     const errorMessageShown = await componentHarness.serverErrorMessageExists();
            //     expect(errorMessageShown).toBeTrue();
            //     const errorMessageText = await componentHarness.serverErrorMessageText();
            //     expect(errorMessageText).toBe("Error: foo bar server error");
            //     numRows = await componentHarness.numRowsInTable();
            //     expect(numRows).toBe(0);
            // });
        });
    });

    // Warnings:
    // - Trying to download more than 5000 candles should display a warning
    // - Trying to download old data should display a warning
    // UI edge cases:
    // - Download button should be disabled before every field is specified
    // - Download button should be disabled and show spinning animation while waiting for the server to return data
    // - Save as csv button should be disabled if the candles array is empty
    // Successes:
    // - Candles should be displayed in the table
    // - A max of 5000 candles should be displayed in the table
    // - Saving as a csv should work properly (this might be more complicated, so if we don't get to it don't worry)
});
