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
import { TabContext, TabPanel } from "@mui/lab";
import { AppBar, Button, Grid, Paper, Tab, Tabs } from "@mui/material";
import { styled } from "@mui/material/styles";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import { constant, flow, identity, pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as S from "fp-ts/string";
import * as E from "fp-ts/Either";
import * as RA from "fp-ts/ReadonlyArray";
import * as RSET from "fp-ts/ReadonlySet";
import * as React from "react";
import { ChangeEvent, useState } from "react";
import { ACLEntityResolvers } from "../../modules/ACLEntityModule";
import {
  parse,
  ACLExpression,
  addRecipients,
  compactACLExpressions,
  flattenRecipients,
  getACLExpressionById,
  removeACLExpression,
  replaceACLExpression,
  revertCompactedACLExpressions,
  generate,
  removeRedundantExpressions,
} from "../../modules/ACLExpressionModule";
import {
  ACLRecipient,
  ACLRecipientTypes,
  recipientEq,
  recipientOrd,
} from "../../modules/ACLRecipientModule";
import { listUsers } from "../../modules/UserModule";
import { languageStrings } from "../../util/langstrings";
import { pfTernary } from "../../util/pointfree";
import ACLExpressionTree from "./ACLExpressionTree";
import ACLHomePanel from "./ACLHomePanel";
import ACLOtherPanel from "./ACLOtherPanel";

const {
  aclExpressionBuilder: { homeTab: homeTabLabel, otherTab: otherTabLabel },
} = languageStrings;

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
    height: 660,
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
  onFinish: (aclExpression: string) => void;
  /**
   *  It could be empty like a portlet hasn't been assigning ACL rules,
   *  user can use this builder to build ACL rules.
   */
  aclExpression?: string;
  /**
   * Function used to replace the default `search` prop for `UserSearch` component.
   */
  searchUserProvider?: (
    query?: string,
    filter?: ReadonlySet<string>,
  ) => Promise<OEQ.UserQuery.UserDetails[]>;
  /**
   * Function used to replace the default `search` prop for `GroupSearch` component.
   */
  searchGroupProvider?: (
    query?: string,
    filter?: ReadonlySet<string>,
  ) => Promise<OEQ.UserQuery.GroupDetails[]>;
  /**
   * Function used to replace the default `search` prop for `RoleSearch` component.
   */
  searchRoleProvider?: (query?: string) => Promise<OEQ.UserQuery.RoleDetails[]>;
  /**
   * Function used to replace the default `resolveGroupsProvider` prop for `UserSearch` and `GroupSearch` component.
   */
  resolveGroupsProvider?: (
    ids: ReadonlySet<string>,
  ) => Promise<OEQ.UserQuery.GroupDetails[]>;
  /**
   * Object includes functions used to replace default acl entity resolvers for `ACLOtherPanel`
   */
  aclEntityResolversProvider?: ACLEntityResolvers;
  /**
   * Function used to replace default `ssoTokensProvider` for `ACLOtherPanel`.
   */
  ssoTokensProvider?: () => Promise<string[]>;
}

export const DEFAULT_ACL_EXPRESSION_ID = "default-acl-expression-id";

const defaultACLExpression: ACLExpression = {
  id: DEFAULT_ACL_EXPRESSION_ID,
  operator: "OR",
  recipients: [],
  children: [],
};

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
  ssoTokensProvider,
}: ACLExpressionBuilderProps): JSX.Element => {
  const parseACLExpression = flow(
    parse,
    E.map(compactACLExpressions),
    E.mapLeft((err) =>
      console.warn(`Set ACLExpression with default value because: ${err}`),
    ),
    E.getOrElse(() => defaultACLExpression),
  );

  const [currentACLExpression, setCurrentACLExpression] =
    useState<ACLExpression>(
      aclExpression ? parseACLExpression(aclExpression) : defaultACLExpression,
    );

  const [activeTabValue, setActiveTabValue] = useState(homeTabLabel);

  const [selectedACLExpression, setSelectedACLExpression] =
    useState<ACLExpression>(currentACLExpression);

  const handleTabChanged = (_: ChangeEvent<object>, newValue: string) =>
    setActiveTabValue(newValue);

  // Update an ACLExpression when the expression has changes like operator changed and recipient deleted.
  const updateACLExpressionRelatedStates = (
    updatedACLExpression: ACLExpression,
  ) => {
    // get new selected ACLExpression from new current ACLExpression by id and then update the state
    const updateSelectedACLExpression = (aclExpression: ACLExpression) =>
      pipe(
        aclExpression,
        getACLExpressionById(selectedACLExpression.id),
        (newSelectedACL) =>
          setSelectedACLExpression(newSelectedACL ?? aclExpression),
      );

    const newCurrentACLExpression = pipe(
      currentACLExpression,
      replaceACLExpression(updatedACLExpression),
    );

    pipe(
      [setCurrentACLExpression, updateSelectedACLExpression],
      A.flap(newCurrentACLExpression),
    );
  };

  // add new selected recipients into selected ACLExpression node
  const updateNewRecipients = (recipients: ReadonlySet<ACLRecipient>) => {
    const existingRecipients = pipe(
      currentACLExpression,
      flattenRecipients,
      RSET.fromReadonlyArray(recipientEq),
    );

    // if the recipient is already exiting in the currentAclExpression, ignore it.
    const filteredRecipients: ACLRecipient[] = pipe(
      recipients,
      RSET.difference(recipientEq)(existingRecipients),
      RSET.toReadonlyArray(recipientOrd),
      RA.toArray,
    );

    pipe(
      selectedACLExpression,
      addRecipients(filteredRecipients),
      updateACLExpressionRelatedStates,
    );
  };

  const handleACLExpressionTreeChanged = (
    changedACLExpression: ACLExpression,
  ) =>
    pipe(
      currentACLExpression,
      replaceACLExpression(changedACLExpression),
      updateACLExpressionRelatedStates,
    );

  const handleACLExpressionDelete = (deleteACLExpression: ACLExpression) =>
    pipe(
      currentACLExpression,
      removeACLExpression(deleteACLExpression.id),
      // in theory root ACL expression (currentACLExpression) can't be deleted by user
      // and the result should never be none after removeACLExpression.
      O.getOrElse(() => currentACLExpression),
      updateACLExpressionRelatedStates,
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

  return (
    <StyledGrid
      spacing={2}
      container
      justifyContent="flex-start"
      direction="column"
    >
      <TabContext value={activeTabValue}>
        <Grid>
          <AppBar position="static" color="default" className={appBarClass}>
            <Tabs value={activeTabValue} onChange={handleTabChanged}>
              <Tab label={homeTabLabel} value={homeTabLabel} />
              <Tab label={otherTabLabel} value={otherTabLabel} />
            </Tabs>
          </AppBar>
        </Grid>
        <Grid container className={mainContentClass}>
          <Grid size={6} className={panelWrapperClass}>
            <Paper className={paperClass}>
              <TabPanel className={tabPanelClass} value={homeTabLabel}>
                <ACLHomePanel
                  onAdd={updateNewRecipients}
                  resolveGroupsProvider={resolveGroupsProvider}
                  searchGroupProvider={searchGroupProvider}
                  searchRoleProvider={searchRoleProvider}
                  searchUserProvider={searchUserProvider}
                />
              </TabPanel>
              <TabPanel className={tabPanelClass} value={otherTabLabel}>
                <ACLOtherPanel
                  onAdd={handleACLOtherPanelAdded}
                  aclEntityResolvers={aclEntityResolversProvider}
                  ssoTokensProvider={ssoTokensProvider}
                />
              </TabPanel>
            </Paper>
          </Grid>
          <Grid size={6} className={panelWrapperClass}>
            <Paper className={paperClass}>
              <ACLExpressionTree
                aclExpression={currentACLExpression}
                onSelect={setSelectedACLExpression}
                onDelete={handleACLExpressionDelete}
                onChange={handleACLExpressionTreeChanged}
                aclEntityResolvers={aclEntityResolversProvider}
              />
            </Paper>
          </Grid>
        </Grid>
        <Grid>
          <Button
            variant="contained"
            color="primary"
            className={actionBtnClass}
            onClick={() =>
              pipe(
                currentACLExpression,
                revertCompactedACLExpressions,
                removeRedundantExpressions,
                generate,
                // if all recipients have been deleted, the ACL string will be an empty,
                // then return `Everyone` as a default value.
                pfTernary(
                  S.isEmpty,
                  constant(ACLRecipientTypes.Everyone),
                  identity,
                ),
                onFinish,
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
