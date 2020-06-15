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
import * as SchemaModule from "../../../tsrc/schema/SchemaModule";
import { getSchemasResp } from "../../../__mocks__/getSchemasResp";
import { getSchemaUuidResp } from "../../../__mocks__/getSchemaUuidResp";
import * as OEQ from "@openequella/rest-api-client";
import { jsxOutput } from "../../../__mocks__/schemaSelectorDataMock";

jest.mock("@openequella/rest-api-client");
(OEQ.Schema.listSchemas as jest.Mock<
  Promise<OEQ.Common.PagedResult<OEQ.Common.BaseEntity>>
>).mockResolvedValue(getSchemasResp);
(OEQ.Schema.getSchema as jest.Mock<
  Promise<OEQ.Schema.EquellaSchema>
>).mockResolvedValue(getSchemaUuidResp);

describe("SchemaModule", () => {
  it("should be able to provide a list of schemas", () =>
    SchemaModule.schemaListSummary().then((schemas) => {
      expect(schemas.size).toEqual(5);
    }));

  it("should be able to retrieve the schema for a specified UUID", () =>
    SchemaModule.schemaTree("unused due to mocked response").then((schema) => {
      const itemNode = schema.children![0];
      const copyrightNode = itemNode.children![1];
      expect(itemNode.name).toEqual("item");
      expect(itemNode.children?.length).toEqual(2);
      expect(copyrightNode.name).toEqual("copyright");
      expect(copyrightNode.children?.length).toEqual(14);
    }));
});

describe("pathForNode", () => {
  const schemaDefinition = {
    child1: { child2: { child3: { _type: "text" } } },
  };
  const testSchema = SchemaModule.buildSchemaTree(schemaDefinition, "xml");
  const testNode = testSchema.children![0].children![0].children![0];

  it("should correctly generate an oEQ path by default", () =>
    expect(SchemaModule.pathForNode(testNode)).toEqual(
      "/child1/child2/child3"
    ));

  it("should correctly include the XML prefix if desired", () =>
    expect(SchemaModule.pathForNode(testNode, false)).toEqual(
      "/xml/child1/child2/child3"
    ));
});

describe("getAllPaths", () => {
  const schemaDefinition = {
    child1: {
      _type: "text",
      child2: { _type: "text", child3: { _type: "text" } },
    },
  };
  const testSchema = SchemaModule.buildSchemaTree(schemaDefinition, "xml");
  it("should correctly generate the list of full paths", () => {
    const paths = SchemaModule.getAllPaths(testSchema, [], false);
    expect(paths).toEqual([
      "/xml",
      "/xml/child1",
      "/xml/child1/child2",
      "/xml/child1/child2/child3",
    ]);
  });
  it("should correctly generate the list of xml stripped paths", () => {
    const paths = SchemaModule.getAllPaths(testSchema, [], true);
    expect(paths).toEqual([
      "",
      "/child1",
      "/child1/child2",
      "/child1/child2/child3",
    ]);
  });
});
describe("renderTree", () => {
  const schemaDefinition = {
    child1: {
      _type: "text",
      child2: { _type: "text", child3: { _type: "text" } },
    },
  };

  const testSchema = SchemaModule.buildSchemaTree(schemaDefinition, "xml");
  const rendered = SchemaModule.renderTree(testSchema);

  it("should render the tree into nested TreeItem components correctly", () => {
    expect(rendered.toString()).toEqual(jsxOutput.toString());
  });
});
