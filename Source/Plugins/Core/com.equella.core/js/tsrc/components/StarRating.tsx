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
import Star from "@material-ui/icons/Star";
import StarBorder from "@material-ui/icons/StarBorder";
import StarHalf from "@material-ui/icons/StarHalf";
import * as React from "react";
import { range } from "lodash";

export interface StarRatingProps {
  /**
   * The number of stars to display.
   */
  numberOfStars: number;
  /**
   * A numeric rating.
   */
  rating: number;
}

/**
 * Generate star icons for Item's ratings.
 * Each rating will be rounded to its nearest 0.5.
 * For example, 3.33 will be 3.5 and 3.23 will be 3.
 */
export const StarRating = ({ numberOfStars, rating }: StarRatingProps) => {
  const roundedRating = Math.round(rating * 2) / 2;
  return (
    <>
      {
        // As the end is not included in the range, ned to plus the end with 1.
        range(1, numberOfStars + 1).map((i) => {
          if (i <= roundedRating) {
            return <Star aria-label="full-star" key={i} />;
          }

          if (i - 0.5 === roundedRating) {
            return <StarHalf aria-label="half-star" key={i} />;
          }

          return <StarBorder aria-label="empty-star" key={i} />;
        })
      }
    </>
  );
};
