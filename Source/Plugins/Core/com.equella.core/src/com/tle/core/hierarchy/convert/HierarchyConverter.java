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

package com.tle.core.hierarchy.convert;

import com.tle.beans.hierarchy.HierarchyTopic;
import com.tle.core.entity.convert.BaseEntityTreeNodeConverter;
import com.tle.core.guice.Bind;
import com.tle.core.hierarchy.HierarchyDao;
import com.tle.core.institution.convert.ConverterParams;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

@Bind
@Singleton
public class HierarchyConverter extends BaseEntityTreeNodeConverter<HierarchyTopic> {
  @Inject private HierarchyDao hierarchyDao;

  @SuppressWarnings("nls")
  public HierarchyConverter() {
    super("hierarchy", "hierarchy.xml");
  }

  @Override
  public HierarchyDao getDao() {
    return hierarchyDao;
  }

  @Override
  protected Map<Long, Long> getIdMap(ConverterParams params) {
    return params.getHierarchies();
  }

  @Override
  public String getStringId() {
    return "HIERARCHY";
  }

  @Override
  public Class<HierarchyTopic> getNodeClass() {
    return HierarchyTopic.class;
  }
}
