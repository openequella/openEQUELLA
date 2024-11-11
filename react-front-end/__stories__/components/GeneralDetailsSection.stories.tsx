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
import * as S from "fp-ts/string";
import * as React from "react";
import type { Meta, StoryFn } from "@storybook/react";
import GeneralDetailsSection, {
  FieldRenderOptions,
  GeneralDetailsSectionProps,
  textFiledComponent,
} from "../../tsrc/components/GeneralDetailsSection";
import Switch from "@mui/material/Switch";

const defaultDetails: Record<string, FieldRenderOptions> = {
  firstname: {
    label: "firstname",
    required: true,
    desc: "This is a required input",
    component: textFiledComponent(
      "firstname",
      "James",
      false,
      true,
      () => {},
      false,
    ),
  },
  lastname: {
    label: "lastname",
    required: false,
    component: textFiledComponent(
      "lastname",
      "Bond",
      false,
      false,
      () => {},
      false,
    ),
  },
  email: {
    label: "Email",
    required: false,
    desc: "This is an input with validation: length > 5",
    validate: (value) => S.isString(value) && value.length > 5,
    component: textFiledComponent(
      "Email",
      "input",
      false,
      false,
      () => {},
      true,
      (value) => S.isString(value) && value.length > 5,
    ),
  },
  description: {
    label: "Description",
    required: false,
    desc: "This is a disabled input",
    component: textFiledComponent(
      "Description",
      "Some descriptions",
      true,
      false,
      () => {},
      false,
    ),
  },
  gender: {
    label: "Gender",
    required: true,
    desc: "This is a Switch",
    component: <Switch required checked onChange={(event) => {}} />,
  },
};

export default {
  title: "component/GeneralDetailsSection",
  component: GeneralDetailsSection,
  argTypes: {
    onChange: { action: "onChange" },
  },
} as Meta<GeneralDetailsSectionProps>;

export const Standard: StoryFn<GeneralDetailsSectionProps> = (args) => (
  <GeneralDetailsSection {...args} />
);
Standard.args = {
  title: "General Details",
  desc: "This is description",
  fields: defaultDetails,
};
