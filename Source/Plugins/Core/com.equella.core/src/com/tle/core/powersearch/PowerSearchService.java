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

package com.tle.core.powersearch;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.PowerSearch;
import com.tle.beans.entity.Schema;
import com.tle.core.entity.EntityEditingBean;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.remoting.RemotePowerSearchService;
import java.util.List;

public interface PowerSearchService
    extends AbstractEntityService<EntityEditingBean, PowerSearch>, RemotePowerSearchService {

  /**
   * List all available Power Searches (Advanced Searches) assuming 'SEARCH_POWER_SEARCH' ACL.
   *
   * @return available power searches
   */
  List<BaseEntityLabel> listSearchable();

  List<BaseEntityLabel> listAllForSchema(long schemaId);

  List<BaseEntityLabel> listAllForSchema(Schema schema);

  /**
   * List all available Power Searches (Advanced Searches) assuming 'SEARCH_POWER_SEARCH' ACL.
   *
   * @return available power searches
   */
  List<PowerSearch> enumerateSearchable();
}
