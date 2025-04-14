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

package com.tle.core.harvester.impl;

import com.tle.beans.item.Item;
import java.io.OutputStream;

public interface PrivateSoapHarvesterService {

  /**
   * Zip up and download all of an items attachments.<br>
   * The logged in user must have the DOWNLOAD_ITEM permission for the item for this method to work.
   *
   * @param cos An opened OutputStream
   * @param item The item to download
   */
  void getItemAttachments(OutputStream cos, Item item) throws Exception;
}
