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
import { Meta, StoryFn } from "@storybook/react";
import { pipe } from "fp-ts/function";
import * as NEA from "fp-ts/NonEmptyArray";
import * as React from "react";
import { ShuffleBox, ShuffleBoxProps } from "../../tsrc/components/ShuffleBox";

export default {
  title: "Component/ShuffleBox",
  component: ShuffleBox,
  argTypes: {
    onSelect: {
      action: "onSelect called",
    },
  },
} as Meta<ShuffleBoxProps>;

export const Basic: StoryFn<ShuffleBoxProps> = (args) => (
  <ShuffleBox {...args} />
);
Basic.args = {
  id: "shufflebox-story",
  options: new Map<string, string>([
    ["option1", "Option One"],
    ["option2", "Option Two"],
    ["option3", "Option Three"],
  ]),
  values: new Set<string>(),
};

export const WithSelection: StoryFn<ShuffleBoxProps> = (args) => (
  <ShuffleBox {...args} />
);
WithSelection.args = {
  ...Basic.args,
  values: new Set<string>(["option2"]),
};

export const LotsOfOptions: StoryFn<ShuffleBoxProps> = (args) => (
  <ShuffleBox {...args} />
);
LotsOfOptions.args = {
  ...Basic.args,
  options: pipe(
    NEA.range(100, 200),
    NEA.map((n) => `Option ${n}`),
    NEA.reduce(new Map<string, string>(), (m, label) =>
      m.set(label.toLowerCase().replace(" ", ""), label),
    ),
  ),
};
