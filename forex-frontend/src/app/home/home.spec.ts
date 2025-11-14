import { ComponentFixture, TestBed } from "@angular/core/testing";

import { Home } from "./home";

describe("Home", () => {
    let component: Home;
    let fixture: ComponentFixture<Home>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [Home],
        }).compileComponents();

        fixture = TestBed.createComponent(Home);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should be created", () => {
        expect(component).toBeTruthy();
    });

    it("should have the correct Oanda URL", () => {
        expect(component.oandaUrl).toBe("https://developer.oanda.com/rest-live-v20/instrument-ep/");
    });

    describe("text", () => {
        it("should show the correct amount of paragraphs", () => {
            const compiled = fixture.nativeElement as HTMLElement;

            expect(compiled.querySelectorAll("p").length).toBe(4);
        });

        it("should be properly displayed", () => {
            const compiled = fixture.nativeElement as HTMLElement;
            const paragraphTags = compiled.querySelectorAll("p");

            expect(paragraphTags[0].className).toBe("biggest");
            expect(paragraphTags[0].textContent).toBe("Download historical forex data.");

            expect(paragraphTags[1].className).toBe("big");
            expect(paragraphTags[1].textContent).toBe(
                "Enhance your analysis, backtesting, and trading strategies."
            );

            expect(paragraphTags[2].className).toBe("note");
            expect(paragraphTags[2].textContent).toBe(
                " Requests with more than 5000 candles might be slower and/or time out, as we are limited by Oanda's API. "
            );
            expect(paragraphTags[2].children[0].nodeName).toBe("A");
            expect(paragraphTags[2].children[0].textContent).toBe("Oanda's API");

            expect(paragraphTags[3].className).toBe("note");
            expect(paragraphTags[3].textContent).toBe(
                "Times are returned as UTC. Daily alignments are made at hour 0."
            );
        });
    });
});
