import { Component } from '@angular/core';
import { Navigator } from './navigator/navigator';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [Navigator, RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {}
