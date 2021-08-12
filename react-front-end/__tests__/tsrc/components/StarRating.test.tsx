/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import "@testing-library/jest-dom/extend-expect";
import { render } from "@testing-library/react";
import * as React from "react";
import { StarRating } from "../../../tsrc/components/StarRating";

describe("<StarRating />", () => {
  it.each([
    ["zero", 0],
    ["half max", 2.5],
    ["max", 5],
    ["max plus 1", 6],
  ])("supports ratings of %s", (_, rating: number) => {
    const { container } = render(
      <StarRating numberOfStars={5} rating={rating} />
    );
    const starIcons = container.querySelectorAll("svg");
    // Regardless of the rating, 5 stars should be always displayed.
    expect(starIcons).toHaveLength(5);
  });

  it.each([0, 5, 10])(
    "displays a specified number of stars: %d",
    (numberOfStars: number) => {
      const { container } = render(
        <StarRating numberOfStars={numberOfStars} rating={1} />
      );
      const starIcons = container.querySelectorAll("svg");
      // Regardless of the rating, the maximum number of stars should be displayed.
      expect(starIcons).toHaveLength(numberOfStars);
    }
  );

  it.each([
    [3.22, 3, 3, 0],
    [3.33, 3.5, 3, 1],
    [3.67, 3.5, 3, 1],
    [3.77, 4, 4, 0],
  ])(
    "rounds %f to %f",
    (
      rating: number,
      roundRating: number,
      fullStarNumber: number,
      halfStarNumber
    ) => {
      const { queryAllByLabelText } = render(
        <StarRating numberOfStars={5} rating={rating} />
      );
      expect(queryAllByLabelText("full-star")).toHaveLength(fullStarNumber);
      expect(queryAllByLabelText("half-star")).toHaveLength(halfStarNumber);
    }
  );
});
