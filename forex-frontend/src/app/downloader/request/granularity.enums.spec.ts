import { TestBed } from "@angular/core/testing";
import { getMinutes, Granularity } from "./granularity.enums";

describe("Granularity", () => {
    beforeEach(() => {
        TestBed.configureTestingModule({});
    });

    it("should serialize and deserialize each enum to JSON correctly", () => {
        const enumValues = Object.values(Granularity);

        enumValues.forEach((value) => {
            const jsonifiedEnum = JSON.stringify(value);
            const parsedValue = JSON.parse(jsonifiedEnum);

            expect(parsedValue).toBe(value);
        });
    });

    describe("getMinutes", () => {
        it("should throw an error for undefined granularity", () => {
            const invalidGranularity = "FOO" as Granularity;

            expect(() => getMinutes(invalidGranularity)).toThrowError("Unknown granularity");
        });
    });
});
