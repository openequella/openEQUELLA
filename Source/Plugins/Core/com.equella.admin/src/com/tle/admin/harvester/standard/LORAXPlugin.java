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

package com.tle.admin.harvester.standard;

import com.tle.common.harvester.LORAXHarvesterSettings;

/**
 * LORAX is a protocol for harvesting metadata, primarily used within the Australian and New Zealand
 * education sectors. It was created to facilitate the exchange of learning resources. LORAX
 * provides a standardized way for systems to communicate and share information about digital
 * learning objects, such as lessons, videos, and interactive activities.
 *
 * <p>Virtually identical to SHEX & MEXPlugin, differing only in string identifiers
 */
public class LORAXPlugin extends AbstractTLFPlugin<LORAXHarvesterSettings> {
  public LORAXPlugin() {
    super(LORAXHarvesterSettings.class);
  }

  @Override
  protected String getPluginsFieldString() {
    return loraxPluginStrings.key("settings");
  }
}
