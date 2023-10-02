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
  ACLRecipient,
  GUEST_USER_ROLE_ID,
  LOGGED_IN_USER_ROLE_ID,
} from "../tsrc/modules/ACLRecipientModule";

export const ownerRecipient: ACLRecipient = {
  expression: "$OWNER",
  type: "$OWNER",
};
export const ownerRecipientRawExpression = "$OWNER";
export const ownerRecipientHumanReadableExpression = "Owner";
export const ownerRecipientWithName: ACLRecipient = {
  ...ownerRecipient,
  name: ownerRecipientHumanReadableExpression,
};

export const everyoneRecipient: ACLRecipient = {
  expression: "*",
  type: "*",
};
export const everyoneRecipientRawExpression = "*";
export const everyoneRecipientHumanReadableExpression = "Everyone";
export const everyoneRecipientWithName: ACLRecipient = {
  ...everyoneRecipient,
  name: everyoneRecipientHumanReadableExpression,
};

export const user100Recipient: ACLRecipient = {
  expression: "20483af2-fe56-4499-a54b-8d7452156895",
  type: "U",
};
export const user100RecipientRawExpression =
  "U:20483af2-fe56-4499-a54b-8d7452156895";
export const user100RecipientHumanReadableExpression =
  "Fabienne Hobson [user100]";
export const user100RecipientWithName = {
  ...user100Recipient,
  name: user100RecipientHumanReadableExpression,
};

export const user200Recipient: ACLRecipient = {
  expression: "f9ec8b09-cf64-44ff-8a0a-08a8f2f9272a",
  type: "U",
};
export const user200RecipientWithName: ACLRecipient = {
  ...user200Recipient,
  name: "Racheal Carlyle [user200]",
};

export const user300Recipient: ACLRecipient = {
  expression: "eb75a832-6533-4d72-93f4-2b7a1b108951",
  type: "U",
};
export const user300RecipientWithName = {
  ...user300Recipient,
  name: "Yasmin Day [user300]",
};

export const user400Recipient: ACLRecipient = {
  expression: "1c2ff1d0-9040-4985-a450-0ff6422ba5ef",
  type: "U",
};
export const user400RecipientWithName: ACLRecipient = {
  ...user400Recipient,
  name: "Ronny Southgate [user400]",
};

export const userAdminRecipient: ACLRecipient = {
  expression: "75abbd62-d91c-4ce5-b4b5-339e0d44ac0e",
  type: "U",
};

export const userContentAdminRecipient: ACLRecipient = {
  expression: "2",
  type: "U",
};

export const roleGuestRecipient: ACLRecipient = {
  expression: GUEST_USER_ROLE_ID,
  type: "R",
};
export const roleGuestRecipientRawExpression = `R:${GUEST_USER_ROLE_ID}`;
export const roleGuestRecipientHumanReadableExpression = "Guest User Role";
export const roleGuestRecipientWithName = {
  ...roleGuestRecipient,
  name: roleGuestRecipientHumanReadableExpression,
};

export const LOGGED_IN_USER_ROLE_NAME = "Logged In User Role";
export const roleLoggedRecipientWithName: ACLRecipient = {
  expression: LOGGED_IN_USER_ROLE_ID,
  type: "R",
  name: LOGGED_IN_USER_ROLE_NAME,
};
export const roleLoggedRecipientRawExpression = `R:${LOGGED_IN_USER_ROLE_ID}`;

export const role100RecipientWithName: ACLRecipient = {
  expression: "fda99983-9eda-440a-ac68-0f746173fdcb",
  type: "R",
  name: "role100",
};

export const role200RecipientWithName = {
  expression: "1de3a6df-dc81-4a26-b69e-e61f8474594a",
  type: "R",
  name: "role200",
};

export const groupStudentRecipient: ACLRecipient = {
  expression: "99806ac8-410e-4c60-b3ab-22575276f0f0",
  type: "G",
};
export const groupStudentRecipientRawExpression =
  "G:99806ac8-410e-4c60-b3ab-22575276f0f0";
export const groupStudentRecipientHumanReadableExpression =
  "Engineering & Computer Science Students";

export const groupStaffRecipient: ACLRecipient = {
  expression: "d0265a33-8f89-4cea-8a36-45fd3c4cf5a1",
  type: "G",
};

export const group100RecipientWithName: ACLRecipient = {
  expression: "303e758c-0051-4aea-9a8e-421f93ed9d1a",
  type: "G",
  name: "group100",
};

export const group200RecipientWithName: ACLRecipient = {
  expression: "d7dd1907-5731-4244-9a65-e0e847f68604",
  type: "G",
  name: "group200",
};

export const group300RecipientWithName: ACLRecipient = {
  expression: "f921a6e3-69a6-4ec4-8cf8-bc193beda5f6",
  type: "G",
  name: "group300",
};

export const group400RecipientWithName: ACLRecipient = {
  expression: "a2576dea-bd5c-490b-a065-637068e1a4fb",
  type: "G",
  name: "group400",
};

export const ssoMoodleRecipient: ACLRecipient = {
  expression: "moodle",
  type: "T",
};
export const ssoMoodleRecipientRawExpression = "T:moodle";
export const ssoMoodleRecipientHumanReadableExpression = "Token ID is moodle";
export const ssoMoodleRecipientWithName = {
  ...ssoMoodleRecipient,
  name: ssoMoodleRecipientHumanReadableExpression,
};

// helper function to generate an IP recipient
export const ipRecipient = (ip: string): ACLRecipient => ({
  expression: ip,
  type: "I",
});

// helper function to generate an IP recipient with name
export const ipRecipientWithName = (ip: string): ACLRecipient => ({
  expression: ip,
  type: "I",
  name: `From ${ip}`,
});

// helper function to generate an refer recipient
export const referRecipient = (refer: string): ACLRecipient => ({
  expression: refer,
  type: "F",
});

// helper function to generate a referrer recipient with name
export const referRecipientWithName = (refer: string): ACLRecipient => ({
  ...referRecipient(refer),
  name: `From ${decodeURIComponent(refer)}`,
});
