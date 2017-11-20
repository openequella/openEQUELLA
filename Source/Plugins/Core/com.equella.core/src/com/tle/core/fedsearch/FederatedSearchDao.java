/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.fedsearch;

import java.util.List;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.FederatedSearch;
import com.tle.core.entity.dao.AbstractEntityDao;

/**
 * @author Nicholas Read
 */
public interface FederatedSearchDao extends AbstractEntityDao<FederatedSearch>
{
	List<Long> findEngineNamesByType(String type);

	List<FederatedSearch> enumerateAllZ3950();

	List<BaseEntityLabel> listEnabled();
}
