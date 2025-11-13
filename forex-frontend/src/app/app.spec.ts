import { ComponentFixture, TestBed } from "@angular/core/testing";
import { App } from "./app";
import { provideRouter } from "@angular/router";

describe("App", () => {
    let component: App;
    let fixture: ComponentFixture<App>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [App],
            providers: [provideRouter([])],
        }).compileComponents();

        fixture = TestBed.createComponent(App);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should be created", () => {
        expect(component).toBeTruthy();
    });

    it("should show the correct title", () => {
        const compiled = fixture.nativeElement as HTMLElement;

        expect(compiled.querySelector("h1")?.textContent).toBe("Forex Candle Downloader");
    });

    it("should include the navigator component", () => {
        const compiled = fixture.nativeElement;

        expect(compiled.querySelector("app-navigator")).toBeTruthy();
    });

    it("should include the title and navigator component in the header", () => {
        const compiled = fixture.nativeElement;
        const header = compiled.querySelector("header");
        const headerChildren = header.childNodes;

        expect(header).toBeTruthy();
        expect(headerChildren.length).toBe(2);
        expect(headerChildren[0].nodeName).toBe("H1");
        expect(headerChildren[0].textContent).toBe("Forex Candle Downloader");
        expect(headerChildren[1].nodeName).toBe("APP-NAVIGATOR");
    });

    it("should contain a router outlet", () => {
        const compiled = fixture.nativeElement;

        expect(compiled.querySelector("router-outlet")).toBeTruthy();
    });
});
