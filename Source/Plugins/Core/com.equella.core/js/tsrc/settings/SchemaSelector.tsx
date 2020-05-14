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
import React from "react";
import SchemaNodeSelector, { SchemaNode } from "./SchemaNodeSelector";
import { MenuItem, Select } from "@material-ui/core";

interface SchemaSelectorProps {}
/**
 * This component defines a schema selector, for selecting a schema and then a node within.
 * When a schema is selected, it will display that schema within a SchemaNodeSelector.
 */
export default function SchemaSelector({}: SchemaSelectorProps) {
  const [schemaList, setSchemaList] = React.useState();
  const [selectedSchema, setSelectedSchema] = React.useState();
  const [schemaTree, setSchemaTree] = React.useState<SchemaNode | undefined>();
  const [schemaNode, setSchemaNode] = React.useState<string | undefined>();
  React.useEffect(() => {
    getSchemaList().then((res) => {
      setSchemaList(res.data);
    });
    //getSchemaList
  }, []);
  React.useEffect(() => {
    getSchema(selectedSchema).then((res) => setSchemaTree(res.data));
    //populate schemaNodeSelector
  }, [selectedSchema]);

  if (schemaTree) {
    return (
      <SchemaNodeSelector tree={schemaTree} setSelectedNode={setSchemaNode} />
    );
  } else if (schemaList) {
    return (
      <Select>
        {schemaList.map((schema) => (
          <MenuItem id={schema.id} value={schema.name}>
            {schema.name}
          </MenuItem>
        ))}
      </Select>
    );
  }
  return <div />;
}
