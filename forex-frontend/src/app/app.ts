import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  readonly githubUrl = "https://github.com/ethanp55/forex-data-downloader";
  readonly oandaUrl = "https://developer.oanda.com/rest-live-v20/instrument-ep/";
}
