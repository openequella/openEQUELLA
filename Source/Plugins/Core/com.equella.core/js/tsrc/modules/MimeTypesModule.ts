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
import * as OEQ from "@openequella/rest-api-client";
import { API_BASE_URL } from "../config";

export const getMIMETypesFromServer = (): Promise<
  OEQ.MimeType.MimeTypeEntry[]
> => OEQ.MimeType.listMimeTypes(API_BASE_URL);

/**
 * Given a MIME Type of the form `<type>/<sub-type>`, validate correct form and then return
 * `type` and `sub-type` in a tuple. `sub-type` will actually also include any attributes, etc. as
 * that's all we need for now (i.e. we're not so interested in sub-types yet).
 *
 * @param mimeType a MIME type of the form `<type>/<sub-type>`
 */
export const splitMimeType = (mimeType: string): [string, string] => {
  const validMimeTypeRegex = /^\w+\/[+.=; \w\-]+$/i;
  if (!validMimeTypeRegex.test(mimeType)) {
    throw new TypeError(
      `Provided string [${mimeType}] is NOT a validly formatted MIME type.`
    );
  }

  return mimeType.split("/", 2) as [string, string];
};

/**
 * Provides a quick check to see if the `mimeType` is a MIME type which can typically be
 * played by the built in browser audio player.
 *
 * @param mimeType A MIME type to be checked
 */
export const isBrowserSupportedAudio = (mimeType: string): boolean =>
  [
    // Standard
    "audio/mpeg",
    "audio/ogg",
    "audio/wav",
    // oEQ standard types
    "audio/x-mp3",
  ].find((supported) => mimeType.startsWith(supported)) !== undefined;

/**
 * Provides a quick check to see if the `mimeType` is a MIME type which can typically be
 * played by the built in browser video player.
 *
 * @param mimeType A MIME type to be checked
 */
export const isBrowserSupportedVideo = (mimeType: string): boolean =>
  ["video/mp4", "video/webm", "video/ogg"].find((supported) =>
    mimeType.startsWith(supported)
  ) !== undefined;
