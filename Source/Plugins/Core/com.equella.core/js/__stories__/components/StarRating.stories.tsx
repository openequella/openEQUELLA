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
import { Meta, Story } from "@storybook/react";
import * as React from "react";
import { StarRating, StarRatingProps } from "../../tsrc/components/StarRating";

export default {
  title: "component/StarRating",
  component: StarRating,
} as Meta<StarRatingProps>;

export const HalfStar: Story<StarRatingProps> = (args) => (
  <StarRating {...args} />
);
HalfStar.args = {
  rating: 0.5,
  numberOfStars: 1,
};

export const FullStar: Story<StarRatingProps> = (args) => (
  <StarRating {...args} />
);
FullStar.args = {
  ...HalfStar.args,
  rating: 5,
};

export const EmptyStar: Story<StarRatingProps> = (args) => (
  <StarRating {...args} />
);
EmptyStar.args = {
  ...HalfStar.args,
  rating: 0,
};

export const MixedStars: Story<StarRatingProps> = (args) => (
  <StarRating {...args} />
);
MixedStars.args = {
  rating: 2.5,
  numberOfStars: 5,
};
