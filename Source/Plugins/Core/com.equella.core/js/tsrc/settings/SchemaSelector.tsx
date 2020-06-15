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
import { Grid, InputLabel, MenuItem, Select } from "@material-ui/core";
import {
  schemaListSummary,
  SchemaNode,
  schemaTree,
} from "../schema/SchemaModule";
import SchemaNodeSelector from "../settings/SchemaNodeSelector";
import { languageStrings } from "../util/langstrings";

interface SchemaSelectorProps {
  /**
   * Callback function triggered upon selection of a node.
   * @param node  Path of the selected node.
   */
  setSchemaNode: (node: string) => void;
}

/**
 * This component defines a schema selector, for selecting a schema and then a node within.
 * When a schema is selected, it will display that schema within a SchemaNodeSelector.
 *
 */
export default function SchemaSelector({ setSchemaNode }: SchemaSelectorProps) {
  const [selectedSchema, setSelectedSchema] = React.useState<
    string | undefined
  >(undefined);
  const [schema, setSchema] = React.useState<SchemaNode>();
  const [schemaList, setSchemaList] = React.useState<JSX.Element[]>([]);
  const [schemaNodePath, setSchemaNodePath] = React.useState<string>("");
  const strings =
    languageStrings.settings.searching.facetedsearchsetting.schemaselector;
  React.useEffect(() => {
    schemaListSummary().then((schemas) => {
      const elementList: JSX.Element[] = [
        <MenuItem value={undefined}>{strings.selectaschema}</MenuItem>,
      ];
      schemas.forEach((name, uuid) => {
        elementList.push(
          <MenuItem id={uuid} value={uuid}>
            {name}
          </MenuItem>
        );
      });
      setSchemaList(elementList);
    });
  }, []);

  React.useEffect(() => {
    if (selectedSchema && selectedSchema !== "") {
      schemaTree(selectedSchema).then((tree) => setSchema(tree));
    } else {
      setSchema(undefined);
    }
  }, [selectedSchema]);

  React.useEffect(() => {
    if (schemaNodePath !== "") {
      setSchemaNode(schemaNodePath);
    }
  }, [schemaNodePath]);
  return (
    <Grid container direction={"column"} spacing={0}>
      <>
        <Grid item>
          {schemaList && (
            <>
              <Select
                fullWidth
                label={
                  <InputLabel shrink id="select-label">
                    {strings.schema}
                  </InputLabel>
                }
                value={selectedSchema}
                displayEmpty
                onChange={(event) => {
                  setSelectedSchema(event.target.value as string | undefined);
                }}
              >
                {schemaList}
              </Select>
            </>
          )}
        </Grid>
        <Grid item>
          {schema && (
            <SchemaNodeSelector
              expandControls
              tree={schema}
              setSelectedNode={setSchemaNodePath}
            />
          )}
        </Grid>
      </>
    </Grid>
  );
}
