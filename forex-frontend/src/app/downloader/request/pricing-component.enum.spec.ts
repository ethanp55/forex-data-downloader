import { TestBed } from "@angular/core/testing";
import { PricingComponent } from "./pricing-component.enums";

describe("PricingComponent", () => {
    beforeEach(() => {
        TestBed.configureTestingModule({});
    });

    it("should serialize and deserialize each enum to JSON correctly", () => {
        const enumValues = Object.values(PricingComponent);

        enumValues.forEach((value) => {
            const jsonifiedEnum = JSON.stringify(value);
            const parsedValue = JSON.parse(jsonifiedEnum);

            expect(parsedValue).toBe(value);
        });
    });
});
