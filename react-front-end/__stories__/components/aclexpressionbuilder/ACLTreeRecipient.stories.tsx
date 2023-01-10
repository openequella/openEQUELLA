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
import { Meta, Story } from "@storybook/react";
import * as React from "react";
import { LOGGED_IN_USER_ROLE_NAME } from "../../../__mocks__/ACLRecipientModule.mock";
import {
  ACLTreeRecipient,
  ACLTreeRecipientProps,
} from "../../../tsrc/components/aclexpressionbuilder/ACLTreeRecipient";

export default {
  title: "Component/ACLExpressionBuilder/ACLTreeRecipient",
  component: ACLTreeRecipient,
} as Meta<ACLTreeRecipientProps>;

const recipient = (expressionName: string) => (
  <ACLTreeRecipient
    nodeId="example"
    expressionName={expressionName}
    onDelete={() => {}}
  />
);

export const RecipientUser: Story<ACLTreeRecipientProps> = () =>
  recipient("Demonstration Content Admin [democontentadmin]");

export const RecipientGroup: Story<ACLTreeRecipientProps> = () =>
  recipient("INT - Librarians");

export const RecipientRole: Story<ACLTreeRecipientProps> = () =>
  recipient("Teacher Role");

export const RecipientEveryone: Story<ACLTreeRecipientProps> = () =>
  recipient("Everyone");

export const RecipientOwner: Story<ACLTreeRecipientProps> = () =>
  recipient("Owner");

export const RecipientLogged: Story<ACLTreeRecipientProps> = () =>
  recipient(LOGGED_IN_USER_ROLE_NAME);
export const RecipientGuest: Story<ACLTreeRecipientProps> = () =>
  recipient("Guest");

export const RecipientSso: Story<ACLTreeRecipientProps> = () =>
  recipient("Token ID is moodle");

export const RecipientIp: Story<ACLTreeRecipientProps> = () =>
  recipient("From 255.255.255.255/32");

export const RecipientReferrerContain: Story<ACLTreeRecipientProps> = () =>
  recipient("Referred by *edalex*");

export const RecipientReferrerExact: Story<ACLTreeRecipientProps> = () =>
  recipient("Referred by https://edalex.com");
