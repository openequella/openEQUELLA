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
  Button,
  CircularProgress,
  Grid,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  TextField,
  Tooltip,
  Typography,
} from "@mui/material";
import ErrorOutline from "@mui/icons-material/ErrorOutline";
import InfoIcon from "@mui/icons-material/Info";
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
import {
  BaseSecurityEntity,
  eqEntityById,
} from "../../modules/ACLEntityModule";
import {
  eqGroupById,
  groupIds,
  groupOrd,
  searchGroups,
  findGroupsByIds,
} from "../../modules/GroupModule";
import { languageStrings } from "../../util/langstrings";
import { CheckboxList } from "../CheckboxList";
import { SelectList } from "../SelectList";
import GroupSearch from "./GroupSearch";

const {
  filterActiveNotice,
  filterByGroupsButtonLabel,
  filteredByPrelude,
  provideQueryMessage,
} = languageStrings.baseSearchComponent;
const {
  edit: editLabel,
  clear: clearLabel,
  select: selectLabel,
  selectAll: selectAllLabel,
  selectNone: clearAllLabel,
} = languageStrings.common.action;

/**
 * Given a set of items, return a set of UUIDs for all the items.
 */
const itemIds: <T extends BaseSecurityEntity>(
  a: ReadonlySet<T>,
) => ReadonlySet<string> = flow(RSET.map(S.Eq)(({ id }) => id));

/**
 * One click mode definition for BaseSearch.
 * In this mode onAdd event handler wil be triggered when the add icon is clicked.
 */
interface OneClickMode<T> {
  type: "one_click";
  onAdd: (item: T) => void;
}

/**
 * Type guard function to check if it's checkbox mode.
 */
const checkIsCheckboxMode = <T,>(
  mode: OneClickMode<T> | CheckboxMode<T>,
): mode is CheckboxMode<T> => mode.type === "checkbox";

/**
 * Checkbox mode definition for BaseSearch.
 * In this mode, it can accept initial selections and the onChange event handler will be triggered
 * when the select button is clicked.
 */
export interface CheckboxMode<T> {
  type: "checkbox";
  /**
   * A set of items in the list which should be 'selected'/ticked/checked.
   * Except some edge cases it will receive an empty set as an initial value.
   */
  selections: ReadonlySet<T>;
  /** Callback triggered when selected items are changed. */
  onChange: (items: ReadonlySet<T>) => void;
  /**
   * An optional select button can be displayed to allow for an explicit user interaction to mark completion.
   * If undefined no select button will be displayed.
   */
  selectButton?: {
    /** Disabled flag. */
    disabled?: boolean;
    /** Callback triggered when selected button are clicked. */
    onClick: () => void;
  };
  /** Whether enable multiple selection or not, default value is `false` **/
  enableMultiSelection?: boolean;
}

/**
 * Commonly shared props definition for entity search components (BaseSearch/UserSearch/GroupSearch).
 * `T` represents the type of Item details.
 */
export interface CommonEntitySearchProps<T> {
  /** An optional `id` attribute for the component. Will also be used to prefix core child elements. */
  id?: string;
  /**
   * Mode to determine the behaviour of the search component.
   * See details of two different modes in {@link OneClickMode} and {@link CheckboxMode}.
   */
  mode: OneClickMode<T> | CheckboxMode<T>;
  /**
   * Customized strings displayed in UI
   */
  strings?: {
    /**
     * The help text tile displayed above the search bar.
     */
    helpTitle: string;
    /**
     * The help text description displayed above the search bar.
     */
    helpDesc: string;
    /**
     * The text displayed in the search bar.
     */
    queryFieldLabel: string;
    /**
     * The text displayed when a search action can't find any item.
     */
    failedToFindMessage: string;
  };
  /**
   * Show help text displayed above the search bar if `true`. Default value is `false`.
   */
  showHelpText?: boolean;
  /**
   * Handler for `select all` button, if undefined no `select all` button will be displayed.
   */
  onSelectAll?: (allItems: ReadonlySet<T>) => void;
  /**
   * Handler for `clear all` (select none) button, if undefined no `clear all` button will be displayed.
   */
  onClearAll?: (allItems: ReadonlySet<T>) => void;
  /**
   * Handler for cancel button, if undefined no cancel button will be displayed.
   */
  onCancel?: () => void;
  /** How high (in pixels) the list of entries should be. */
  listHeight?: number;
  /** A list of groups UUIDs to filter the items by. */
  groupFilter?: ReadonlySet<string>;
  /** `true` to let user choose groups to filter the search result. The default value is `false`. **/
  groupFilterEditable?: boolean;
  /**
   * Function which will resolve group IDs to full group details so that the group names can be
   * used for display.
   */
  resolveGroupsProvider?: (
    ids: ReadonlySet<string>,
  ) => Promise<OEQ.UserQuery.GroupDetails[]>;
  /** Function which will provide the list of group. Used to let user choose what groups are used to filter the result.
   *
   * @param query Text used to query groups.
   * */
  groupSearch?: (query?: string) => Promise<OEQ.UserQuery.GroupDetails[]>;
}

/**
 * `BaseSearch` props definition which accepts a type parameter representing the type of Item details.
 */
export interface BaseSearchProps<T> extends CommonEntitySearchProps<T> {
  /** Function which will provide the list of items.
   *
   * @param query Text used to query items.
   * @param filter A list of other item's UUIDs to filter the item by.
   *               Mainly used for searching users since users can be filtered by groups.
   */
  search: (query?: string, filter?: ReadonlySet<string>) => Promise<T[]>;
  /** Order used to sort items. Default order is determined by ID.*/
  itemOrd?: ORD.Ord<T>;
  /**
   * Eq used to determine the equality of items.
   * The equality is determined by item's ID by default.
   */
  itemEq?: EQ.Eq<T>;
  /**
   * A template used to display an item entry in the CheckboxList.
   * Ideally the element will be ListItemText. But other type of elements are still acceptable.
   * The default value will display the id of item in `ListItemText`.
   */
  itemDetailsToEntry?: (item: T) => JSX.Element;
}

/**
 * Generate query with wildcard suffix if it's not undefined.
 */
export const wildcardQuery = (query?: string): string | undefined =>
  query ? `${query}*` : undefined;

/**
 * Provides a control to list items(security entities: user/group/role) via an input field text query filter.
 * Items can then be selected (support single/multiple select).
 *
 * Each Item must have a string ID.
 *
 * The search component has 2 modes:
 *
 * Default mode: In this mode, a checkbox list is used to display items.
 *               Users need to tick the checkboxes to select items.
 *               And under this mode it also has two sub modes multiple selection and single selection mode.
 *
 * One click mode: This mode is activated when the `onAdd` property is not undefined.
 *                 In this scenario, a SelectList is used to display items.
 *                 Users can trigger the onAdd event directly by clicking the add icon in the corresponding item.
 */
const BaseSearch = <T extends BaseSecurityEntity>({
  id,
  mode,
  listHeight,
  strings = languageStrings.baseSearchComponent,
  showHelpText,
  onCancel,
  onSelectAll,
  onClearAll,
  itemOrd = ORD.contramap(({ id }: T) => id)(S.Ord),
  itemEq = eqEntityById<T>(),
  itemDetailsToEntry = (item: T) => <ListItemText primary={item.id} />,
  groupFilterEditable = false,
  groupFilter,
  search,
  groupSearch = (query?: string) => searchGroups(wildcardQuery(query)),
  resolveGroupsProvider = findGroupsByIds,
}: BaseSearchProps<T>) => {
  const [query, setQuery] = useState<string>("");
  const [items, setItems] = useState<T[]>([]);
  // Group details used for show group's names in the tooltip content
  const [groupDetails, setGroupDetails] = useState<
    ReadonlySet<OEQ.UserQuery.GroupDetails>
  >(RSET.empty);

  // use this intermedia group details to store selections for group filters search, so it can support cancel action.
  const [groupFilterSearchGroupDetails, setGroupFilterSearchGroupDetails] =
    useState<ReadonlySet<OEQ.UserQuery.GroupDetails>>(RSET.empty);

  const [showGroupFilterSearch, setShowGroupFilterSearch] = useState(false);

  const [hasSearched, setHasSearched] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string>();
  const [showSpinner, setShowSpinner] = useState<boolean>(false);

  const { helpTitle, helpDesc, queryFieldLabel, failedToFindMessage } = strings;

  const isCheckboxMode = checkIsCheckboxMode<T>(mode);

  // look-up of the group details for groupFilter
  useEffect(() => {
    const updateGroupDetails = (
      groups: ReadonlySet<OEQ.UserQuery.GroupDetails>,
    ) => {
      setGroupDetails(groups);
      setGroupFilterSearchGroupDetails(groups);
    };

    // generate a collection of GroupDetails from groupIds where the name is simply the same as the id
    const buildUnnamedGroupDetails = (
      groupIds: ReadonlySet<string>,
    ): ReadonlyArray<OEQ.UserQuery.GroupDetails> =>
      pipe(
        groupIds,
        RSET.map(eqGroupById)((gid: string) => ({ name: gid, id: gid })),
        RSET.toReadonlyArray(groupOrd),
      );

    const nonUndefinedGroupFilter: ReadonlySet<string> =
      groupFilter ?? RSET.empty;

    const retrieveGroupDetails: TASK.Task<void> = pipe(
      nonUndefinedGroupFilter,
      O.fromPredicate(not(RSET.isEmpty)),
      O.map(
        flow(
          TE.tryCatchK(
            resolveGroupsProvider,
            (reason) => `Failed to retrieve full group details: ${reason}`,
          ),
          TE.mapLeft((e: string) => {
            console.error(e);
            return buildUnnamedGroupDetails(nonUndefinedGroupFilter);
          }),
          TE.getOrElse(TASK.of),
          TASK.map(RSET.fromReadonlyArray(eqGroupById)),
        ),
      ),
      O.getOrElse<TASK.Task<ReadonlySet<OEQ.UserQuery.GroupDetails>>>(() =>
        TASK.of(RSET.empty),
      ),
      TASK.map(updateGroupDetails),
    );

    (async () => await retrieveGroupDetails())();
  }, [groupFilter, resolveGroupsProvider]);

  // Simple helper function to assist with providing useful id's for testing and theming.
  const genId = (suffix?: string) =>
    (id ? `${id}-` : "") + "BaseSearch" + (suffix ? `-${suffix}` : "");

  /**
   * The elements related to group filter, display different content based on `filterDetails` and `groupFilterEditable`.
   */
  const groupFilterContent = () => {
    const filterDetails = () => (
      <>
        <Typography variant="caption">{filteredByPrelude}</Typography>
        <ul>
          {pipe(
            groupDetails,
            RSET.toReadonlyArray(groupOrd),
            RA.sort(
              ORD.contramap((g: OEQ.UserQuery.GroupDetails) => g.name)(S.Ord),
            ),
            RA.map(({ id, name }) => <li key={id}>{name}</li>),
          )}
        </ul>
      </>
    );

    /**
     * Filter enabled notice for user.
     */
    const filterNotice = (): JSX.Element => (
      <Grid key="filterNotice" container spacing={1}>
        <Grid>
          <Tooltip title={filterDetails()}>
            <InfoIcon fontSize="small" />
          </Tooltip>
        </Grid>
        <Grid>
          <Typography variant="caption">{filterActiveNotice}</Typography>
        </Grid>
      </Grid>
    );

    /**
     * Filter by group button.
     * If `groupFilterEditable` is enabled it should be displayed when `GroupFilter` is empty.
     */
    const filterByGroupButton = () => (
      <Grid key="filterByGroupButton" container>
        <Button onClick={() => setShowGroupFilterSearch(true)} color="primary">
          {filterByGroupsButtonLabel}
        </Button>
      </Grid>
    );

    /**
     * Edit and clear button.
     * If `groupFilterEditable` is enabled it should be displayed when `GroupFilter` is not empty.
     */
    const editAndClearGroupFilterButtons = () => (
      <Grid key="editAndClearGroupFilterButtons" container>
        <Button onClick={() => setShowGroupFilterSearch(true)} color="primary">
          {editLabel}
        </Button>
        <Button
          onClick={() => {
            setGroupDetails(RSET.empty);
            setGroupFilterSearchGroupDetails(RSET.empty);
          }}
          color="primary"
        >
          {clearLabel}
        </Button>
      </Grid>
    );

    const hasGroupDetails = !RSET.isEmpty(groupDetails);

    const groupDetailsWithEdit = () =>
      hasGroupDetails
        ? [filterNotice(), editAndClearGroupFilterButtons()]
        : [filterByGroupButton()];

    const groupDetailsNoEdit = () =>
      hasGroupDetails ? [filterNotice()] : undefined;

    const groupFilterElements = groupFilterEditable
      ? groupDetailsWithEdit()
      : groupDetailsNoEdit();

    return groupFilterElements ? (
      <Grid style={{ padding: "15px" }}>{groupFilterElements}</Grid>
    ) : undefined;
  };

  const handleOnSearch = () => {
    setShowSpinner(true);
    setErrorMessage(undefined);

    search(query, groupIds(groupDetails))
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
        if (isCheckboxMode) {
          mode.onChange(RSET.empty);
        }
      });
  };

  /**
   * Generate a `item` ReadonlySet based on the selected item ids and pass it to `onChange` function.
   * And if `multipleSelection` is not enabled, the previous selected item would uncheck and then new item will be added.
   * */
  const handleSelectedItemChanged =
    (mode: CheckboxMode<T>) => (ids: ReadonlySet<string>) =>
      pipe(
        items,
        A.filter((i) => ids.has(i.id)),
        RSET.fromReadonlyArray(itemEq),
        (currentSelectedEntries) =>
          mode.enableMultiSelection
            ? currentSelectedEntries
            : RSET.difference(itemEq)(currentSelectedEntries, mode.selections),
        mode.onChange,
      );

  const handleQueryFieldKeypress = (event: KeyboardEvent<HTMLDivElement>) => {
    switch (event.key) {
      case "Escape":
        setQuery("");
        setItems([]);
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
      <Grid size="grow">
        <TextField
          label={queryFieldLabel}
          value={query}
          onChange={(event) => {
            setHasSearched(false);
            setQuery(event.target.value);
          }}
          onKeyDown={handleQueryFieldKeypress}
          fullWidth
          variant="standard"
        />
      </Grid>
    </Grid>
  );

  /**
   * Convert `ItemDetails` Array to a simple Map which can be used in CheckboxList.
   */
  const itemDetailsToEntriesMap: (_: T[]) => Map<string, JSX.Element> = flow(
    A.reduce(new Map<string, JSX.Element>(), (entries, item) =>
      pipe(entries, M.upsertAt(S.Eq)(item.id, itemDetailsToEntry(item))),
    ),
  );

  const warningMessage = (
    <ListItem>
      <ListItemIcon>
        <ErrorOutline color={errorMessage ? "secondary" : "inherit"} />
      </ListItemIcon>
      <ListItemText
        secondary={
          hasSearched
            ? (errorMessage ?? sprintf(failedToFindMessage, query))
            : provideQueryMessage
        }
      />
    </ListItem>
  );

  const isItemFound = items.length > 0;

  /**
   * Display a list of items in CheckboxList.
   */
  const itemList = () => {
    const list = !isCheckboxMode ? (
      <SelectList
        options={itemDetailsToEntriesMap(items)}
        onSelect={(itemId) =>
          pipe(
            items,
            A.findFirst(({ id }) => id === itemId),
            O.map(mode.onAdd),
          )
        }
      />
    ) : (
      <CheckboxList
        id="item-search-list"
        options={itemDetailsToEntriesMap(items)}
        checked={itemIds(mode.selections)}
        onChange={handleSelectedItemChanged(mode)}
      />
    );

    return (
      <List
        id={genId("ItemList")}
        style={listHeight ? { height: listHeight, overflow: "auto" } : {}}
      >
        {isItemFound ? list : warningMessage}
      </List>
    );
  };

  const selectAllButton = () =>
    onSelectAll ? (
      <Grid>
        <Button
          color="secondary"
          onClick={() =>
            pipe(
              items,
              RSET.fromReadonlyArray(itemEq),
              RSET.union(itemEq)(isCheckboxMode ? mode.selections : RSET.empty),
              onSelectAll,
            )
          }
        >
          {selectAllLabel}
        </Button>
      </Grid>
    ) : undefined;

  const clearAllButton = () =>
    onClearAll ? (
      <Grid>
        <Button color="secondary" onClick={() => onClearAll(RSET.empty)}>
          {clearAllLabel}
        </Button>
      </Grid>
    ) : undefined;

  const selectButtonElement = () =>
    isCheckboxMode && mode.selectButton ? (
      <Grid>
        <Button
          color="primary"
          onClick={mode.selectButton.onClick}
          disabled={mode.selectButton.disabled}
        >
          {selectLabel}
        </Button>
      </Grid>
    ) : undefined;

  const cancelButton = () =>
    onCancel ? (
      <Grid>
        <Button color="primary" onClick={onCancel}>
          {languageStrings.common.action.cancel}
        </Button>
      </Grid>
    ) : undefined;

  const spinner = (
    <Grid container justifyContent="center">
      <Grid>
        <CircularProgress />
      </Grid>
    </Grid>
  );

  return showGroupFilterSearch ? (
    <Grid container direction="column">
      <Grid>
        <GroupSearch
          id="GroupFilter"
          mode={{
            type: "checkbox",
            onChange: setGroupFilterSearchGroupDetails,
            selections: groupFilterSearchGroupDetails,
            selectButton: {
              disabled: RSET.isEmpty(groupFilterSearchGroupDetails),
              onClick: () => {
                setGroupDetails(groupFilterSearchGroupDetails);
                setShowGroupFilterSearch(false);
              },
            },
            enableMultiSelection: true,
          }}
          strings={languageStrings.groupSearchComponent}
          onCancel={() => {
            setGroupFilterSearchGroupDetails(groupDetails);
            setShowGroupFilterSearch(false);
          }}
          listHeight={listHeight}
          groupFilter={RSET.empty}
          groupFilterEditable={false}
          search={groupSearch}
          showHelpText
        />
      </Grid>
    </Grid>
  ) : (
    <Grid id={genId()} container direction="column" spacing={1}>
      {showHelpText && (
        <Grid>
          <Typography variant="h6" gutterBottom>
            {helpTitle}
          </Typography>
          <Typography variant="body1">{helpDesc}</Typography>
        </Grid>
      )}

      <Grid>{queryBar}</Grid>
      {groupFilterContent()}
      <Grid>{showSpinner ? spinner : itemList()}</Grid>
      <Grid container direction="row">
        <Grid container size={6}>
          {isItemFound && selectAllButton()}
          {isItemFound && clearAllButton()}
        </Grid>
        <Grid container size={6} direction="row" justifyContent="flex-end">
          {selectButtonElement()}
          {cancelButton()}
        </Grid>
      </Grid>
    </Grid>
  );
};

export default BaseSearch;
