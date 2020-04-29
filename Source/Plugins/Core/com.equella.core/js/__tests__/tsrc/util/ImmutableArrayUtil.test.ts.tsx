import {
  addElement,
  deleteElement,
  replaceElement,
} from "../../../tsrc/util/ImmutableArrayUtil";
import { MimeTypeFilter } from "../../../tsrc/settings/Search/searchfilter/SearchFilterSettingsModule";

describe("ImmutableArrayUtil", () => {
  describe("create new arrays of primitives", () => {
    const testingArray = [1, 2, 3];
    let newArray: number[] = [];
    const comparator = (figure: number) => figure >= 4;

    it("should add a new element", () => {
      newArray = addElement(testingArray, 4);
      expect(newArray.toString()).toBe("1,2,3,4");
    });

    it("should replace an existing element with a new one", () => {
      newArray = replaceElement(testingArray, comparator, 5);
      expect(newArray.toString()).toBe("1,2,3,5");
    });

    it("should delete one existing element", () => {
      newArray = deleteElement(testingArray, comparator, 1);
      expect(newArray.toString()).toBe("1,2,3");
    });
  });

  describe("create new arrays of objects", () => {
    const testingArray: MimeTypeFilter[] = [
      { id: "1", name: "PNG Image filter", mimeTypes: ["image/png"] },
    ];
    let newArray: MimeTypeFilter[] = [];
    const comparator = (filter: MimeTypeFilter) =>
      filter.name === "PNG Image filter";
    const videoFilter: MimeTypeFilter = {
      id: "2",
      name: "Video filter",
      mimeTypes: ["video/mp4"],
    };

    it("should add a new filter", () => {
      newArray = addElement(testingArray, videoFilter);
      expect(newArray).toHaveLength(2);
    });

    it("should replace the PNG Image filter with a different filter", () => {
      const gifFilter = {
        id: "3",
        name: "GIF Image filter",
        mimeTypes: ["image/gif"],
      };
      newArray = replaceElement(testingArray, comparator, gifFilter);
      expect(newArray).toHaveLength(1);
      expect(
        newArray.some((filter) => filter.name === "GIF Image filter")
      ).toBeTruthy();
    });

    it("should delete the PNG Image filter", () => {
      newArray = deleteElement(testingArray, comparator, 1);
      expect(newArray).toHaveLength(0);
    });
  });
});
