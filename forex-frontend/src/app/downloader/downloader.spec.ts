import { ComponentFixture, TestBed } from "@angular/core/testing";

import { Downloader } from "./downloader";

describe("Downloader", () => {
    let component: Downloader;
    let fixture: ComponentFixture<Downloader>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({ imports: [Downloader] }).compileComponents();

        fixture = TestBed.createComponent(Downloader);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
