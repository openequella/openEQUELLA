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
import * as CommonCodec from './gen/Common';
import * as SearchCodec from './gen/Search';
import * as WizardControlCodec from './gen/WizardControl';
import * as LtiPlatformCodec from './gen/LtiPlatform';
import * as OidcCodec from './gen/Oidc';
import * as DashboardCodec from './gen/Dashboard';

export * as Acl from './Acl';
export * as AdvancedSearch from './AdvancedSearch';
export * as Auth from './Auth';
export * as BatchOperationResponse from './BatchOperationResponse';
export * as BrowseHierarchy from './BrowseHierarchy';
export * as Collection from './Collection';
export * as Common from './Common';
export * as Drm from './Drm';
export * as Dashboard from './Dashboard';
export * as Errors from './Errors';
export * as FacetedSearchSettings from './FacetedSearchSettings';
export * as Favourite from './Favourite';
export * as Heartbeat from './Heartbeat';
export * as Hierarchy from './Hierarchy';
export * as LegacyContent from './LegacyContent';
export * as LtiPlatform from './LtiPlatform';
export * as MimeType from './MimeType';
export * as Oidc from './Oidc';
export * as RemoteSearch from './RemoteSearch';
export * as Schema from './Schema';
export * as Scrapbook from './Scrapbook';
export * as Search from './Search';
export * as SearchFacets from './SearchFacets';
export * as SearchFilterSettings from './SearchFilterSettings';
export * as SearchSettings from './SearchSettings';
export * as Security from './Security';
export * as Settings from './Settings';
export * as Taxonomy from './Taxonomy';
export * as Theme from './Theme';
export * as Task from './Task';
export * as UserQuery from './UserQuery';
export * as Utils from './Utils';
export * as WizardCommonTypes from './WizardCommonTypes';
export * as WizardControl from './WizardControl';

export const Codec = {
  Common: CommonCodec,
  Dashboard: DashboardCodec,
  LtiPlatform: LtiPlatformCodec,
  Oidc: OidcCodec,
  Search: SearchCodec,
  WizardControl: WizardControlCodec,
};
