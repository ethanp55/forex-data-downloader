import { Component } from "@angular/core";

@Component({
    selector: "app-home",
    imports: [],
    templateUrl: "./home.html",
    styleUrl: "./home.css",
})
/**
 * Home component that shows the main/home page of the website.
 */
export class Home {
    readonly oandaUrl = "https://developer.oanda.com/rest-live-v20/instrument-ep/";
}
