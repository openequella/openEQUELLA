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
import * as OEQ from "@openequella/rest-api-client";
import { absurd } from "fp-ts/function";

export const getAdvancedSearchesFromServerResult: OEQ.Common.BaseEntitySummary[] =
  [
    { name: "Advanced Search 1", uuid: "369c92fa-ae59-4845-957d-8fcaa22c15e3" },
    { name: "Advanced Search 2", uuid: "3e7e68dd-3aa0-4e05-a70c-d6a559c53aa3" },
    { name: "Advanced Search 3", uuid: "a07212ff-3af9-4d78-89d5-48c2d263a810" },
  ];

type TargetNodeEssentials = Pick<
  OEQ.WizardCommonTypes.TargetNode,
  "target" | "attribute"
>;

export const buildTargetNodes = (
  nodes: TargetNodeEssentials[],
): OEQ.WizardCommonTypes.TargetNode[] =>
  nodes.map(({ target, attribute }) => {
    const fullPath = `${target}${attribute}`;
    return {
      target,
      attribute,
      fullTarget: fullPath,
      xoqlPath: fullPath,
      freetextField: fullPath,
    };
  });

/**
 * The bare necessities to mock a WizardBasicControl.
 */
export interface BasicControlEssentials
  extends Pick<
    OEQ.WizardControl.WizardBasicControl,
    | "title"
    | "description"
    | "mandatory"
    | "options"
    | "controlType"
    | "defaultValues"
  > {
  schemaNodes: TargetNodeEssentials[];
}

const mockBasicControl = (
  mockDetails: BasicControlEssentials,
): OEQ.WizardControl.WizardBasicControl => ({
  description: mockDetails.description,
  include: true,
  mandatory: mockDetails.mandatory,
  options: mockDetails.options,
  defaultValues: mockDetails.defaultValues,
  reload: false,
  size1: 0,
  size2: 1,
  targetNodes: buildTargetNodes(mockDetails.schemaNodes),
  title: mockDetails.title,
  visibilityScript: "return true;",
  controlType: mockDetails.controlType,
});

const mockOptionTypeControl = (
  mockDetails: BasicControlEssentials,
): OEQ.WizardControl.WizardBasicControl => ({
  ...mockBasicControl(mockDetails),
  size1: 3,
});

const mockEditbox = (
  mockDetails: BasicControlEssentials,
): OEQ.WizardControl.WizardEditBoxControl => ({
  ...mockBasicControl(mockDetails),
  controlType: "editbox",
  isAllowLinks: false,
  isAllowMultiLang: false,
  isCheckDuplication: false,
  isForceUnique: false,
  isNumber: false,
});

const mockCalendar = (
  mockDetails: BasicControlEssentials,
): OEQ.WizardControl.WizardCalendarControl => ({
  ...mockBasicControl(mockDetails),
  isRange: true,
  dateFormat: "DMY",
  controlType: "calendar",
});

export const mockRawHtmlContent = `<div><p>label for raw HTML control</p><hr></div>`;

export const mockWizardControlFactory = (
  mockDetails: BasicControlEssentials,
): OEQ.WizardControl.WizardBasicControl => {
  const { controlType } = mockDetails;
  switch (controlType) {
    case "editbox":
      return mockEditbox(mockDetails);
    case "checkboxgroup":
      return mockOptionTypeControl(mockDetails);
    case "radiogroup":
      return mockOptionTypeControl(mockDetails);
    case "html":
      return mockBasicControl(mockDetails);
    case "listbox":
      return mockOptionTypeControl(mockDetails);
    case "calendar":
      return mockCalendar(mockDetails);
    case "shufflebox":
      return mockBasicControl(mockDetails);
    case "shufflelist":
      return mockBasicControl(mockDetails);
    case "termselector":
    case "userselector":
      throw new Error(
        `Unsupported controlType [${controlType}] - please implement!`,
      );
    default:
      return absurd(controlType);
  }
};

export const getAdvancedSearchDefinition: OEQ.AdvancedSearch.AdvancedSearchDefinition =
  {
    name: "All Controls Power Search",
    collections: [
      {
        uuid: "0896be21-77d9-1279-90d9-1765e76e5f84",
        name: "Power Search Collection",
      },
    ],
    controls: [
      {
        mandatory: false,
        reload: false,
        include: true,
        size1: 0,
        size2: 0,
        title: "Calender 1",
        targetNodes: [
          {
            target: "/item/controls/calendar/nodefault",
            attribute: "",
            fullTarget: "/item/controls/calendar/nodefault",
            xoqlPath: "/item/controls/calendar/nodefault",
            freetextField: "/item/controls/calendar/nodefault",
          },
        ],
        options: [
          {
            value: "",
          },
        ],
        defaultValues: [],
        controlType: "calendar",
        isRange: true,
      },
      {
        mandatory: false,
        reload: false,
        include: true,
        size1: 1,
        size2: 0,
        title: "Check Box Group",
        targetNodes: [
          {
            target: "/item/controls/checkboxes",
            attribute: "",
            fullTarget: "/item/controls/checkboxes",
            xoqlPath: "/item/controls/checkboxes",
            freetextField: "/item/controls/checkboxes",
          },
        ],
        options: [
          {
            text: "1",
            value: "1",
          },
          {
            text: "2",
            value: "2",
          },
        ],
        defaultValues: [],
        controlType: "checkboxgroup",
      },
    ],
  };
