import { TestBed } from "@angular/core/testing";
import { CurrencyPair } from "./currency-pair.enums";

describe("CurrencyPair", () => {
    beforeEach(() => {
        TestBed.configureTestingModule({});
    });

    it("should serialize and deserialize each enum to JSON correctly", () => {
        const enumValues = Object.values(CurrencyPair);

        enumValues.forEach((value) => {
            const jsonifiedEnum = JSON.stringify(value);
            const parsedValue = JSON.parse(jsonifiedEnum);

            expect(parsedValue).toBe(value);
        });
    });
});
