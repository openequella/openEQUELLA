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

import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.EntityLock;

public interface EntityLockingService
{
	EntityLock lockEntity(BaseEntity entity);

	EntityLock lockEntity(BaseEntity entity, String sessionId);

	void unlockEntity(BaseEntity entity, boolean force);

	EntityLock getLock(BaseEntity entity);

	EntityLock getLock(BaseEntity entity, String lockId);

	/**
	 * Does not attempt to perform a lock or see if we own it the existing one,
	 * just gets an existing lock (if any)
	 * 
	 * @param entity
	 * @return
	 */
	EntityLock getLockUnbound(BaseEntity entity);

	/**
	 * @param entity
	 * @param userId Optional. Is the entity locked by the supplied user ID?
	 * @param sessionId Optional. Is the entity locked by the supplied session
	 *            ID?
	 * @return
	 */
	boolean isEntityLocked(BaseEntity entity, String userId, String sessionId);
}
