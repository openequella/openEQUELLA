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
import {
  CircularProgress,
  Grid,
  IconButton,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  TextField,
  Tooltip,
  Typography,
} from "@material-ui/core";
import ErrorOutline from "@material-ui/icons/ErrorOutline";
import InfoIcon from "@material-ui/icons/Info";
import SearchIcon from "@material-ui/icons/Search";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import * as EQ from "fp-ts/Eq";
import { flow, pipe } from "fp-ts/function";
import * as M from "fp-ts/Map";
import * as O from "fp-ts/Option";
import * as ORD from "fp-ts/Ord";
import { not } from "fp-ts/Predicate";
import * as RA from "fp-ts/ReadonlyArray";
import * as TASK from "fp-ts/Task";
import * as TE from "fp-ts/TaskEither";
import * as React from "react";
import * as RSET from "fp-ts/ReadonlySet";
import * as S from "fp-ts/string";
import { KeyboardEvent, useEffect, useState } from "react";
import { sprintf } from "sprintf-js";
import { resolveGroups } from "../../modules/GroupModule";
import { languageStrings } from "../../util/langstrings";
import { OrdAsIs } from "../../util/Ord";
import { CheckboxList } from "../CheckboxList";

const { filterActiveNotice } = languageStrings.baseSearchComponent;
const { filteredByPrelude } = languageStrings.userSearchComponent;

export interface BaseSecurityEntity {
  id: string;
}

/**
 * Given a set of items, return a set of UUIDs for all the items.
 */
const itemIds: <T extends BaseSecurityEntity>(
  a: ReadonlySet<T>
) => ReadonlySet<string> = flow(RSET.map(S.Eq)(({ id }) => id));

/**
 * Generic function which can generate an `Eq` for item with id attribute.
 */
export const eqItemById = <T extends BaseSecurityEntity>() =>
  EQ.contramap<string, T>((entry: T) => entry.id)(S.Eq);

/**
 * Commonly shared props definition for entity search components (BaseSearch/UserSearch/GroupSearch).
 * `T` represents the type of Item details.
 */
export interface CommonEntitySearchProps<T> {
  /**
   * A set of items in the list which should be 'selected'/ticked/checked.
   * Except some edge cases it will receive an empty set as an initial value.
   */
  selections: ReadonlySet<T>;
  /** Callback triggered when selected items are changed. */
  onChange: (items: ReadonlySet<T>) => void;
  /** How high (in pixels) the list of entries should be. */
  listHeight?: number;
  /** Whether enable multiple selection or not, default value is `false` **/
  enableMultiSelection?: boolean;
  /** A list of groups UUIDs to filter the items by. */
  groupFilter?: ReadonlySet<string>;
  /**
   * Function which will resolve group IDs to full group details so that the group names can be
   * used for display.
   */
  resolveGroupsProvider?: (
    ids: ReadonlyArray<string>
  ) => Promise<OEQ.UserQuery.GroupDetails[]>;
}

/**
 * `BaseSearch` props definition which accepts a type parameter representing the type of Item details.
 */
export interface BaseSearchProps<T> extends CommonEntitySearchProps<T> {
  /** An optional `id` attribute for the component. Will also be used to prefix core child elements. */
  id?: string;
  /**
   * Customized strings displayed in UI
   */
  strings?: {
    /**
     * The text displayed in the search bar.
     */
    queryFieldLabel: string;
    /**
     * The text displayed when a search action can't find any item.
     */
    failedToFindMessage: string;
  };
  /** Order used to sort items. Default order is determined by ID.*/
  itemOrd?: ORD.Ord<T>;
  /**
   * Eq used to determine the equality of items.
   * The equality is determined by item's ID by default.
   */
  itemEq?: EQ.Eq<T>;
  /** Function which will provide the list of items.
   *
   * @param query Text used to query items.
   * @param filter A list of other item's UUIDs to filter the item by.
   *               Mainly used for searching users since users can be filtered by groups.
   * */
  itemListProvider: (
    query?: string,
    filter?: ReadonlySet<string>
  ) => Promise<T[]>;
  /**
   * A template used to display an item entry in the CheckboxList.
   * Ideally the element will be ListItemText. But other type of elements are still acceptable.
   * The default value will display the id of item in `ListItemText`.
   */
  itemDetailsToEntry?: (item: T) => JSX.Element;
}

/**
 * Provides a control to list items(security entities: user/group/role) via an input field text query filter.
 * Items can then be selected (support single/multiple select).
 *
 * Each Item must have a string ID.
 */
const BaseSearch = <T extends BaseSecurityEntity>({
  id,
  listHeight,
  strings = languageStrings.baseSearchComponent,
  selections,
  onChange,
  itemOrd = ORD.contramap(({ id }: T) => id)(S.Ord),
  itemEq = eqItemById<T>(),
  itemDetailsToEntry = (item: T) => <ListItemText primary={item.id} />,
  enableMultiSelection = false,
  itemListProvider,
  groupFilter,
  resolveGroupsProvider = resolveGroups,
}: BaseSearchProps<T>) => {
  const [query, setQuery] = useState<string>("");
  const [items, setItems] = useState<T[]>([]);
  // Group details used for show group's names in the tooltip content
  const [groupDetails, setGroupDetails] = useState<
    OEQ.UserQuery.GroupDetails[]
  >([]);

  const [hasSearched, setHasSearched] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<String>();
  const [showSpinner, setShowSpinner] = useState<boolean>(false);

  const { queryFieldLabel, failedToFindMessage } = strings;

  useEffect(() => {
    if (!groupFilter) {
      setGroupDetails([]);
      return;
    }

    const retrieveGroupDetails: TASK.Task<void> = pipe(
      groupFilter,
      O.fromPredicate(not(RSET.isEmpty)),
      O.map(
        flow(
          RSET.toReadonlyArray<string>(OrdAsIs),
          TE.tryCatchK(
            resolveGroupsProvider,
            (reason) => `Failed to retrieve full group details: ${reason}`
          ),
          TE.match(console.error, setGroupDetails)
        )
      ),
      O.getOrElse(() => TASK.fromIO(() => setGroupDetails([])))
    );

    (async () => await retrieveGroupDetails())();
  }, [groupFilter, resolveGroupsProvider]);

  const filterDetails = A.isNonEmpty(groupDetails) ? (
    <>
      <Typography variant="caption">{filteredByPrelude}</Typography>
      <ul>
        {pipe(
          groupDetails,
          RA.sort(
            ORD.contramap((g: OEQ.UserQuery.GroupDetails) => g.name)(S.Ord)
          ),
          RA.map(({ id, name }) => <li key={id}>{name}</li>)
        )}
      </ul>
    </>
  ) : undefined;

  // Simple helper function to assist with providing useful id's for testing and theming.
  const genId = (suffix?: string) =>
    (id ? `${id}-` : "") + "BaseSearch" + (suffix ? `-${suffix}` : "");

  const handleOnSearch = () => {
    setShowSpinner(true);
    setErrorMessage(undefined);

    itemListProvider(query, groupFilter)
      .then(flow(A.sort(itemOrd), setItems))
      .catch((error: OEQ.Errors.ApiError) => {
        setItems([]);
        if (error.status !== 404) {
          setErrorMessage(error.message);
        }
      })
      .finally(() => {
        setShowSpinner(false);
        setHasSearched(true);
      });
  };

  /**
   * Generate a `item` ReadonlySet based on the selected item ids and pass it to `onChange` function.
   * And if `multipleSelection` is not enabled, the previous selected item would uncheck and then new item will be added.
   * */
  const handleSelectedItemChanged = (ids: ReadonlySet<string>) =>
    pipe(
      items,
      A.filter((i) => ids.has(i.id)),
      RSET.fromReadonlyArray(itemEq),
      (currentSelectedEntries) =>
        enableMultiSelection
          ? currentSelectedEntries
          : RSET.difference(itemEq)(currentSelectedEntries, selections),
      onChange
    );

  const handleQueryFieldKeypress = (event: KeyboardEvent<HTMLDivElement>) => {
    switch (event.key) {
      case "Escape":
        setQuery("");
        setItems([]);
        onChange(RSET.empty);
        setHasSearched(false);
        event.stopPropagation();
        break;
      case "Enter":
        handleOnSearch();
        break;
    }
  };

  const queryBar = (
    <Grid id={genId("QueryBar")} container spacing={1}>
      <Grid item>
        <IconButton onClick={handleOnSearch}>
          <SearchIcon />
        </IconButton>
      </Grid>
      <Grid item style={{ flexGrow: 1 }}>
        <TextField
          label={queryFieldLabel}
          value={query}
          onChange={(event) => {
            setHasSearched(false);
            setQuery(event.target.value);
          }}
          onKeyDown={handleQueryFieldKeypress}
          fullWidth
        />
      </Grid>
    </Grid>
  );

  /**
   * Filter enabled notice for user.
   */
  const filterNotice = (filterDetails: JSX.Element): JSX.Element => (
    <Grid container spacing={1}>
      <Grid item>
        <Tooltip title={filterDetails}>
          <InfoIcon fontSize="small" />
        </Tooltip>
      </Grid>
      <Grid item>
        <Typography variant="caption">{filterActiveNotice}</Typography>
      </Grid>
    </Grid>
  );

  /**
   * Convert `ItemDetails` Array to a simple Map which can be used in CheckboxList.
   */
  const itemDetailsToEntriesMap: (_: T[]) => Map<string, JSX.Element> = flow(
    A.reduce(new Map<string, JSX.Element>(), (entries, item) =>
      pipe(entries, M.upsertAt(S.Eq)(item.id, itemDetailsToEntry(item)))
    )
  );

  /**
   * Display a list of items in CheckboxList.
   */
  const itemList = () => {
    const isItemFound = items.length > 0;

    // If there's no entries because a search has not been done,
    // then return with nothing
    if (!isItemFound && !hasSearched) {
      return null;
    }

    return (
      <List
        id={genId("ItemList")}
        style={listHeight ? { height: listHeight, overflow: "auto" } : {}}
      >
        {isItemFound ? (
          <CheckboxList
            id="item-search-list"
            options={itemDetailsToEntriesMap(items)}
            checked={itemIds(selections)}
            onChange={handleSelectedItemChanged}
          />
        ) : (
          <ListItem>
            <ListItemIcon>
              <ErrorOutline color={errorMessage ? "secondary" : "inherit"} />
            </ListItemIcon>
            <ListItemText
              secondary={errorMessage ?? sprintf(failedToFindMessage, query)}
            />
          </ListItem>
        )}
      </List>
    );
  };

  const spinner = (
    <Grid container justifyContent="center">
      <Grid item>
        <CircularProgress />
      </Grid>
    </Grid>
  );

  return (
    <Grid id={genId()} container direction="column" spacing={1}>
      <Grid item xs={12}>
        {queryBar}
      </Grid>
      {filterDetails && <Grid item>{filterNotice(filterDetails)}</Grid>}
      <Grid item xs={12}>
        {showSpinner ? spinner : itemList()}
      </Grid>
    </Grid>
  );
};

export default BaseSearch;
