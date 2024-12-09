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

package com.tle.common.scripting.objects;

import com.tle.common.scripting.ScriptObject;
import com.tle.common.scripting.types.DrmPartyScriptType;
import com.tle.common.scripting.types.DrmSettingsScriptType;

/**
 * Referenced by the 'drm' variable in script.
 *
 * @author aholland
 */
@SuppressWarnings("nls")
public interface DrmScriptObject extends ScriptObject {
  String DEFAULT_VARIABLE = "drm";

  /**
   * @return The DRM settings of the current item
   */
  DrmSettingsScriptType getSettings();

  /**
   * Create a DRM Party for use in the DRMSettingsScriptType returned by getSettings()
   *
   * @param userId A user ID, either of an internal EQUELLA user or an externally authenticated user
   * @param owner Whether the party is an owner or not
   * @return A populated DrmPartyScriptType
   */
  DrmPartyScriptType createPartyFromUserId(String userId, boolean owner);

  /**
   * Create a DRM Party for use in the DRMSettingsScriptType returned by getSettings()
   *
   * @param name The display name of the party
   * @param emailAddress The email address of the party
   * @param owner Whether the party is an owner or not
   * @return A DrmPartyScriptType populated from the supplied details
   */
  DrmPartyScriptType createParty(String name, String emailAddress, boolean owner);
}
