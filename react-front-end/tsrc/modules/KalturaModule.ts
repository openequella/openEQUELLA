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
import * as E from "fp-ts/Either";
import { pipe } from "fp-ts/function";

export interface KalturaPlayerDetails {
  partnerId: number;
  uiconfId: number;
  entryId: string;
}

export const EXTERNAL_ID_PARAM = "externalId";

/**
 * Given an externalId from a Kaltura attachments in the format of `<partnerId>/<uiconfId>/<entryId>`
 * splits it into it its parts and returns a representative object. If there are issues with the
 * format, then a `TypeError` will be thrown.
 *
 * @param externalId an externalId property for Kaltura attachments from the oEQ server
 */
export const parseExternalId = (externalId: string): KalturaPlayerDetails => {
  const result: E.Either<string, KalturaPlayerDetails> = pipe(
    externalId.split("/"),
    E.fromPredicate(
      (xs) => xs.length === 3,
      (xs) => `externalId should have three parts, but has ${xs.length}`
    ),
    E.map(([partnerId, uiconfId, entryId]) => ({
      partnerId: Number.parseInt(partnerId),
      uiconfId: Number.parseInt(uiconfId),
      entryId,
    })),
    E.filterOrElse(
      ({ partnerId }) => Number.isInteger(partnerId),
      () => "partnerId should be a number"
    ),
    E.filterOrElse(
      ({ uiconfId }) => Number.isInteger(uiconfId),
      () => "uiconfId should be a number"
    )
  );

  if (E.isLeft(result)) {
    throw new TypeError(result.left);
  }
  return result.right;
};
