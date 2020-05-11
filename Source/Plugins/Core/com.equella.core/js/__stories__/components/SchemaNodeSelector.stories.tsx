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
import * as React from "react";
import SchemaNodeSelector, {
  SchemaNode,
} from "../../tsrc/settings/SchemaNodeSelector";
import v4 from "uuid/v4";
import { action } from "@storybook/addon-actions";

const testSchema: SchemaNode = {
  id: v4(),
  label: "schemaRoot",
  children: [
    {
      id: v4(),
      label: "schemaChild1",
      children: [
        {
          id: v4(),
          label: "schemaGrandChild1",
        },
        {
          id: v4(),
          label: "schemaGrandChild2",
        },
      ],
    },
    {
      id: v4(),
      label: "schemaChild2",
    },
    {
      id: v4(),
      label: "schemaChild3",
      children: [
        {
          id: v4(),
          label: "schemaGrandChild3",
          children: [
            {
              id: v4(),
              label: "schemaGreatGrandChild1",
            },
          ],
        },
      ],
    },
  ],
};
export default {
  title: "SchemaNodeSelector",
  component: SchemaNodeSelector,
};

export const SchemaNodeSelectorComponent = () => (
  <SchemaNodeSelector
    setSelectedNode={action("Node Selected")}
    tree={testSchema}
  />
);
