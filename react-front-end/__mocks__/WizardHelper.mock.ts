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
import type { FieldValueMap } from "../tsrc/components/wizard/WizardHelper";

export const controls: OEQ.WizardControl.WizardControl[] = [
  {
    mandatory: false,
    reload: true,
    include: true,
    size1: 2,
    size2: 0,
    title: "Do you want to show or hide?",
    targetNodes: [
      {
        target: "/item/name",
        attribute: "",
        fullTarget: "/item/name",
        xoqlPath: "/item/name",
        freetextField: "/item/name",
      },
    ],
    options: [
      {
        text: "Hide",
        value: "hide",
      },
      {
        text: "Show",
        value: "",
      },
    ],
    defaultValues: [],
    controlType: "radiogroup",
  },
  {
    mandatory: true,
    reload: false,
    include: true,
    size1: 0,
    size2: 1,
    customName: "Edit Box #1",
    title: "Edit Box #1",
    description: "The first edit box - target name",
    visibilityScript:
      "var bRet = false; \nif( xml.get('/item/name') != 'hide' ) \n{ \n    bRet = true; \n} \nreturn bRet; \n",
    targetNodes: [
      {
        target: "/item/name",
        attribute: "",
        fullTarget: "/item/name",
        xoqlPath: "/item/name",
        freetextField: "/item/name",
      },
    ],
    options: [],
    defaultValues: [],
    controlType: "editbox",
    isAllowLinks: false,
    isNumber: false,
    isAllowMultiLang: false,
    isForceUnique: false,
    isCheckDuplication: false,
  },
  {
    mandatory: false,
    reload: false,
    include: true,
    size1: 0,
    size2: 3,
    title: "Edit Box #2",
    description:
      "The second edit box, with three rows and targetting description",
    visibilityScript:
      "var bRet = false; \nif( xml.get('/item/name') != 'hide' && xml.get('/item/name') != 'hide2' ) \n{ \n    bRet = true; \n} \nreturn bRet; \n",
    targetNodes: [
      {
        target: "/item/description",
        attribute: "",
        fullTarget: "/item/description",
        xoqlPath: "/item/description",
        freetextField: "/item/description",
      },
    ],
    options: [],
    defaultValues: [],
    controlType: "editbox",
    isAllowLinks: false,
    isNumber: false,
    isAllowMultiLang: false,
    isForceUnique: false,
    isCheckDuplication: false,
  },
  {
    mandatory: true,
    reload: false,
    include: true,
    size1: 0,
    size2: 1,
    description: "This has no title - only a description",
    targetNodes: [
      {
        target: "/item/description",
        attribute: "",
        fullTarget: "/item/description",
        xoqlPath: "/item/description",
        freetextField: "/item/description",
      },
    ],
    options: [],
    defaultValues: [],
    controlType: "editbox",
    isAllowLinks: false,
    isNumber: false,
    isAllowMultiLang: false,
    isForceUnique: false,
    isCheckDuplication: false,
  },
  {
    mandatory: false,
    reload: false,
    include: true,
    size1: 0,
    size2: 0,
    title: "Sort Order Test",
    description: "These items should be devoid of any obvious order",
    targetNodes: [
      {
        target: "/item/name",
        attribute: "",
        fullTarget: "/item/name",
        xoqlPath: "/item/name",
        freetextField: "/item/name",
      },
    ],
    options: [
      {
        text: "Zebra",
        value: "z",
      },
      {
        text: "Ant",
        value: "a",
      },
      {
        text: "Cat",
        value: "c",
      },
      {
        text: "Bat",
        value: "b",
      },
    ],
    defaultValues: [],
    controlType: "shufflebox",
  },
  {
    mandatory: false,
    reload: false,
    include: true,
    size1: 0,
    size2: 0,
    title: "Keywords",
    description: "Searches against /item/keywords/keyword",
    targetNodes: [
      {
        target: "/item/keywords/keyword",
        attribute: "",
        fullTarget: "/item/keywords/keyword",
        xoqlPath: "/item/keywords/keyword",
        freetextField: "/item/keywords/keyword",
      },
    ],
    options: [],
    defaultValues: [],
    controlType: "shufflelist",
    isForceUnique: false,
    isCheckDuplication: false,
    isTokenise: true,
  },
  {
    mandatory: false,
    reload: false,
    include: true,
    size1: 2,
    size2: 0,
    title: "Check box group",
    description: "This is check box group, shows 2 items per row",
    targetNodes: [
      {
        target: "/item/description",
        attribute: "",
        fullTarget: "/item/description",
        xoqlPath: "/item/description",
        freetextField: "/item/description",
      },
    ],
    options: [
      {
        text: "Zebra",
        value: "z",
      },
      {
        text: "Cat",
        value: "c",
      },
      {
        text: "Mouse",
        value: "m",
      },
    ],
    defaultValues: [],
    controlType: "checkboxgroup",
  },
  {
    mandatory: false,
    reload: false,
    include: true,
    size1: 0,
    size2: 0,
    title: "List box",
    description: "This is list box targeting name",
    targetNodes: [
      {
        target: "/item/name",
        attribute: "",
        fullTarget: "/item/name",
        xoqlPath: "/item/name",
        freetextField: "/item/name",
      },
    ],
    options: [
      {
        text: "Zebra",
        value: "z",
      },
      {
        text: "Dog",
        value: "d",
      },
      {
        text: "Monkey",
        value: "m",
      },
    ],
    defaultValues: [],
    controlType: "listbox",
  },
  // User select allowing singular selection (isSelectMultiple: false)
  {
    mandatory: false,
    reload: false,
    include: true,
    size1: 0,
    size2: 0,
    title: "Select a user",
    description: "Which user would you like to search by - against /item/@user",
    targetNodes: [
      {
        target: "/item/",
        attribute: "user",
        fullTarget: "/item/@user",
        xoqlPath: "/item/@user",
        freetextField: "/item/@user",
      },
    ],
    options: [],
    defaultValues: [],
    controlType: "userselector",
    isSelectMultiple: false,
    isRestricted: false,
    restrictedTo: [],
  },
  // User select allowing singular selection (isSelectMultiple: false)
  {
    mandatory: false,
    reload: false,
    include: true,
    size1: 0,
    size2: 0,
    title: "Select multiple users",
    description: "This one is setup for multiple users (/item/users)",
    targetNodes: [
      {
        target: "/item/users",
        attribute: "",
        fullTarget: "/item/users",
        xoqlPath: "/item/users",
        freetextField: "/item/users",
      },
    ],
    options: [],
    defaultValues: [],
    controlType: "userselector",
    isSelectMultiple: true,
    isRestricted: false,
    restrictedTo: [],
  },
  {
    mandatory: false,
    reload: false,
    include: true,
    size1: 0,
    size2: 0,
    title: "Raw Html",
    description: "<h1>This is just a raw Html content</h1>",
    targetNodes: [],
    options: [],
    defaultValues: [],
    controlType: "html",
  },
  { controlType: "unknown" },
];

export const mockedFieldValueMap: FieldValueMap = new Map([
  [
    { schemaNode: ["/controls/calendar1"], type: "calendar" },
    ["2021-11-01", ""],
  ],
  [
    { schemaNode: ["/controls/calendar2"], type: "calendar" },
    ["", "2021-11-01"],
  ],
  [
    { schemaNode: ["/controls/calendar3"], type: "calendar" },
    ["2021-11-01", "2021-11-11"],
  ],
  [{ schemaNode: ["/controls/calendar4"], type: "calendar" }, ["", ""]],
  [
    { schemaNode: ["/controls/checkbox"], type: "checkboxgroup" },
    ["optionA", "optionB"],
  ],
  [
    {
      schemaNode: ["/controls/editbox"],
      type: "editbox",
      isValueTokenised: true,
    },
    ["hello world"],
  ],
  [{ schemaNode: ["/controls/html"], type: "html" }, [""]],
  [{ schemaNode: ["/controls/listbox"], type: "listbox" }, ["optionC"]],
  [{ schemaNode: ["/controls/radiogroup"], type: "radiogroup" }, ["optionD"]],
  [
    { schemaNode: ["/controls/shufflebox"], type: "shufflebox" },
    ["optionE", "optionF"],
  ],
  [
    {
      schemaNode: ["/controls/shufflelist"],
      type: "shufflelist",
      isValueTokenised: true,
    },
    ["The house is nice", "walking"],
  ],
  [
    { schemaNode: ["/controls/termselector"], type: "termselector" },
    ["programming"],
  ],
  [{ schemaNode: ["/controls/userselector"], type: "userselector" }, ["admin"]],
]);
