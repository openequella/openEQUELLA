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
    ["5 stars", 3.5, 3, 1, 1],
    ["10 stars", 6.5, 6, 1, 3],
  ])(
    "should display %s",
    (
      _,
      rating: number,
      fullStarNumber: number,
      halfStarNumber: number,
      emptyStarNumber: number
    ) => {
      const { queryAllByLabelText } = render(
        <StarRating
          numberOfStars={fullStarNumber + halfStarNumber + emptyStarNumber}
          rating={rating}
        />
      );
      expect(queryAllByLabelText("full-star")).toHaveLength(fullStarNumber);
      expect(queryAllByLabelText("empty-star")).toHaveLength(emptyStarNumber);
      expect(queryAllByLabelText("half-star")).toHaveLength(halfStarNumber);
    }
  );
});
