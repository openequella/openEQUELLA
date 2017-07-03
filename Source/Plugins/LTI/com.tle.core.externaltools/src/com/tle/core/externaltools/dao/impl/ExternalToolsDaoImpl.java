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

package com.tle.core.externaltools.dao.impl;

import com.google.inject.Singleton;
import com.tle.common.externaltools.entity.ExternalTool;
import com.tle.core.entity.dao.impl.AbstractEntityDaoImpl;
import com.tle.core.externaltools.dao.ExternalToolsDao;
import com.tle.core.guice.Bind;

@Bind(ExternalToolsDao.class)
@Singleton
public class ExternalToolsDaoImpl extends AbstractEntityDaoImpl<ExternalTool> implements ExternalToolsDao
{
	public ExternalToolsDaoImpl()
	{
		super(ExternalTool.class);
	}

	// TODO
}
