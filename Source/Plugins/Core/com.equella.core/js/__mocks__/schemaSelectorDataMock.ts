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
import { SchemaNode } from "../tsrc/schema/SchemaModule";

export const testSchema: SchemaNode = {
  name: "schemaRoot",
  children: [
    {
      name: "schemaChild1",
      children: [
        {
          name: "schemaGrandChild1",
          parent: {
            name: "SchemaChild1",
            parent: { name: "schemaRoot" },
          },
        },
        {
          name: "schemaGrandChild2",
          parent: {
            name: "SchemaChild1",
            parent: { name: "schemaRoot" },
          },
        },
      ],
    },
    {
      name: "schemaChild2",
      parent: { name: "schemaRoot" },
    },
    {
      name: "schemaChild3",
      parent: { name: "schemaRoot" },
      children: [
        {
          name: "schemaGrandChild3",
          parent: {
            name: "schemaChild3",
            parent: { name: "schemaRoot" },
          },
          children: [
            {
              name: "schemaGreatGrandChild1",
              parent: {
                name: "schemaGrandChild3",
                parent: {
                  name: "schemaChild3",
                  parent: { name: "schemaRoot" },
                },
              },
            },
          ],
        },
      ],
    },
  ],
};
