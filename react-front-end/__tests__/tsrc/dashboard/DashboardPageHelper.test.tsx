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
import * as ClosestEdge from "@atlaskit/pragmatic-drag-and-drop-hitbox/closest-edge";
import { Edge } from "@atlaskit/pragmatic-drag-and-drop-hitbox/closest-edge";
import * as OEQ from "@openequella/rest-api-client";
import * as E from "fp-ts/Either";
import { pipe } from "fp-ts/function";
import {
  basicPortlet,
  dashboardDetailsWithLayout,
  dashboardDetailsWithNoFirstColumnPortlets,
  minimisedPortlet,
  noEditPortlet,
  privatePortlet,
} from "../../../__mocks__/Dashboard.mock";
import { DndPortletData } from "../../../tsrc/dashboard/components/DraggablePortlet";
import {
  computeDndPortletNewPosition,
  getMovedPortlets,
  getOrderForRestoredPortlet,
  movePortlet,
  updatePortletPosition,
} from "../../../tsrc/dashboard/DashboardPageHelper";
import { DndColumnData } from "../../../tsrc/dashboard/portlet/PortletDropZoneGrid";
import { portletFilterByColumn } from "../../../tsrc/dashboard/portlet/PortletHelper";
import { PortletPosition } from "../../../tsrc/modules/DashboardModule";

const mockEdge = jest.spyOn(ClosestEdge, "extractClosestEdge");

describe("computeDndPortletNewPosition", () => {
  const topPortletDndData: DndPortletData = {
    portlet: basicPortlet,
    position: { column: 0, order: 0 },
  };

  const secondPortletDndData: DndPortletData = {
    portlet: privatePortlet,
    position: { column: 0, order: 1 },
  };

  const thirdPortletDndData: DndPortletData = {
    portlet: minimisedPortlet,
    position: { column: 0, order: 2 },
  };

  const anotherColumnPortletDndData: DndPortletData = {
    portlet: noEditPortlet,
    position: { column: 1, order: 0 },
  };

  const targetColumn0: DndColumnData = { column: 0, count: 3 };
  const targetColumn1: DndColumnData = { column: 1, count: 1 };

  it.each<
    [
      string,
      DndPortletData,
      DndPortletData | DndColumnData,
      Edge,
      PortletPosition,
    ]
  >([
    [
      "same column: move top one below third",
      topPortletDndData,
      thirdPortletDndData,
      "bottom",
      { column: 0, order: 2 },
    ],
    [
      "same column: move top one above third",
      topPortletDndData,
      thirdPortletDndData,
      "top",
      { column: 0, order: 1 },
    ],
    [
      "same column: moves third one below top",
      thirdPortletDndData,
      topPortletDndData,
      "bottom",
      { column: 0, order: 1 },
    ],
    [
      "same column: moves third one above top",
      thirdPortletDndData,
      topPortletDndData,
      "top",
      { column: 0, order: 0 },
    ],
    [
      "different column: move portlet above another column's portlet",
      secondPortletDndData,
      anotherColumnPortletDndData,
      "top",
      { column: 1, order: 0 },
    ],
    [
      "different column: move portlet below another column's portlet",
      secondPortletDndData,
      anotherColumnPortletDndData,
      "bottom",
      { column: 1, order: 1 },
    ],
    [
      "moves a portlet to same column",
      secondPortletDndData,
      targetColumn0,
      "top",
      { column: 0, order: 2 },
    ],
    [
      "moves a portlet to another column",
      secondPortletDndData,
      targetColumn1,
      "top",
      { column: 1, order: 1 },
    ],
  ])(
    "should compute new position when: %s",
    (_, sourceDndData, targetDndData, edge, newPosition) => {
      mockEdge.mockReturnValueOnce(edge);
      const result = computeDndPortletNewPosition({
        sourceDndData,
        targetDndData,
      });
      expect(result).toEqual(E.right(newPosition));
    },
  );
});

describe("movePortlet", () => {
  // Represents the portlets position.
  interface From {
    // Indicates the column of the portlet.
    column: "left" | "right";
    // Indicates the index of the portlet in the column.
    index: number;
  }

  // Helper to get portlet by column and index.
  const getPortlet = (
    { column, index }: From,
    { portlets, layout }: OEQ.Dashboard.DashboardDetails,
  ): OEQ.Dashboard.BasicPortlet => {
    const columnIndex = column === "left" ? 0 : 1;

    const portletsInColumn =
      layout === "SingleColumn"
        ? [
            ...portletFilterByColumn(0)(portlets),
            ...portletFilterByColumn(1)(portlets),
          ]
        : portletFilterByColumn(columnIndex)(portlets);

    return portletsInColumn[index];
  };

  const singleColumnResultTopBelowThird = [
    updatePortletPosition({ column: 0, order: 0 }, privatePortlet),
    updatePortletPosition({ column: 0, order: 1 }, minimisedPortlet),
    updatePortletPosition({ column: 0, order: 2 }, basicPortlet),
    updatePortletPosition({ column: 0, order: 3 }, noEditPortlet),
  ];

  const singleColumnResultTopToBottom = [
    updatePortletPosition({ column: 0, order: 0 }, privatePortlet),
    updatePortletPosition({ column: 0, order: 1 }, minimisedPortlet),
    updatePortletPosition({ column: 0, order: 2 }, noEditPortlet),
    updatePortletPosition({ column: 0, order: 3 }, basicPortlet),
  ];

  const singleColumnResultBottomAboveThird = [
    updatePortletPosition({ column: 0, order: 0 }, basicPortlet),
    updatePortletPosition({ column: 0, order: 1 }, privatePortlet),
    updatePortletPosition({ column: 0, order: 2 }, noEditPortlet),
    updatePortletPosition({ column: 0, order: 3 }, minimisedPortlet),
  ];

  const singleColumnResultBottomToTop = [
    updatePortletPosition({ column: 0, order: 0 }, noEditPortlet),
    updatePortletPosition({ column: 0, order: 1 }, basicPortlet),
    updatePortletPosition({ column: 0, order: 2 }, privatePortlet),
    updatePortletPosition({ column: 0, order: 3 }, minimisedPortlet),
  ];

  const leftTopToBelowSecond = [
    updatePortletPosition({ column: 0, order: 0 }, privatePortlet),
    updatePortletPosition({ column: 0, order: 1 }, basicPortlet),
    updatePortletPosition({ column: 0, order: 2 }, minimisedPortlet),
    updatePortletPosition({ column: 1, order: 0 }, noEditPortlet),
  ];

  const leftTopToBottom = [
    updatePortletPosition({ column: 0, order: 0 }, privatePortlet),
    updatePortletPosition({ column: 0, order: 1 }, minimisedPortlet),
    updatePortletPosition({ column: 0, order: 2 }, basicPortlet),
    updatePortletPosition({ column: 1, order: 0 }, noEditPortlet),
  ];

  const leftBottomAboveSecond = [
    updatePortletPosition({ column: 0, order: 0 }, basicPortlet),
    updatePortletPosition({ column: 0, order: 1 }, minimisedPortlet),
    updatePortletPosition({ column: 0, order: 2 }, privatePortlet),
    updatePortletPosition({ column: 1, order: 0 }, noEditPortlet),
  ];

  const leftBottomToTop = [
    updatePortletPosition({ column: 0, order: 0 }, minimisedPortlet),
    updatePortletPosition({ column: 0, order: 1 }, basicPortlet),
    updatePortletPosition({ column: 0, order: 2 }, privatePortlet),
    updatePortletPosition({ column: 1, order: 0 }, noEditPortlet),
  ];

  const rightToLeftColumnTopResult = [
    updatePortletPosition({ column: 0, order: 0 }, noEditPortlet),
    updatePortletPosition({ column: 0, order: 1 }, basicPortlet),
    updatePortletPosition({ column: 0, order: 2 }, privatePortlet),
    updatePortletPosition({ column: 0, order: 3 }, minimisedPortlet),
  ];

  const rightToLeftColumnMiddleResult = [
    updatePortletPosition({ column: 0, order: 0 }, basicPortlet),
    updatePortletPosition({ column: 0, order: 1 }, noEditPortlet),
    updatePortletPosition({ column: 0, order: 2 }, privatePortlet),
    updatePortletPosition({ column: 0, order: 3 }, minimisedPortlet),
  ];

  const rightToLeftColumnBottomResult = [
    updatePortletPosition({ column: 0, order: 0 }, basicPortlet),
    updatePortletPosition({ column: 0, order: 1 }, privatePortlet),
    updatePortletPosition({ column: 0, order: 2 }, minimisedPortlet),
    updatePortletPosition({ column: 0, order: 3 }, noEditPortlet),
  ];

  // Note the in the left column all remaining portlets' position are not updated.
  const leftTopToRightTopResult = [
    updatePortletPosition({ column: 0, order: 1 }, privatePortlet),
    updatePortletPosition({ column: 0, order: 2 }, minimisedPortlet),
    updatePortletPosition({ column: 1, order: 0 }, basicPortlet),
    updatePortletPosition({ column: 1, order: 1 }, noEditPortlet),
  ];

  const leftTopToRightBottomResult = [
    updatePortletPosition({ column: 0, order: 1 }, privatePortlet),
    updatePortletPosition({ column: 0, order: 2 }, minimisedPortlet),
    updatePortletPosition({ column: 1, order: 0 }, noEditPortlet),
    updatePortletPosition({ column: 1, order: 1 }, basicPortlet),
  ];

  const leftMiddleToRightTopResult = [
    updatePortletPosition({ column: 0, order: 0 }, basicPortlet),
    updatePortletPosition({ column: 0, order: 2 }, minimisedPortlet),
    updatePortletPosition({ column: 1, order: 0 }, privatePortlet),
    updatePortletPosition({ column: 1, order: 1 }, noEditPortlet),
  ];

  const leftMiddleToRightBottomResult = [
    updatePortletPosition({ column: 0, order: 0 }, basicPortlet),
    updatePortletPosition({ column: 0, order: 2 }, minimisedPortlet),
    updatePortletPosition({ column: 1, order: 0 }, noEditPortlet),
    updatePortletPosition({ column: 1, order: 1 }, privatePortlet),
  ];

  const leftBottomToRightTopResult = [
    updatePortletPosition({ column: 0, order: 0 }, basicPortlet),
    updatePortletPosition({ column: 0, order: 1 }, privatePortlet),
    updatePortletPosition({ column: 1, order: 0 }, minimisedPortlet),
    updatePortletPosition({ column: 1, order: 1 }, noEditPortlet),
  ];

  const leftBottomToRightBottomResult = [
    updatePortletPosition({ column: 0, order: 0 }, basicPortlet),
    updatePortletPosition({ column: 0, order: 1 }, privatePortlet),
    updatePortletPosition({ column: 1, order: 0 }, noEditPortlet),
    updatePortletPosition({ column: 1, order: 1 }, minimisedPortlet),
  ];

  const expectMovePortletChanges = (
    layout: OEQ.Dashboard.DashboardLayout,
    from: From,
    newPos: PortletPosition,
    expected: OEQ.Dashboard.BasicPortlet[],
  ) => {
    const dashboardDetails = dashboardDetailsWithLayout(layout);
    const testPortlet = getPortlet(from, dashboardDetails);
    const moveResult = pipe(
      dashboardDetails,
      movePortlet(testPortlet, newPos),
      E.getOrElseW(() => [] as OEQ.Dashboard.BasicPortlet[]),
    );

    expect(moveResult).toHaveLength(expected.length);
    expect(moveResult).toEqual(expect.arrayContaining(expected));
  };

  it.each<[string, From, PortletPosition, OEQ.Dashboard.BasicPortlet[]]>([
    [
      "move top one below third",
      { column: "left", index: 0 },
      { column: 0, order: 2 },
      singleColumnResultTopBelowThird,
    ],
    [
      "move top one to bottom",
      { column: "left", index: 0 },
      { column: 0, order: 3 },
      singleColumnResultTopToBottom,
    ],
    [
      "move bottom one above third",
      { column: "left", index: 3 },
      { column: 0, order: 2 },
      singleColumnResultBottomAboveThird,
    ],
    [
      "move bottom one to top",
      { column: "left", index: 3 },
      { column: 0, order: 0 },
      singleColumnResultBottomToTop,
    ],
  ])(
    "SingleColumn: should update portlets position when: %s",
    (_, fromPortlet, newPosition, expectedPortlets) =>
      expectMovePortletChanges(
        "SingleColumn",
        fromPortlet,
        newPosition,
        expectedPortlets,
      ),
  );

  it.each<[string, From, PortletPosition, OEQ.Dashboard.BasicPortlet[]]>([
    [
      "move left top one below second",
      { column: "left", index: 0 },
      { column: 0, order: 1 },
      leftTopToBelowSecond,
    ],
    [
      "move left top one to bottom",
      { column: "left", index: 0 },
      { column: 0, order: 2 },
      leftTopToBottom,
    ],
    [
      "move left bottom one above second",
      { column: "left", index: 2 },
      { column: 0, order: 1 },
      leftBottomAboveSecond,
    ],
    [
      "move left bottom one to top",
      { column: "left", index: 2 },
      { column: 0, order: 0 },
      leftBottomToTop,
    ],
    [
      "move right column one to left column top",
      { column: "right", index: 0 },
      { column: 0, order: 0 },
      rightToLeftColumnTopResult,
    ],
    [
      "move right column one to left column middle(below top one)",
      { column: "right", index: 0 },
      { column: 0, order: 1 },
      rightToLeftColumnMiddleResult,
    ],
    [
      "move right column one to left column bottom",
      { column: "right", index: 0 },
      { column: 0, order: 3 },
      rightToLeftColumnBottomResult,
    ],
    [
      "move left column top one to right column top",
      { column: "left", index: 0 },
      { column: 1, order: 0 },
      leftTopToRightTopResult,
    ],
    [
      "move left column top one to right column bottom",
      { column: "left", index: 0 },
      { column: 1, order: 1 },
      leftTopToRightBottomResult,
    ],
    [
      "move left column middle one to right column top",
      { column: "left", index: 1 },
      { column: 1, order: 0 },
      leftMiddleToRightTopResult,
    ],
    [
      "move left column middle one to right column bottom",
      { column: "left", index: 1 },
      { column: 1, order: 1 },
      leftMiddleToRightBottomResult,
    ],
    [
      "move left column bottom one to right column top",
      { column: "left", index: 2 },
      { column: 1, order: 0 },
      leftBottomToRightTopResult,
    ],
    [
      "move right column bottom one to right column bottom",
      { column: "left", index: 2 },
      { column: 1, order: 1 },
      leftBottomToRightBottomResult,
    ],
  ])(
    "TwoEqualColumns: should update portlets position when: %s",
    (_, fromPortlet, newPosition, expectedPortlets) =>
      expectMovePortletChanges(
        "TwoEqualColumns",
        fromPortlet,
        newPosition,
        expectedPortlets,
      ),
  );
});

describe("getMovedPortlets", () => {
  it("should only return moved portlets", () => {
    const portlets = dashboardDetailsWithLayout().portlets;

    // Move to different column.
    const movedPortlet1 = updatePortletPosition(
      { column: 1, order: 1 },
      portlets[1],
    );
    // Move to different order.
    const movedPortlet2 = updatePortletPosition(
      { column: 0, order: 0 },
      portlets[3],
    );

    const newPortlets: OEQ.Dashboard.BasicPortlet[] = [
      portlets[0],
      movedPortlet1,
      portlets[2],
      movedPortlet2,
    ];

    const expectedResult: OEQ.Dashboard.BasicPortlet[] = [
      movedPortlet1,
      movedPortlet2,
    ];

    const result = getMovedPortlets(portlets, newPortlets);

    expect(result).toHaveLength(expectedResult.length);
    expect(result).toEqual(expect.arrayContaining(expectedResult));
  });
});

describe("getOrderForRestoredPortlet", () => {
  it.each([
    ["dashboardDetails", undefined],
    ["first-column portlets list", dashboardDetailsWithNoFirstColumnPortlets],
  ])("should return 0 if %s is empty", (_, dashboardDetails) => {
    expect(getOrderForRestoredPortlet(dashboardDetails)).toBe(0);
  });

  it("should return the correct order for a restored portlet when the first column is not empty", () => {
    const result = pipe(
      dashboardDetailsWithLayout(),
      getOrderForRestoredPortlet,
    );
    expect(result).toBe(3);
  });
});
