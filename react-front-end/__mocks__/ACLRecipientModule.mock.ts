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
export const ownerRecipient = {
  expression: "$OWNER",
  type: "$OWNER",
};
export const ownerRecipientRawExpression = "$OWNER";
export const ownerRecipientHumanReadableExpression = "Owner";

export const everyoneRecipient = {
  expression: "*",
  type: "*",
};
export const everyoneRecipientRawExpression = "*";
export const everyoneRecipientHumanReadableExpression = "Everyone";

export const user100Recipient = {
  expression: "20483af2-fe56-4499-a54b-8d7452156895",
  type: "U",
};
export const user100RecipientRawExpression =
  "U:20483af2-fe56-4499-a54b-8d7452156895";
export const user100RecipientHumanReadableExpression =
  "Fabienne Hobson [user100]";

export const user200Recipient = {
  expression: "f9ec8b09-cf64-44ff-8a0a-08a8f2f9272a",
  type: "U",
};

export const user300Recipient = {
  expression: "eb75a832-6533-4d72-93f4-2b7a1b108951",
  type: "U",
};

export const user400Recipient = {
  expression: "1c2ff1d0-9040-4985-a450-0ff6422ba5ef",
  type: "U",
};

export const userAdminRecipient = {
  expression: "75abbd62-d91c-4ce5-b4b5-339e0d44ac0e",
  type: "U",
};

export const userContentAdminRecipient = {
  expression: "2",
  type: "U",
};

export const roleGuestRecipient = {
  expression: "TLE_GUEST_USER_ROLE",
  type: "R",
};
export const roleGuestRecipientRawExpression = "R:TLE_GUEST_USER_ROLE";
export const roleGuestRecipientHumanReadableExpression = "Guest";

export const groupStudentRecipient = {
  expression: "99806ac8-410e-4c60-b3ab-22575276f0f0",
  type: "G",
};
export const groupStudentRecipientRawExpression =
  "G:99806ac8-410e-4c60-b3ab-22575276f0f0";
export const groupStudentRecipientHumanReadableExpression =
  "Engineering & Computer Science Students";

export const groupStaffRecipient = {
  expression: "d0265a33-8f89-4cea-8a36-45fd3c4cf5a1",
  type: "G",
};

export const ssoMoodleRecipient = {
  expression: "moodle",
  type: "T",
};
export const ssoMoodleRecipientRawExpression = "T:moodle";
export const ssoMoodleRecipientHumanReadableExpression = "Token ID is moodle";

// helper function to generate an IP recipient
export const ipRecipient = (ip: string) => ({
  expression: ip,
  type: "I",
});

// helper function to generate a refer recipient
export const referRecipient = (refer: string) => ({
  expression: refer,
  type: "F",
});
