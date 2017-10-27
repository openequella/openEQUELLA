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

package com.tle.core.entity;

import java.io.Serializable;
import java.util.Map;

import com.tle.beans.entity.BaseEntity;
import com.tle.common.EntityPack;

/**
 * @author aholland
 */
public interface EntityEditingSession<B extends EntityEditingBean, E extends BaseEntity> extends Serializable
{
	@Deprecated
	<P extends EntityPack<E>> P getPack();

	/**
	 * Use getBean instead
	 * 
	 * @return
	 */
	@Deprecated
	E getEntity();

	boolean isNew();

	B getBean();

	String getSessionId();

	String getStagingId();

	void setStagingId(String stagingId);

	Map<String, Object> getValidationErrors();

	Map<Class<? extends Serializable>, Serializable> getAttributes();

	<T extends Serializable> T getAttribute(Class<T> key);

	<T extends Serializable> void setAttribute(Class<T> key, T value);

	void setValid(boolean valid);

	boolean isValid();

}
