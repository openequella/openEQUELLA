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

/**
 * Builds a URL to access the YouTube provided thumbnail for a video specified with `videoId`. This
 * is based on the URLs return from the Google API in 2021. They could change in the future.
 *
 * `quality` currently seems to correlate to:
 *
 *  - default : 120 x 90
 *  - medium  : 320 x 180
 *  - high    : 480 x 360
 *  - max     : 1280 x 720
 *
 * @param videoId The id of a known YouTube video
 * @param quality The desired quality of the thumbnail
 */
export const buildThumbnailUrl = (
  videoId: string,
  quality: "default" | "medium" | "high" | "max"
): string => {
  const baseUrl = `https://i.ytimg.com/vi/${videoId}/`;
  switch (quality) {
    case "default":
      return baseUrl + "default.jpg";
    case "high":
      return baseUrl + "hqdefault.jpg";
    case "max":
      return baseUrl + "maxresdefault.jpg";
    case "medium":
      return baseUrl + "mqdefault.jpg";
  }
};

export const buildViewUrl = (videoId: string): string =>
  `https://www.youtube.com/watch?v=${videoId}`;

/**
 * Will extract the Video ID from a YouTube view URL - i.e. a URL which has a `v` search param.
 *
 * @param viewUrl a YouTube view URL containing a `v` search param.
 */
export const extractVideoId = (viewUrl: string): string | null =>
  new URL(viewUrl).searchParams.get("v");
