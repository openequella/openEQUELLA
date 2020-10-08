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
import { is } from 'typescript-is';
import { GET } from './AxiosInstance';

export interface MimeTypeEntry {
  /**
   * The name of a MIME type.
   */
  mimeType: string;
  /**
   * The description of a MIME type.
   */
  desc?: string;
}

/**
 * Configuration for a MIME type viewer.
 *
 * Based on: com.tle.web.viewurl.ResourceViewerConfig
 */
export interface ViewerConfig {
  /**
   * Whether the viewer should open in a new window.
   */
  openInNewWindow: boolean;
  /**
   * CSS style width specification.
   */
  width: string;
  /**
   * CSS style height specification.
   */
  height: string;
  /**
   * Whether a light-table type viewer should be used - i.e. originally the old
   * [Thickbox](http://codylindley.com/thickbox/) and more recently
   * [Fancybox](http://fancybox.net/).
   */
  thickbox: boolean;
  /**
   * Various freeform configuration for the specific viewer.
   */
  attr: Record<string, unknown>;
}

/**
 * Current list of known oEQ ViewerIds.
 */
export type ViewerId =
  | 'downloadIms'
  | 'echoCenterViewer'
  | 'echoPlayerViewer'
  | 'echoPodcastViewer'
  | 'echoVodcastViewer'
  | 'externalToolViewer'
  | 'fancy'
  | 'file'
  | 'googledocviewer'
  | 'htmlFiveViewer'
  | 'kalturaViewer'
  | 'livNavTreeViewer'
  | 'qtiTestViewer'
  | 'tohtml'
  | 'toimg'
  | 'save';

/**
 * Basic details for each viewer, including optional configuration. If `config` is not present, then
 * the consumer should use it's own defaults.
 */
export interface MimeTypeViewerDetail {
  /**
   * One of the standard oEQ viewer types.
   */
  viewerId: ViewerId;
  /**
   * Standard config (optionally defined) around how this should be displayed.
   */
  config?: ViewerConfig;
}

/**
 * The complete viewer configuration for a MIME type.
 */
export interface MimeTypeViewerConfiguration {
  /**
   * Which viewer should be used by default - e.g. on search result pages.
   */
  defaultViewer: string;
  /**
   * Full list of viewers which are enabled for a MIME type, including any additional custom
   * configuration.
   */
  viewers: MimeTypeViewerDetail[];
}

const isMimeTypeEntryList = (instance: unknown): instance is MimeTypeEntry[] =>
  is<MimeTypeEntry[]>(instance);

const isMimeTypeViewerConfiguration = (
  instance: unknown
): instance is MimeTypeViewerConfiguration =>
  is<MimeTypeViewerConfiguration>(instance);

const MIMETYPE_ROOT_PATH = '/mimetype';

/**
 * List all available MIME types for the institution.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 */
export const listMimeTypes = (apiBasePath: string): Promise<MimeTypeEntry[]> =>
  GET(apiBasePath + MIMETYPE_ROOT_PATH, isMimeTypeEntryList);

/**
 * Given a MIME type (e.g. "application/pdf") retrieve the oEQ viewer configuration for it. Note
 * that configuration details will be brief if there are no additional (non-default) settings defined.
 * It is then up to the consumer to determine their own defaults.
 *
 * @param apiBasePath Base URI to the oEQ institution and API
 * @param mimeType A simple string in the normal format of <type>/<subtype>
 */
export const getViewersForMimeType = (
  apiBasePath: string,
  mimeType: string
): Promise<MimeTypeViewerConfiguration> =>
  GET(
    apiBasePath + MIMETYPE_ROOT_PATH + '/viewerconfig/' + mimeType,
    isMimeTypeViewerConfiguration
  );
