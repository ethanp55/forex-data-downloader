/**
 * Enum that holds the different time frames/granularities users can choose from.
 */
export enum Granularity {
    M1 = "M1",
    M2 = "M2",
    M4 = "M4",
    M5 = "M5",
    M10 = "M10",
    M15 = "M15",
    M30 = "M30",
    H1 = "H1",
    H2 = "H2",
    H3 = "H3",
    H4 = "H4",
    H6 = "H6",
    H8 = "H8",
    H12 = "H12",
    D = "D",
    W = "W",
    Month = "M",
}

/**
 * Helper function for getting the number of minutes a time frame/granularity represents.
 *
 * @param granularity The time frame/granularity we want to calculate the number of minutes
 * for.
 *
 * @returns The number of minutes a time frame/granularity represents.  For example, H1 (an
 * hour) represents 60 minutes.
 */
export function getMinutes(granularity: Granularity): number {
    switch (granularity) {
        case Granularity.M1:
            return 1;
        case Granularity.M2:
            return 2;
        case Granularity.M4:
            return 4;
        case Granularity.M5:
            return 5;
        case Granularity.M10:
            return 10;
        case Granularity.M15:
            return 15;
        case Granularity.M30:
            return 30;
        case Granularity.H1:
            return 60;
        case Granularity.H2:
            return 60 * 2;
        case Granularity.H3:
            return 60 * 3;
        case Granularity.H4:
            return 60 * 4;
        case Granularity.H6:
            return 60 * 6;
        case Granularity.H8:
            return 60 * 8;
        case Granularity.H12:
            return 60 * 12;
        case Granularity.D:
            return 60 * 24;
        case Granularity.W:
            return 60 * 24 * 7;
        case Granularity.Month:
            return 60 * 24 * 30;
        default:
            throw new Error("Unknown granularity");
    }
}
