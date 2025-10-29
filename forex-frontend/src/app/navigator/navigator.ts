import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-navigator',
  imports: [RouterLink],
  templateUrl: './navigator.html',
  styleUrl: './navigator.css',
})
export class Navigator {
  readonly githubUrl = 'https://github.com/ethanp55/forex-data-downloader';
}
