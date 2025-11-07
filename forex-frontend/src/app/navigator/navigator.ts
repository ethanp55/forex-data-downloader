import { Component } from "@angular/core";
import { RouterLink } from "@angular/router";

@Component({
    selector: "app-navigator",
    imports: [RouterLink],
    templateUrl: "./navigator.html",
    styleUrl: "./navigator.css",
})
/**
 * Navigator component that displays in the top bar of the website.  Shows a few different
 * navigation buttons for the user.
 */
export class Navigator {
    readonly githubUrl = "https://github.com/ethanp55/forex-data-downloader";
}
