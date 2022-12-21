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
  createStyles,
  FormControl,
  FormControlLabel,
  FormLabel,
  Grid,
  InputLabel,
  makeStyles,
  MenuItem,
  Paper,
  Radio,
  RadioGroup,
  Select,
  Tab,
  Tabs,
  TextField,
  Theme,
  Typography,
} from "@material-ui/core";
import { TabContext, TabPanel } from "@material-ui/lab";
import * as OEQ from "@openequella/rest-api-client";
import { pipe } from "fp-ts/function";
import * as A from "fp-ts/Array";
import * as RA from "fp-ts/ReadonlyArray";
import * as RSET from "fp-ts/ReadonlySet";
import * as React from "react";
import { ChangeEvent, ReactNode, useState } from "react";
import { Literal, Static, Union } from "runtypes";
import {
  ACLExpression,
  addRecipients,
  compactACLExpressions,
  flattenRecipients,
  getACLExpressionById,
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

const {
  aclExpressionBuilder: {
    type: typeLabel,
    homeTab: homeTabLabel,
    otherTab: otherTabLabel,
    otherACLDescriptions: {
      everyone: everyoneDesc,
      owner: ownerDesc,
      logged: loggedDesc,
      guest: guestDesc,
      sso: ssoDesc,
      ip: ipDesc,
      ipPlaceholder,
      referrer: referrerDesc,
      exactReferrer: exactReferrerDesc,
      containReferrer: containReferrerDesc,
    },
    otherACLTypes: {
      everyone: everyoneLabel,
      owner: ownerLabel,
      logged: loggedLabel,
      guest: guestLabel,
      sso: ssoLabel,
      ip: ipLabel,
      referrer: referrerLabel,
    },
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

/**
 * Runtypes definition for ACL expression types in `other` panel.
 */
const OtherACLTypesUnion = Union(
  Literal("Everyone"),
  Literal("Owner"),
  Literal("Logged"),
  Literal("Guest"),
  Literal("Sso"),
  Literal("Ip"),
  Literal("Referrer")
);

type OtherACLType = Static<typeof OtherACLTypesUnion>;

/**
 * Referrer Type.
 * Contain: Match referrers containing this value.
 * Exact: Only match this exact referrer.
 */
const ReferrerTypesUnion = Union(Literal("Contain"), Literal("Exact"));

type ReferrerType = Static<typeof ReferrerTypesUnion>;

const useACLExpressionBuilderStyles = makeStyles((theme: Theme) =>
  createStyles({
    appBar: {
      backgroundColor: "transparent",
      boxShadow: "0px 0px 0px 0px rgb(0 0 0 / 0%)",
    },
    mainContent: {
      height: 600,
    },
    panelWrapper: {
      position: "relative",
      padding: theme.spacing(1),
      display: "flex",
      flexDirection: "column",
      height: "100%",
    },
    paper: {
      flexGrow: 1,
      padding: theme.spacing(2),
      overflowY: "auto",
      overflowX: "hidden",
    },
    tabPanel: {
      padding: 0,
    },
    actionBtn: {
      float: "right",
    },
  })
);

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
  const [activeOtherACLType, setActiveOtherACLType] =
    useState<OtherACLType>("Everyone");
  const [activeReferrerType, setActiveReferrerType] =
    useState<ReferrerType>("Contain");

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
    // if the recipient is already exiting in the currentAclExpression, ignore it.
    const filteredRecipients: ACLRecipient[] = pipe(
      recipients,
      RSET.difference(recipientEq)(flattenRecipients(currentACLExpression)),
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

  const handleACLItemDelete = (nodeID: string) => {
    // TODO: delete ACL item
  };

  const handleOtherAclTypeChanged = (event: ChangeEvent<{ value: unknown }>) =>
    setActiveOtherACLType(OtherACLTypesUnion.check(event.target.value));

  const {
    appBar: appBarClass,
    mainContent: mainContentClass,
    panelWrapper: panelWrapperClass,
    paper: paperClass,
    tabPanel: tabPanelClass,
    actionBtn: actionBtnClass,
  } = useACLExpressionBuilderStyles();

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

  const otherACLPanel = (): JSX.Element => {
    const handleReferrerTypeChanged = (
      event: ChangeEvent<{ value: unknown }>
    ) => setActiveReferrerType(event.target.value as ReferrerType);

    const typographyContent = (label: string) => (
      <Typography>{label}</Typography>
    );

    const GridContent = ({ children }: { children: ReactNode }) => (
      <Grid container direction="column" spacing={1}>
        {children}
      </Grid>
    );

    const selection = (value: string, label: string) => (
      <MenuItem key={value} value={value}>
        {label}
      </MenuItem>
    );

    const bindSelections = OtherACLTypesUnion.match(
      (Everyone) => selection(Everyone, everyoneLabel),
      (Owner) => selection(Owner, ownerLabel),
      (Logged) => selection(Logged, loggedLabel),
      (Guest) => selection(Guest, guestLabel),
      (Sso) => selection(Sso, ssoLabel),
      (Ip) => selection(Ip, ipLabel),
      (Referrer) => selection(Referrer, referrerLabel)
    );

    /**
     * TODO: Get all SSO selects from API
     * */
    const bindControls = OtherACLTypesUnion.match(
      (Everyone) => typographyContent(everyoneDesc),
      (Owner) => typographyContent(ownerDesc),
      (Logged) => typographyContent(loggedDesc),
      (Guest) => typographyContent(guestDesc),
      (Sso) => (
        <GridContent>
          <Grid item>{typographyContent(ssoDesc)}</Grid>
          <Grid item>
            <FormControl>
              <Select displayEmpty>
                <MenuItem value="Moodle">Moodle</MenuItem>
              </Select>
            </FormControl>
          </Grid>
        </GridContent>
      ),
      (Ip) => (
        <GridContent>
          <Grid item>{typographyContent(ipDesc)}</Grid>
          <Grid item>
            <TextField name="ip" label={ipPlaceholder}></TextField>
          </Grid>
        </GridContent>
      ),
      (Referrer) => (
        <GridContent>
          <Grid item>{typographyContent(referrerDesc)}</Grid>
          <Grid item>
            <TextField name="referrer" />
          </Grid>
          <Grid item>
            <RadioGroup
              name="referrer"
              value={activeReferrerType}
              onChange={handleReferrerTypeChanged}
            >
              {ReferrerTypesUnion.alternatives.map((referrerType) => (
                <FormControlLabel
                  key={referrerType.value}
                  value={referrerType.value}
                  control={<Radio />}
                  label={ReferrerTypesUnion.match(
                    (Contain) => containReferrerDesc,
                    (Exact) => exactReferrerDesc
                  )(referrerType.value)}
                />
              ))}
            </RadioGroup>
          </Grid>
        </GridContent>
      )
    );

    return (
      <Grid container direction="column" spacing={1}>
        <Grid item>
          <FormControl variant="outlined">
            <InputLabel>{typeLabel}</InputLabel>
            <Select
              value={activeOtherACLType}
              onChange={handleOtherAclTypeChanged}
              label={typeLabel}
            >
              {OtherACLTypesUnion.alternatives.map((aclType) =>
                bindSelections(aclType.value)
              )}
            </Select>
          </FormControl>
        </Grid>
        {/*The padding aims to align with the next button's text.*/}
        <Grid container style={{ padding: 20 }}>
          {bindControls(activeOtherACLType)}
        </Grid>
        <Grid item>
          <Button color="primary">{languageStrings.common.action.add}</Button>
        </Grid>
      </Grid>
    );
  };

  return (
    <Grid container spacing={2} justifyContent="flex-start">
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
                {otherACLPanel()}
              </TabPanel>
            </Paper>
          </Grid>
          <Grid item xs={6} className={panelWrapperClass}>
            <Paper className={paperClass}>
              <ACLExpressionTree
                aclExpression={currentACLExpression}
                onSelect={setSelectedACLExpression}
                onDelete={handleACLItemDelete}
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
    </Grid>
  );
};

export default ACLExpressionBuilder;
