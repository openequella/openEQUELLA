import * as OEQ from '../src';
import {is} from "typescript-is";

describe("Convert date fields", () => {
  interface StringDate {
    dates: {
      yesterday: string;
      today: string;
    };
  }
  interface StandardDate {
    dates: {
      yesterday: Date;
      today: Date;
    };
  }
  interface InvalidDate {
    dates: {
      yesterday: undefined;
      today: undefined;
    };
  }

  it("should convert valid date strings to objects of Date", () => {
    const validDates: StringDate = {
      dates: {
        yesterday: "2020-06-11T11:45:24.296+10:00",
        today: "2020-06-12T11:45:24.296+10:00",
      }
    };

    const dates = OEQ.Utils.convertDateFields<StandardDate>(validDates, ["yesterday", "today"] );
    expect(is<StandardDate>(dates)).toBe(true);
  });

  it("should return undefined for fields that have invalid date strings", () => {
    const invalidDates: StringDate = {
      dates: {
        yesterday: "hello",
        today: "world",
      }
    };
    const dates = OEQ.Utils.convertDateFields<StandardDate>(invalidDates, ["yesterday", "today"] );
    expect(is<InvalidDate>(dates)).toBe(true);
  })
});
