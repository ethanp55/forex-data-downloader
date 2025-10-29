import { Routes } from "@angular/router";
import { Downloader } from "./downloader/downloader";
import { Home } from "./home/home";

export const routes: Routes = [
    {
        path: "",
        component: Home,
    },
    {
        path: "download",
        component: Downloader,
    },
];
