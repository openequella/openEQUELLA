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
import { Meta, StoryFn } from "@storybook/react";
import * as React from "react";
import { defaultACLEntityResolvers } from "../../../__mocks__/ACLExpressionBuilder.mock";
import {
  everyoneRecipient,
  group100RecipientWithName,
  ipRecipient,
  ownerRecipient,
  referRecipient,
  role100RecipientWithName,
  roleGuestRecipient,
  roleLoggedRecipientWithName,
  ssoMoodleRecipient,
  user100Recipient,
} from "../../../__mocks__/ACLRecipientModule.mock";
import {
  ACLTreeRecipient,
  ACLTreeRecipientProps,
} from "../../../tsrc/components/aclexpressionbuilder/ACLTreeRecipient";
import type { ACLRecipient } from "../../../tsrc/modules/ACLRecipientModule";

export default {
  title: "Component/ACLExpressionBuilder/ACLTreeRecipient",
  component: ACLTreeRecipient,
} as Meta<ACLTreeRecipientProps>;

const recipient = (recipient: ACLRecipient) => (
  <ACLTreeRecipient
    itemId="example"
    recipient={recipient}
    onDelete={() => {}}
    aclEntityResolvers={defaultACLEntityResolvers}
  />
);

export const RecipientUser: StoryFn<ACLTreeRecipientProps> = () =>
  recipient(user100Recipient);

export const RecipientGroup: StoryFn<ACLTreeRecipientProps> = () =>
  recipient(group100RecipientWithName);

export const RecipientRole: StoryFn<ACLTreeRecipientProps> = () =>
  recipient(role100RecipientWithName);

export const RecipientEveryone: StoryFn<ACLTreeRecipientProps> = () =>
  recipient(everyoneRecipient);

export const RecipientOwner: StoryFn<ACLTreeRecipientProps> = () =>
  recipient(ownerRecipient);

export const RecipientLogged: StoryFn<ACLTreeRecipientProps> = () =>
  recipient(roleLoggedRecipientWithName);
export const RecipientGuest: StoryFn<ACLTreeRecipientProps> = () =>
  recipient(roleGuestRecipient);

export const RecipientSso: StoryFn<ACLTreeRecipientProps> = () =>
  recipient(ssoMoodleRecipient);

export const RecipientIp: StoryFn<ACLTreeRecipientProps> = () =>
  recipient(ipRecipient("255.255.255.255/32"));

export const RecipientReferrerContain: StoryFn<ACLTreeRecipientProps> = () =>
  recipient(referRecipient("*edalex*"));

export const RecipientReferrerExact: StoryFn<ACLTreeRecipientProps> = () =>
  recipient(referRecipient("https://edalex.com"));
