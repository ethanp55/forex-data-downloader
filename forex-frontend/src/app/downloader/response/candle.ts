/**
 * Class that holds candle data returned from the server.
 */
export class Candle {
    /**
     * Candle constructor.  We just use it to store data.
     *
     * @param complete Whether the candle is complete.  Should always be
     * true for historical data.
     * @param volume Candle volume.
     * @param time Time the candle started.
     * @param bid Bid pricing information.
     * @param mid Mid pricing information.
     * @param ask Ask pricing information.
     */
    constructor(
        public complete: boolean,
        public volume: number,
        public time: Date,
        public bid?: Price,
        public mid?: Price,
        public ask?: Price
    ) {}

    /**
     * Static helper method to parse JSON data into a Candle object.
     *
     * @param data JSON representing a Candle.
     *
     * @returns A Candle object.
     */
    static fromJSON(data: any): Candle {
        return new Candle(
            data.complete,
            data.volume,
            new Date(parseInt(data.time) * 1000), // Multiply by 1000 to get the number of milliseconds
            data.bid ? Price.fromJSON(data.bid) : undefined,
            data.mid ? Price.fromJSON(data.mid) : undefined,
            data.ask ? Price.fromJSON(data.ask) : undefined
        );
    }

    /**
     * Static helper method to convert an array of Candle objects into a string representing CSV data.
     *
     * @param candles Array of Candles.
     *
     * @returns A string representing the candles as CSV data.
     */
    static generateCSV(candles: Candle[]): string {
        const header =
            "Complete,Volume,Time,Bid_Open,Bid_High,Bid_Low,Bid_Close,Mid_Open,Mid_High,Mid_Low,Mid_Close,Ask_Open,Ask_High,Ask_Low,Ask_Close\n";
        // Use empty strings for missing price components
        const rows = candles
            .map(
                (candle) =>
                    `${candle.complete},${candle.volume},${candle.time.toISOString()},${
                        candle.bid?.o ?? ""
                    },${candle.bid?.h ?? ""},${candle.bid?.l ?? ""},${candle.bid?.c ?? ""},${
                        candle.mid?.o ?? ""
                    },${candle.mid?.h ?? ""},${candle.mid?.l ?? ""},${candle.mid?.c ?? ""},${
                        candle.ask?.o ?? ""
                    },${candle.ask?.h ?? ""},${candle.ask?.l ?? ""},${candle.ask?.c ?? ""}`
            )
            .join("\n");

        return header + rows;
    }
}

/**
 * Class used within the Candle class.  A Candle object can have information about the bid price, mid
 * price, and/or ask price, depending on what the user wants when they download candle data.  Each
 * price has open, high, low, and close values (the values that occurred during the life of the candle).
 */
export class Price {
    /**
     * Constructor used to store data.
     *
     * @param o Open price.
     * @param h High price.
     * @param l Low price.
     * @param c Close price.
     */
    constructor(public o: number, public h: number, public l: number, public c: number) {}

    /**
     * Static helper method for parsing JSON data into a Price object.
     *
     * @param data JSON representing a Price.
     *
     * @returns A Price object.
     */
    static fromJSON(data: any): Price {
        return new Price(
            parseFloat(data.o),
            parseFloat(data.h),
            parseFloat(data.l),
            parseFloat(data.c)
        );
    }
}
