import { Component } from '@angular/core';

@Component({
  selector: 'app-home',
  imports: [],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {
  readonly oandaUrl = "https://developer.oanda.com/rest-live-v20/instrument-ep/";
}
