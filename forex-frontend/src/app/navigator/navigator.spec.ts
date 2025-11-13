import { ComponentFixture, TestBed } from "@angular/core/testing";
import { Navigator } from "./navigator";
import { Component } from "@angular/core";
import { provideRouter } from "@angular/router";
import { RouterTestingHarness } from "@angular/router/testing";

@Component({
    template: "<h1>Home</h1>",
})
class MockHome {}

@Component({
    template: "<h1>Download</h1>",
})
class MockDownload {}

describe("Navigator", () => {
    let component: Navigator;
    let fixture: ComponentFixture<Navigator>;
    let harness: RouterTestingHarness;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [Navigator],
            providers: [
                provideRouter([
                    { path: "", component: MockHome },
                    { path: "download", component: MockDownload },
                ]),
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(Navigator);
        component = fixture.componentInstance;
        fixture.detectChanges();
        harness = await RouterTestingHarness.create();
    });

    it("should be created", () => {
        expect(component).toBeTruthy();
    });

    it("should have the correct GitHub URL", () => {
        expect(component.githubUrl).toBe("https://github.com/ethanp55/forex-data-downloader");
    });

    describe("buttons", () => {
        it("should have the correct text", () => {
            const compiled = fixture.nativeElement;
            const buttons = compiled.querySelectorAll(".btn");

            expect(buttons.length).toBe(3);
            expect(buttons[0].textContent).toContain("Home");
            expect(buttons[1].textContent).toContain("Download Data");
            expect(buttons[2].textContent).toContain("GitHub Repo");
        });

        it("should have the correct links", () => {
            const compiled = fixture.nativeElement;
            const links = compiled.querySelectorAll("a");

            expect(links[0].getAttribute("routerlink")).toBe("");
            expect(links[1].getAttribute("routerlink")).toBe("/download");
            expect(links[2].href).toBe(component.githubUrl);
        });
    });

    it("home button should display home component", async () => {
        await harness.navigateByUrl("");
        expect(harness.routeNativeElement?.textContent).toContain("Home");
    });

    it("download button should display download component", async () => {
        await harness.navigateByUrl("download");
        expect(harness.routeNativeElement?.textContent).toContain("Download");
    });
});
