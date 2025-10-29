export class Candle {
    constructor(
        public complete: boolean,
        public volume: number,
        public time: Date,
        public bid?: Price,
        public mid?: Price,
        public ask?: Price,
    ) {}
}

export class Price {
    constructor(
        public o: number,
        public h: number,
        public l: number,
        public c: number,
    ) {}
}
