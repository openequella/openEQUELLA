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
import { Checkbox, TextField } from "@mui/material";
import CheckBoxIcon from "@mui/icons-material/CheckBox";
import CheckBoxOutlineBlankIcon from "@mui/icons-material/CheckBoxOutlineBlank";
import { Autocomplete } from "@mui/material";
import { AutocompleteGetTagProps } from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import * as React from "react";
import { useContext, useEffect, useState } from "react";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as ORD from "fp-ts/Ord";
import * as O from "fp-ts/Option";
import * as S from "fp-ts/string";
import { TooltipChip } from "../../components/TooltipChip";
import {
  Collection,
  collectionListSummary,
} from "../../modules/CollectionsModule";
import { languageStrings } from "../../util/langstrings";
import { SearchContext } from "../SearchPageHelper";

/**
 * List of Collection UUIDs that are externally configured to be available for selection.
 * If provided, it takes the precedence over the full list of Collections, but it is also
 * subject to permission control. Hence, the final list is the intersection between the
 * full list and this configured list.
 *
 * It is usually provided through OEQ Legacy server side rendering together with Legacy content
 * API. For more details, please check the use of {@link PageContent#script} and {@link LegacyContent}
 */
declare const configuredCollections: string[] | undefined;

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

const { title, noOptions } = languageStrings.searchpage.collectionSelector;

/**
 * As a refine search control, this component is used to filter search results by collections.
 * The initially selected collections are either provided through props or an empty array.
 *
 * Note that the available Collections can be pre-configured externally. So the final list of Collections
 * is the intersection of the full list and the pre-configured list.
 */
export const CollectionSelector = ({
  onSelectionChange,
  value,
}: CollectionSelectorProps) => {
  const [collections, setCollections] = useState<Collection[]>([]);
  const { searchPageErrorHandler } = useContext(SearchContext);

  useEffect(() => {
    collectionListSummary([OEQ.Acl.ACL_SEARCH_COLLECTION])
      .then((collections: Collection[]) => {
        const fullList: Collection[] = pipe(
          collections,
          A.sort(
            ORD.contramap<string, Collection>(({ name }) => name.toLowerCase())(
              S.Ord,
            ),
          ),
        );

        const filterByUUID = (uuid: string): O.Option<Collection> =>
          pipe(
            fullList,
            A.findFirst((c) => c.uuid === uuid),
          );

        setCollections(
          pipe(
            typeof configuredCollections !== "undefined"
              ? configuredCollections
              : undefined,
            O.fromNullable,
            O.map(A.filterMap(filterByUUID)),
            O.getOrElse(() => fullList),
          ),
        );
      })
      .catch(searchPageErrorHandler);
  }, [searchPageErrorHandler]);

  return (
    <Autocomplete
      multiple
      limitTags={2}
      renderTags={(
        collections: Collection[],
        getTagProps: AutocompleteGetTagProps,
      ) =>
        collections.map((collection: Collection, index: number) => (
          <TooltipChip
            key={collection.uuid}
            id={`collectionChip-${collection.uuid}`}
            title={collection.name}
            maxWidth={200}
            tagProps={getTagProps({ index })}
          />
        ))
      }
      onChange={(_, value: Collection[]) => {
        onSelectionChange(value);
      }}
      value={value ?? []}
      options={collections}
      disableCloseOnSelect
      getOptionLabel={(collection) => collection.name}
      isOptionEqualToValue={(collection, selected) =>
        selected.uuid === collection.uuid
      }
      renderOption={(props, collection, { selected }) => (
        <li {...props}>
          <Checkbox
            icon={<CheckBoxOutlineBlankIcon fontSize="small" />}
            checkedIcon={<CheckBoxIcon fontSize="small" />}
            checked={selected}
          />
          {collection.name}
        </li>
      )}
      noOptionsText={noOptions}
      renderInput={(params) => (
        <TextField
          {...params}
          variant="outlined"
          label={title}
          placeholder={title}
        />
      )}
    />
  );
};
