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
import { render } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import * as React from "react";
import {
  BASIC_ITEM,
  getCollectionMap,
} from "../../../../__mocks__/getCollectionsResp";
import * as CollectionsModule from "../../../../tsrc/modules/CollectionsModule";
import { CollectionSelector } from "../../../../tsrc/search/components/CollectionSelector";

jest
  .spyOn(CollectionsModule, "collectionListSummary")
  .mockResolvedValue(getCollectionMap);

const collectionNames = getCollectionMap.map((c) => c.name);

describe("<CollectionSelector />", () => {
  const getAvailableCollections = async () => {
    const { getByRole, findAllByRole } = render(
      <CollectionSelector onSelectionChange={jest.fn()} />,
    );
    const input = getByRole("combobox");
    await userEvent.click(input);

    return findAllByRole("option");
  };

  it("retrieves a list of Collections", async () => {
    const collections = await getAvailableCollections();
    expect(collections.map((s) => s.textContent)).toEqual(collectionNames);
  });

  it("uses pre-configured Collections", async () => {
    //eslint-disable-next-line @typescript-eslint/no-explicit-any
    (global as any).configuredCollections = [BASIC_ITEM.uuid];
    const collections = await getAvailableCollections();
    expect(collections.map((s) => s.textContent)).toEqual([BASIC_ITEM.name]);
  });
});
