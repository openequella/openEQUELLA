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

import com.tle.common.harvester.MEXHarvesterSettings;

/**
 * Metadata Exchange (MEX) is a protocol developed in Australia to facilitate the harvesting of free
 * digital content, particularly within the context of the National Digital Learning Resource
 * Network (NDLRN). The NDLRN was a national initiative aimed at providing a network of digital
 * learning resources. Unlike LORAX, which was tied specifically to TLF content, MEX was designed
 * for a broader range of free content providers. It standardizes how content providers can make
 * their metadata available to a central system or network, enabling the discovery of diverse
 * educational resources from different sources across the country.
 *
 * <p>Virtually identical to SHEX & LORAXPlugin, differing only in string identifiers
 */
public class MEXPlugin extends AbstractTLFPlugin<MEXHarvesterSettings> {
  public MEXPlugin() {
    super(MEXHarvesterSettings.class);
  }

  @Override
  protected String getPluginsFieldString() {
    return strings.key("mexplugin.settings");
  }
}
