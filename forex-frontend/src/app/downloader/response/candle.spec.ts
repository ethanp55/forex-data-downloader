import { TestBed } from "@angular/core/testing";
import { Candle, Price } from "./candle";

describe("Candle and Price classes", () => {
    beforeEach(() => {
        TestBed.configureTestingModule({});
    });

    describe("Price", () => {
        it("should create Price from JSON", () => {
            const jsonString = '{"o": "1.1", "h": "1.2", "l": "1.0", "c": "1.15"}';
            const jsonData = JSON.parse(jsonString);
            const price = Price.fromJSON(jsonData);

            expect(price).toBeDefined();
            expect(price.o).toBe(1.1);
            expect(price.h).toBe(1.2);
            expect(price.l).toBe(1.0);
            expect(price.c).toBe(1.15);
        });
    });

    describe("Candle", () => {
        it("should create Candle from JSON", () => {
            const jsonString =
                '{"complete": true, "volume": 100, "time": "1631136000", "bid": { "o": "1.1", "h": "1.2", "l": "1.0", "c": "1.15" }, "mid": { "o": "1.05", "h": "1.1", "l": "1.0", "c": "1.05" }, "ask": { "o": "1.2", "h": "1.25", "l": "1.15", "c": "1.2" }}';
            const jsonData = JSON.parse(jsonString);
            const candle = Candle.fromJSON(jsonData);

            expect(candle).toBeDefined();
            expect(candle.complete).toBe(true);
            expect(candle.volume).toBe(100);
            expect(candle.time).toEqual(new Date(1631136000 * 1000));
            expect(candle.bid).toBeDefined();
            expect(candle.bid?.o).toBe(1.1);
            expect(candle.mid).toBeDefined();
            expect(candle.mid?.o).toBe(1.05);
            expect(candle.ask).toBeDefined();
            expect(candle.ask?.o).toBe(1.2);
        });

        it("should generate CSV correctly", () => {
            const candles = [
                new Candle(
                    true,
                    100,
                    new Date("2025-09-07T00:00:00Z"),
                    new Price(1.1, 1.2, 1.0, 1.15),
                    new Price(1.05, 1.1, 1.0, 1.05),
                    new Price(1.2, 1.25, 1.15, 1.2)
                ),
                // Leave out the mid price to make sure empty strings are inserted in the CSV
                new Candle(
                    false,
                    50,
                    new Date("2025-09-08T00:00:00Z"),
                    new Price(1.2, 1.3, 1.1, 1.25),
                    undefined,
                    new Price(1.25, 1.3, 1.2, 1.3)
                ),
            ];
            const expectedHeader =
                "Complete,Volume,Time,Bid_Open,Bid_High,Bid_Low,Bid_Close,Mid_Open,Mid_High,Mid_Low,Mid_Close,Ask_Open,Ask_High,Ask_Low,Ask_Close\n";
            const expectedRows =
                "true,100,2025-09-07T00:00:00.000Z,1.1,1.2,1,1.15,1.05,1.1,1,1.05,1.2,1.25,1.15,1.2\n" +
                "false,50,2025-09-08T00:00:00.000Z,1.2,1.3,1.1,1.25,,,,,1.25,1.3,1.2,1.3";
            const csv = Candle.generateCSV(candles);

            expect(csv).toBe(expectedHeader + expectedRows);
        });
    });
});
