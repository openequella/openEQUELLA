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
import {
  Collection,
  collectionListSummary,
} from "../../modules/CollectionsModule";
import { Autocomplete } from "@material-ui/lab";
import CheckBoxOutlineBlankIcon from "@material-ui/icons/CheckBoxOutlineBlank";
import CheckBoxIcon from "@material-ui/icons/CheckBox";
import { languageStrings } from "../../util/langstrings";

interface CollectionSelectorProps {
  /**
   * Fires when collection selections are changed.
   * @param collections Selected collections.
   */
  onSelectionChange: (collections: Collection[]) => void;
  /**
   * Initially selected collections.
   */
  value?: Collection[];
}

/**
 * As a refine search control, this component is used to filter search results by collections.
 * The initially selected collections are either provided through props or an empty array.
 */
export const CollectionSelector = ({
  onSelectionChange,
  value,
}: CollectionSelectorProps) => {
  const collectionSelectorStrings =
    languageStrings.searchpage.collectionSelector;
  const [collections, setCollections] = useState<Collection[]>([]);
  const [selectedCollections, setSelectedCollections] = useState<Collection[]>(
    value ?? []
  );

  useEffect(() => {
    collectionListSummary([
      "SEARCH_COLLECTION",
    ]).then((collections: Map<string, string>) =>
      setCollections(
        Array.from(collections, ([uuid, name]) => ({ uuid, name }))
      )
    );
  }, []);

  return (
    <Autocomplete
      multiple
      fullWidth
      limitTags={2}
      onChange={(_, value: Collection[]) => {
        setSelectedCollections(value);
        onSelectionChange(value);
      }}
      value={selectedCollections}
      options={collections}
      disableCloseOnSelect
      getOptionLabel={(collection) => collection.name}
      getOptionSelected={(collection, selected) =>
        selected.uuid === collection.uuid
      }
      renderOption={(collection, { selected }) => (
        <>
          <Checkbox
            icon={<CheckBoxOutlineBlankIcon fontSize="small" />}
            checkedIcon={<CheckBoxIcon fontSize="small" />}
            checked={selected}
          />
          {collection.name}
        </>
      )}
      renderInput={(params) => (
        <TextField
          {...params}
          variant="outlined"
          label={collectionSelectorStrings.label}
          placeholder={collectionSelectorStrings.label}
        />
      )}
    />
  );
};
