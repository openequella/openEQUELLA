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
  FormControl,
  Grid,
  InputLabel,
  MenuItem,
  Select,
  SelectChangeEvent,
  Typography,
} from "@mui/material";
import * as E from "fp-ts/Either";
import { flow, identity, pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as TE from "fp-ts/TaskEither";
import * as React from "react";
import { ReactNode, useCallback, useState } from "react";
import { Literal, Static, Union } from "runtypes";
import { ACLEntityResolvers } from "../../modules/ACLEntityModule";
import {
  ACLRecipient,
  ACLRecipientTypes,
  createACLRecipient,
  GUEST_USER_ROLE_ID,
  LOGGED_IN_USER_ROLE_ID,
  showRecipientHumanReadable,
} from "../../modules/ACLRecipientModule";
import { findGroupById } from "../../modules/GroupModule";
import { findRoleById } from "../../modules/RoleModule";
import { findUserById } from "../../modules/UserModule";
import { languageStrings } from "../../util/langstrings";
import IPv4CIDRInput from "../IPv4CIDRInput";
import ACLHTTPReferrerInput from "./ACLHTTPReferrerInput";
import ACLSSOMenu from "./ACLSSOMenu";

const {
  aclExpressionBuilder: {
    type: typeLabel,
    otherACLDescriptions: {
      everyone: everyoneDesc,
      owner: ownerDesc,
      logged: loggedDesc,
      guest: guestDesc,
      sso: ssoDesc,
      ip: ipDesc,
      referrer: referrerDesc,
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
 * Runtypes definition for ACL expression types in `other` panel.
 */
const OtherACLTypesUnion = Union(
  Literal("Everyone"),
  Literal("Owner"),
  Literal("Logged"),
  Literal("Guest"),
  Literal("SSO"),
  Literal("IP"),
  Literal("Referrer")
);
type OtherACLType = Static<typeof OtherACLTypesUnion>;

export interface ACLOtherPanelProps {
  /**
   * Handler for `add` button.
   * Triggered only if user input or selection can be formed as a valid recipient.
   */
  onAdd: (recipient: ACLRecipient) => void;
  /**
   * An object includes functions used to replace the default `findGroupById`, `findUserById`, `findRoleById` resolvers.
   */
  aclEntityResolvers?: ACLEntityResolvers;
  /**
   * Function to provide a list of SSO tokens.
   */
  ssoTokensProvider?: () => Promise<string[]>;
}

/**
 ** It provides an `other` panel for `ACLExpressionBuilder` to use,
 *  It includes special ACL permissions like Everyone, Guest, Owner, Logged user, SSO, IP and referrer.
 */
const ACLOtherPanel = ({
  onAdd,
  aclEntityResolvers = {
    resolveGroupProvider: findGroupById,
    resolveUserProvider: findUserById,
    resolveRoleProvider: findRoleById,
  },
  ssoTokensProvider,
}: ACLOtherPanelProps) => {
  const [activeACLType, setActiveACLType] = useState<OtherACLType>("Everyone");

  const [httpReferrer, setHTTPReferrer] = useState("");

  const [ipAddress, setIPAddress] = useState("");

  const [ssoToken, setSSOToken] = useState("");

  const handleAclTypeChanged = (event: SelectChangeEvent<OtherACLType>) =>
    setActiveACLType(OtherACLTypesUnion.check(event.target.value));

  const handleAddButtonClicked = async () => {
    // generate raw ACL expression string for corresponding recipient type
    const generateExpression = OtherACLTypesUnion.match(
      (everyone) => ACLRecipientTypes.Everyone,
      (owner) => ACLRecipientTypes.Owner,
      (logged) => `${ACLRecipientTypes.Role}:${LOGGED_IN_USER_ROLE_ID}`,
      (guest) => `${ACLRecipientTypes.Role}:${GUEST_USER_ROLE_ID}`,
      (sso) => (ssoToken ? `${ACLRecipientTypes.Sso}:${ssoToken}` : undefined),
      (ip) => (ipAddress ? `${ACLRecipientTypes.Ip}:${ipAddress}` : undefined),
      (referrer) =>
        httpReferrer ? `${ACLRecipientTypes.Refer}:${httpReferrer}` : undefined
    );

    // create an ACLRecipient object
    const optionRecipient: O.Option<ACLRecipient> = pipe(
      activeACLType,
      generateExpression,
      O.fromNullable,
      O.chain(flow(createACLRecipient, O.fromEither))
    );

    // fetch the human-readable name
    const nameTask: TE.TaskEither<string, string> = pipe(
      optionRecipient,
      O.map(showRecipientHumanReadable(aclEntityResolvers)),
      O.getOrElse(() => TE.left(""))
    );

    const name = pipe(await nameTask(), E.getOrElse(identity));

    // update name for ACLRecipient and add it to the ACLExpression
    pipe(
      optionRecipient,
      O.map((r: ACLRecipient) => ({
        ...r,
        name,
      })),
      O.map(onAdd)
    );
  };

  const selection = (value: string, label: string) => (
    <MenuItem key={value} value={value}>
      {label}
    </MenuItem>
  );

  const buildSelections = OtherACLTypesUnion.match(
    (Everyone) => selection(Everyone, everyoneLabel),
    (Owner) => selection(Owner, ownerLabel),
    (Logged) => selection(Logged, loggedLabel),
    (Guest) => selection(Guest, guestLabel),
    (Sso) => selection(Sso, ssoLabel),
    (Ip) => selection(Ip, ipLabel),
    (Referrer) => selection(Referrer, referrerLabel)
  );

  const OtherControl = ({
    title,
    children,
  }: {
    title: string;
    children?: ReactNode;
  }) => (
    <Grid container direction="column" rowSpacing={2}>
      <Grid item>
        <Typography>{title}</Typography>
      </Grid>

      {children && <Grid item>{children}</Grid>}
    </Grid>
  );

  /**
   * TODO: Get all SSO selects from API
   * */
  const buildControls = useCallback(
    () =>
      pipe(
        activeACLType,
        OtherACLTypesUnion.match(
          (Everyone) => <OtherControl title={everyoneDesc} />,
          (Owner) => <OtherControl title={ownerDesc} />,
          (Logged) => <OtherControl title={loggedDesc} />,
          (Guest) => <OtherControl title={guestDesc} />,
          (Sso) => (
            <OtherControl title={ssoDesc}>
              <ACLSSOMenu
                getSSOTokens={ssoTokensProvider}
                onChange={setSSOToken}
              />
            </OtherControl>
          ),
          (Ip) => (
            <OtherControl title={ipDesc}>
              <IPv4CIDRInput onChange={setIPAddress} />
            </OtherControl>
          ),
          (Referrer) => (
            <OtherControl title={referrerDesc}>
              <ACLHTTPReferrerInput onChange={setHTTPReferrer} />
            </OtherControl>
          )
        )
      ),
    [activeACLType, ssoTokensProvider]
  );

  return (
    <Grid container direction="column" spacing={1}>
      <Grid item>
        <FormControl variant="outlined">
          <InputLabel>{typeLabel}</InputLabel>
          <Select
            id="recipient-type-select"
            value={activeACLType}
            onChange={handleAclTypeChanged}
            label={typeLabel}
          >
            {OtherACLTypesUnion.alternatives.map((aclType) =>
              buildSelections(aclType.value)
            )}
          </Select>
        </FormControl>
      </Grid>
      {/*The padding aims to align with the following `add` button's text.*/}
      <Grid container style={{ padding: 25 }}>
        {buildControls()}
      </Grid>
      <Grid item>
        <Button color="primary" onClick={handleAddButtonClicked}>
          {languageStrings.common.action.add}
        </Button>
      </Grid>
    </Grid>
  );
};

export default ACLOtherPanel;
