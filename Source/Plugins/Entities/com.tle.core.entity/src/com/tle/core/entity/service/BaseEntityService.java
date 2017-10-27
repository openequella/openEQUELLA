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

package com.tle.core.entity.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tle.core.remoting.RemoteBaseEntityService;

/**
 * @author Nicholas Read
 */
public interface BaseEntityService extends RemoteBaseEntityService
{
	List<Long> getIdsFromUuids(Set<String> uuids);

	Map<Long, String> getUuids(Set<Long> ids);

	/**
	 * A list of edit privileges for entities where the user either does not
	 * have permission, or does have permission but there are no actual entities
	 * that can be edited (there are none, or revokes are on the entities
	 * themselves).
	 */
	List<String> getEditPrivilegeForEntitiesIHaveNoneToEdit();
}
