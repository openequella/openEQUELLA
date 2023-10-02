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
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import { ACLExpression } from "../../../tsrc/modules/ACLExpressionModule";

/**
 * Helper function to replace all id with expect.any.
 * Because depending on how the expression is processed, the ID of an ACLExpression might be changed.
 */
export const ignoreId = (aclExpression: ACLExpression): ACLExpression =>
  pipe(
    aclExpression.children,
    A.reduce<ACLExpression, ACLExpression[]>([], (acc, child) =>
      pipe(child, ignoreId, (newChild) => [...acc, newChild]),
    ),
    (newChildren) => ({
      ...aclExpression,
      children: newChildren,
      id: expect.any(String),
    }),
  );
