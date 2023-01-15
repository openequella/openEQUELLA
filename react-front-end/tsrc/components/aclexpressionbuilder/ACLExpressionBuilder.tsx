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
  AppBar,
  Button,
  FormControl,
  FormControlLabel,
  FormLabel,
  Grid,
  Paper,
  Radio,
  RadioGroup,
  Tab,
  Tabs,
} from "@mui/material";
import { TabContext, TabPanel } from "@mui/lab";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as RA from "fp-ts/ReadonlyArray";
import * as RSET from "fp-ts/ReadonlySet";
import * as React from "react";
import { ChangeEvent, useState } from "react";
import { Literal, Static, Union } from "runtypes";
import { ACLEntityResolvers } from "../../modules/ACLEntityModule";
import {
  ACLExpression,
  addRecipients,
  compactACLExpressions,
  flattenRecipients,
  getACLExpressionById,
  removeACLExpression,
  replaceACLExpression,
  revertCompactedACLExpressions,
} from "../../modules/ACLExpressionModule";
import {
  ACLRecipient,
  groupToRecipient,
  recipientEq,
  recipientOrd,
  roleToRecipient,
  userToRecipient,
} from "../../modules/ACLRecipientModule";
import { listUsers } from "../../modules/UserModule";
import { languageStrings } from "../../util/langstrings";
import GroupSearch from "../securityentitysearch/GroupSearch";
import RoleSearch from "../securityentitysearch/RoleSearch";
import UserSearch from "../securityentitysearch/UserSearch";
import ACLExpressionTree from "./ACLExpressionTree";
import ACLOtherPanel from "./ACLOtherPanel";
import { styled } from "@mui/material/styles";

const {
  aclExpressionBuilder: {
    type: typeLabel,
    homeTab: homeTabLabel,
    otherTab: otherTabLabel,
  },
} = languageStrings;

/**
 * Runtypes definition for home panel search filter type.
 */
const SearchFilterTypesUnion = Union(
  Literal("Users"),
  Literal("Groups"),
  Literal("Roles")
);

type SearchFilterType = Static<typeof SearchFilterTypesUnion>;

const PREFIX = "ACLExpressionBuilder";
const classes = {
  appBar: `${PREFIX}-appBar`,
  mainContent: `${PREFIX}-mainContent`,
  panelWrapper: `${PREFIX}-panelWrapper`,
  paper: `${PREFIX}-paper`,
  tabPanel: `${PREFIX}-tabPanel`,
  actionBtn: `${PREFIX}-actionBtn`,
};

const StyledGrid = styled(Grid)(({ theme }) => ({
  [`& .${classes.appBar}`]: {
    backgroundColor: "transparent",
    boxShadow: "0px 0px 0px 0px rgb(0 0 0 / 0%)",
  },
  [`& .${classes.mainContent}`]: {
    height: 600,
  },
  [`& .${classes.panelWrapper}`]: {
    position: "relative",
    padding: theme.spacing(1),
    display: "flex",
    flexDirection: "column",
    height: "100%",
  },
  [`& .${classes.paper}`]: {
    flexGrow: 1,
    padding: theme.spacing(2),
    overflowY: "auto",
    overflowX: "hidden",
  },
  [`& .${classes.tabPanel}`]: {
    padding: 0,
  },
  [`& .${classes.actionBtn}`]: {
    float: "right",
  },
}));

export interface ACLExpressionBuilderProps {
  /** Handler for when user click OK button. */
  onFinish: (aclExpression: ACLExpression) => void;
  /**
   *  It could be empty like a portlet hasn't been assigning ACL rules,
   *  user can use this builder to create an ACLExpression.
   */
  aclExpression?: ACLExpression;
  /**
   * Function used to replace the default `search` prop for `UserSearch` component.
   */
  searchUserProvider?: (
    query?: string,
    filter?: ReadonlySet<string>
  ) => Promise<OEQ.UserQuery.UserDetails[]>;
  /**
   * Function used to replace the default `search` prop for `GroupSearch` component.
   */
  searchGroupProvider?: (
    query?: string,
    filter?: ReadonlySet<string>
  ) => Promise<OEQ.UserQuery.GroupDetails[]>;
  /**
   * Function used to replace the default `search` prop for `RoleSearch` component.
   */
  searchRoleProvider?: (query?: string) => Promise<OEQ.UserQuery.RoleDetails[]>;
  /**
   * Function used to replace the default `resolveGroupsProvider` prop for `UserSearch` and `GroupSearch` component.
   */
  resolveGroupsProvider?: (
    ids: ReadonlyArray<string>
  ) => Promise<OEQ.UserQuery.GroupDetails[]>;
  /**
   * Object includes functions used to replace default acl entity resolvers for `ACLOtherPanel`
   */
  aclEntityResolversProvider?: ACLEntityResolvers;
}

/**
 ** It provides a `home` panel that can search for `users` `groups` and `roles`,
 *  and an `other` panel that includes special ACL permissions like SSO, IP and referrer.
 *  At the same time, it also renders an ACL tree so that the user can clearly see the content of the current ACL settings.
 */
const ACLExpressionBuilder = ({
  onFinish,
  aclExpression,
  searchUserProvider = listUsers,
  searchGroupProvider,
  searchRoleProvider,
  resolveGroupsProvider,
  aclEntityResolversProvider,
}: ACLExpressionBuilderProps): JSX.Element => {
  const [currentACLExpression, setCurrentACLExpression] =
    useState<ACLExpression>(
      aclExpression
        ? compactACLExpressions(aclExpression)
        : {
            id: "default-acl-expression-id",
            operator: "OR",
            recipients: [],
            children: [],
          }
    );

  const [activeTabValue, setActiveTabValue] = useState(homeTabLabel);

  const [activeSearchFilterType, setActiveSearchFilterType] =
    useState<SearchFilterType>("Users");

  const [userSelections, setUserSelections] = useState<
    ReadonlySet<OEQ.UserQuery.UserDetails>
  >(RSET.empty);
  const [groupSelections, setGroupSelections] = useState<
    ReadonlySet<OEQ.UserQuery.GroupDetails>
  >(RSET.empty);
  const [roleSelections, setRoleSelections] = useState<
    ReadonlySet<OEQ.UserQuery.RoleDetails>
  >(RSET.empty);

  const [selectedACLExpression, setSelectedACLExpression] =
    useState<ACLExpression>(currentACLExpression);

  const handleSearchFilterChange = (event: ChangeEvent<HTMLInputElement>) =>
    setActiveSearchFilterType(SearchFilterTypesUnion.check(event.target.value));

  const handleTabChanged = (_: ChangeEvent<{}>, newValue: string) =>
    setActiveTabValue(newValue);

  // Update an ACLExpression when the expression has changes like operator changed and recipient deleted.
  const updateACLExpressionRelatedStates = (
    updatedACLExpression: ACLExpression
  ) => {
    // get new selected ACLExpression from new current ACLExpression by id and then update the state
    const updateSelectedACLExpression = (aclExpression: ACLExpression) =>
      pipe(
        aclExpression,
        getACLExpressionById(selectedACLExpression.id),
        (newSelectedACL) =>
          setSelectedACLExpression(newSelectedACL ?? aclExpression)
      );

    const newCurrentACLExpression = pipe(
      currentACLExpression,
      replaceACLExpression(updatedACLExpression)
    );

    pipe(
      [setCurrentACLExpression, updateSelectedACLExpression],
      A.flap(newCurrentACLExpression)
    );
  };

  // add new selected recipients into selected ACLExpression node
  const updateNewRecipients = (recipients: ReadonlySet<ACLRecipient>) => {
    const existingRecipients = pipe(
      currentACLExpression,
      flattenRecipients,
      RSET.fromReadonlyArray(recipientEq)
    );

    // if the recipient is already exiting in the currentAclExpression, ignore it.
    const filteredRecipients: ACLRecipient[] = pipe(
      recipients,
      RSET.difference(recipientEq)(existingRecipients),
      RSET.toReadonlyArray(recipientOrd),
      RA.toArray
    );

    pipe(
      selectedACLExpression,
      addRecipients(filteredRecipients),
      updateACLExpressionRelatedStates
    );
  };

  const handleEntitySelected = <T,>(
    selections: ReadonlySet<T>,
    entityToReceipt: (entity: T) => ACLRecipient
  ) =>
    pipe(
      selections,
      RSET.map(recipientEq)(entityToReceipt),
      updateNewRecipients
    );

  const handleACLExpressionTreeChanged = (
    changedACLExpression: ACLExpression
  ) =>
    pipe(
      currentACLExpression,
      replaceACLExpression(changedACLExpression),
      updateACLExpressionRelatedStates
    );

  const handleACLExpressionDelete = (deleteACLExpression: ACLExpression) =>
    pipe(
      currentACLExpression,
      removeACLExpression(deleteACLExpression.id),
      // in theory root ACL expression (currentACLExpression) can't be deleted by user
      // and the result should never be none after removeACLExpression.
      O.getOrElse(() => currentACLExpression),
      updateACLExpressionRelatedStates
    );

  const handleACLOtherPanelAdded = (recipient: ACLRecipient) =>
    pipe(recipient, RSET.singleton, updateNewRecipients);

  const {
    appBar: appBarClass,
    mainContent: mainContentClass,
    panelWrapper: panelWrapperClass,
    paper: paperClass,
    tabPanel: tabPanelClass,
    actionBtn: actionBtnClass,
  } = classes;

  const homeACLPanel = () => {
    const sharedProps = {
      listHeight: 300,
      groupFilterEditable: true,
      groupSearch: searchGroupProvider,
      resolveGroupsProvider: resolveGroupsProvider,
      enableMultiSelection: true,
    };

    return (
      <FormControl fullWidth component="fieldset">
        <Grid spacing={4} container direction="row" alignItems="center">
          <Grid item>
            <FormLabel>{typeLabel}</FormLabel>
          </Grid>
          <Grid item>
            <RadioGroup
              row
              name="searchFilterType"
              value={activeSearchFilterType}
              onChange={handleSearchFilterChange}
            >
              {SearchFilterTypesUnion.alternatives.map((searchType) => (
                <FormControlLabel
                  key={searchType.value}
                  value={searchType.value}
                  control={<Radio />}
                  label={searchType.value}
                />
              ))}
            </RadioGroup>
          </Grid>
        </Grid>
        {pipe(
          activeSearchFilterType,
          SearchFilterTypesUnion.match(
            (Users) => (
              <UserSearch
                key={Users}
                {...sharedProps}
                search={searchUserProvider}
                selections={userSelections}
                onChange={setUserSelections}
                onClearAll={setUserSelections}
                onSelectAll={setUserSelections}
                onAdd={(user) =>
                  handleEntitySelected(RSET.singleton(user), userToRecipient)
                }
                selectButton={{
                  onClick: () =>
                    handleEntitySelected(userSelections, userToRecipient),
                }}
              />
            ),
            (Groups) => (
              <GroupSearch
                key={Groups}
                {...sharedProps}
                search={searchGroupProvider}
                selections={groupSelections}
                onChange={setGroupSelections}
                onClearAll={setGroupSelections}
                onSelectAll={setGroupSelections}
                onAdd={(group) =>
                  handleEntitySelected(RSET.singleton(group), groupToRecipient)
                }
                selectButton={{
                  onClick: () =>
                    handleEntitySelected(groupSelections, groupToRecipient),
                }}
              />
            ),
            (Roles) => (
              <RoleSearch
                key={Roles}
                {...sharedProps}
                search={searchRoleProvider}
                selections={roleSelections}
                onChange={setRoleSelections}
                onClearAll={setRoleSelections}
                onSelectAll={setRoleSelections}
                onAdd={(role) =>
                  handleEntitySelected(RSET.singleton(role), roleToRecipient)
                }
                listHeight={367}
                groupFilterEditable={false}
                selectButton={{
                  onClick: () =>
                    handleEntitySelected(roleSelections, roleToRecipient),
                }}
              />
            )
          )
        )}
      </FormControl>
    );
  };

  return (
    <StyledGrid spacing={2} container justifyContent="flex-start">
      <TabContext value={activeTabValue}>
        <Grid item>
          <AppBar position="static" color="default" className={appBarClass}>
            <Tabs value={activeTabValue} onChange={handleTabChanged}>
              <Tab label={homeTabLabel} value={homeTabLabel} />
              <Tab label={otherTabLabel} value={otherTabLabel} />
            </Tabs>
          </AppBar>
        </Grid>
        <Grid container className={mainContentClass}>
          <Grid item xs={6} className={panelWrapperClass}>
            <Paper className={paperClass}>
              <TabPanel className={tabPanelClass} value={homeTabLabel}>
                {homeACLPanel()}
              </TabPanel>
              <TabPanel className={tabPanelClass} value={otherTabLabel}>
                <ACLOtherPanel
                  onAdd={handleACLOtherPanelAdded}
                  aclEntityResolvers={aclEntityResolversProvider}
                />
              </TabPanel>
            </Paper>
          </Grid>
          <Grid item xs={6} className={panelWrapperClass}>
            <Paper className={paperClass}>
              <ACLExpressionTree
                aclExpression={currentACLExpression}
                onSelect={setSelectedACLExpression}
                onDelete={handleACLExpressionDelete}
                onChange={handleACLExpressionTreeChanged}
              />
            </Paper>
          </Grid>
        </Grid>
        <Grid item xs={12}>
          <Button
            variant="contained"
            color="primary"
            className={actionBtnClass}
            onClick={() =>
              pipe(
                currentACLExpression,
                revertCompactedACLExpressions,
                onFinish
              )
            }
          >
            {languageStrings.common.action.ok}
          </Button>
        </Grid>
      </TabContext>
    </StyledGrid>
  );
};

export default ACLExpressionBuilder;
