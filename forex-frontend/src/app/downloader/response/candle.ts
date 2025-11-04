export class Candle {
    constructor(
        public complete: boolean,
        public volume: number,
        public time: Date,
        public bid?: Price,
        public mid?: Price,
        public ask?: Price
    ) {}

    static fromJSON(data: any): Candle {
        return new Candle(
            data.complete,
            data.volume,
            new Date(parseInt(data.time) * 1000),
            data.bid ? Price.fromJSON(data.bid) : undefined,
            data.mid ? Price.fromJSON(data.mid) : undefined,
            data.ask ? Price.fromJSON(data.ask) : undefined
        );
    }

    static generateCSV(candles: Candle[]): string {
        const header =
            "Complete,Volume,Time,Bid_Open,Bid_High,Bid_Low,Bid_Close,Mid_Open,Mid_High,Mid_Low,Mid_Close,Ask_Open,Ask_High,Ask_Low,Ask_Close\n";
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

export class Price {
    constructor(public o: number, public h: number, public l: number, public c: number) {}

    static fromJSON(data: any): Price {
        return new Price(
            parseFloat(data.o),
            parseFloat(data.h),
            parseFloat(data.l),
            parseFloat(data.c)
        );
    }
}
