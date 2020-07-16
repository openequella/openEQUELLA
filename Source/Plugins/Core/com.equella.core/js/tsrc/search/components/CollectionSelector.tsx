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
import { Checkbox, TextField } from "@material-ui/core";
import { useEffect, useState } from "react";
import { collectionListSummary } from "../../modules/CollectionsModule";
import { Autocomplete } from "@material-ui/lab";
import CheckBoxOutlineBlankIcon from "@material-ui/icons/CheckBoxOutlineBlank";
import CheckBoxIcon from "@material-ui/icons/CheckBox";

interface CollectionSelectorProps {
  /**
   * Fire when collection selections are changed.
   * @param collections Selected collections.
   */
  onSelectionChange: (collections: string[]) => void;
}

/**
 * As a refine search control, this component is used to filter search results by collections.
 */
export const CollectionSelector = ({
  onSelectionChange,
}: CollectionSelectorProps) => {
  const [collections, setCollections] = useState<[string, string][]>([]);
  useEffect(() => {
    collectionListSummary([
      "SEARCH_COLLECTION",
    ]).then((collections: Map<string, string>) =>
      setCollections(Array.from(collections))
    );
  }, []);

  return (
    <Autocomplete
      multiple
      disablePortal
      fullWidth
      limitTags={2}
      onChange={(event: React.ChangeEvent<{}>, value: [string, string][]) => {
        onSelectionChange(value.map((collection) => collection[0]));
      }}
      options={collections}
      disableCloseOnSelect
      getOptionLabel={(collection) => collection[1]}
      renderOption={(collection, { selected }) => (
        <>
          <Checkbox
            icon={<CheckBoxOutlineBlankIcon fontSize="small" />}
            checkedIcon={<CheckBoxIcon fontSize="small" />}
            checked={selected}
          />
          {collection[1]}
        </>
      )}
      renderInput={(params) => (
        <TextField
          {...params}
          variant="outlined"
          label="Collections"
          placeholder="Collections"
        />
      )}
    />
  );
};
