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
  Grid,
  Button,
  AppBar,
  Tabs,
  Tab,
  TextField,
  MenuItem,
  Select,
  FormControl,
  FormLabel,
  RadioGroup,
  FormControlLabel,
  Radio,
  InputLabel,
  Typography,
  Paper,
  Theme,
  makeStyles,
  createStyles,
} from "@material-ui/core";
import { TabContext, TabPanel } from "@material-ui/lab";
import * as React from "react";
import { useState, ChangeEvent, ReactNode } from "react";
import { Union, Static, Literal } from "runtypes";
import { languageStrings } from "../../util/langstrings";
import UserSearch from "../UserSearch";
import { ACLExpression } from "./ACLExpressionHelper";
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
      height: 500,
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
  /**
   *  It could be empty like a portlet hasn't been assigning ACL rules,
   *  user can use this builder to create an ACLExpression.
   */
  aclExpression?: ACLExpression;
}

/**
 ** It provides a `home` panel that can search for `users` `groups` and `roles`,
 *  and an `other` panel that includes special ACL permissions like SSO, IP and referrer.
 *  At the same time, it also renders an ACL tree so that the user can clearly see the content of the current ACL settings.
 */
const ACLExpressionBuilder = ({
  aclExpression,
}: ACLExpressionBuilderProps): JSX.Element => {
  const [currentAclExpression] = useState<ACLExpression>(
    aclExpression ?? { id: "1", operator: "OR", recipients: [], children: [] }
  );
  const [activeTabValue, setActiveTabValue] = useState(homeTabLabel);
  const [activeSearchFilterType, setActiveSearchFilterType] =
    useState<SearchFilterType>("Users");
  const [activeOtherACLType, setActiveOtherACLType] =
    useState<OtherACLType>("Everyone");
  const [activeReferrerType, setActiveReferrerType] =
    useState<ReferrerType>("Contain");

  const handleSearchFilterChange = (event: ChangeEvent<HTMLInputElement>) =>
    setActiveSearchFilterType(SearchFilterTypesUnion.check(event.target.value));

  const handleTabChanged = (_: ChangeEvent<{}>, newValue: string) =>
    setActiveTabValue(newValue);

  const handleACLItemSelected = (nodeID: string) => {
    // TODO: handle ACL item selected
  };

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

  const homeACLPanel = () => (
    <FormControl component="fieldset">
      <FormLabel component="legend">{typeLabel}</FormLabel>
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
      <UserSearch onSelect={() => {}} listHeight={300} />
    </FormControl>
  );

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
                aclExpression={currentAclExpression}
                onSelect={handleACLItemSelected}
                onDelete={handleACLItemDelete}
              />
            </Paper>
          </Grid>
        </Grid>
        <Grid item xs={12}>
          <Button
            variant="contained"
            color="primary"
            className={actionBtnClass}
          >
            {languageStrings.common.action.ok}
          </Button>
        </Grid>
      </TabContext>
    </Grid>
  );
};

export default ACLExpressionBuilder;
