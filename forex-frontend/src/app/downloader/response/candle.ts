export class Candle {
    constructor(
        public complete: boolean,
        public volume: number,
        public time: Date,
        public bid?: Price,
        public mid?: Price,
        public ask?: Price,
    ) {}

    static fromJSON(data: any): Candle {
        return new Candle(
            data.complete,
            data.volume,
            new Date(parseInt(data.time) * 1000),
            data.bid ? Price.fromJSON(data.bid) : undefined,
            data.mid ? Price.fromJSON(data.mid) : undefined,
            data.ask ? Price.fromJSON(data.ask) : undefined,
        );
    }
}

export class Price {
    constructor(
        public o: number,
        public h: number,
        public l: number,
        public c: number,
    ) {}

    static fromJSON(data: any): Price {
        return new Price(
            parseFloat(data.o),
            parseFloat(data.h),
            parseFloat(data.l),
            parseFloat(data.c),
        );
    }
}
