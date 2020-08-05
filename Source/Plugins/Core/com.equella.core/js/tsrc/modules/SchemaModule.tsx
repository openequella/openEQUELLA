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
import { API_BASE_URL } from "../config";
import { summarisePagedBaseEntities } from "./OEQHelpers";

/**
 * A minimal representation of a node within an oEQ schema.
 *
 * If a node has no/undefined 'parent' then it is the root. If a node has no/undefined
 * children then it is a leaf node.
 *
 * Technically leaf nodes typically have a type (e.g. text) however that information
 * (along with some other bits) are not retained in this representation. However it can be
 * retrieved via the REST API.
 */
export interface SchemaNode {
  name: string;
  parent?: SchemaNode;
  children?: SchemaNode[];
}

/**
 * Provides a simple Map<string,string> summary of available schemas, where K is the UUID
 * and V is the schema's name.
 *
 * On failure, an OEQ.Errors.ApiError will be thrown.
 */
export const schemaListSummary = (): Promise<Map<string, string>> =>
  OEQ.Schema.listSchemas(API_BASE_URL, {
    // We believe very few people have more than 5 schemas, so this will do for now.
    // As there are some oddities currently in the oEQ paging as seen in
    // https://github.com/openequella/openEQUELLA/issues/1735
    length: 100,
  }).then(summarisePagedBaseEntities);

/**
 * Recursive helper function to build a simple outline of the structure of an oEQ schema.
 *
 * @param definition Typically something like a Record<string,any> returned via the REST API
 * @param name The name to be used for the next new node.
 * @param parent Mainly for recursive calls to provide back linking to the parents.
 */
export const buildSchemaTree = (
  definition: any,
  name: string,
  parent?: SchemaNode
): SchemaNode => {
  const node: SchemaNode = { name: name, parent: parent };
  node.children = Object.keys(definition)
    .filter((childName: string) => typeof definition[childName] === "object")
    .map((childName: string) =>
      buildSchemaTree(definition[childName], childName, node)
    );

  return node;
};

/**
 * A function to provide a structural outline of a schema. On success returns an oEQ schema
 * rooted with the standard <xml> root - which oEQ typically drops when capturing schema
 * paths.
 *
 * Any request type errors will result in ApiError. If it is found that the root is not the
 * standard singular <xml> then a basic Error will be thrown.
 *
 * @param uuid The UUID of the schema to retrieve from the server via REST API.
 */
export const schemaTree = (uuid: string): Promise<SchemaNode> =>
  OEQ.Schema.getSchema(API_BASE_URL, uuid).then(
    (schema: OEQ.Schema.EquellaSchema) => {
      const elements = Object.keys(schema.definition);
      const standardRoot = "xml";
      if (elements.length !== 1 && elements[0] !== standardRoot) {
        throw new Error(
          "Received schema does not start with the standard <xml> root element."
        );
      }
      return buildSchemaTree(schema.definition[standardRoot], standardRoot);
    }
  );

/**
 * Helper function to generate node path string from a node.
 *
 * @param node The node to generate a path for.
 * @param stripXml Whether to include the leading /xml - although returned in schemas, not typically used in paths.
 */
export const pathForNode = (node: SchemaNode, stripXml = true): string => {
  let path = "";
  let currentNode: SchemaNode | undefined = node;
  while (currentNode) {
    path = `/${currentNode.name}${path}`;
    currentNode = currentNode.parent;
  }

  // Minor optimisation to chop the prefix off at the end
  if (stripXml) {
    path = path.substring(4);
  }

  return path;
};

/**
 * Recursive helper function to get a list of all possible xml paths from a given SchemaNode.
 * @param nodes The schema to generate paths for.
 * @param paths Used by the recursive algorithm to build up the returned list.
 *          When passing in initially, leave as a blank array.
 * @param stripXml Passed into pathForNode. Whether to include the leading /xml.
 */
export const getAllPaths = (
  nodes: SchemaNode,
  stripXml = true,
  paths: string[] = []
): string[] =>
  paths
    .concat(pathForNode(nodes, stripXml))
    .concat(
      nodes.children?.flatMap((childNode) =>
        getAllPaths(childNode, stripXml, paths)
      ) ?? []
    );
