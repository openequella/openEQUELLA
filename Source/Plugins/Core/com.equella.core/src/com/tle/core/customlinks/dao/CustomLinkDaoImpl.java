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

package com.tle.core.customlinks.dao;

import java.util.List;

import javax.inject.Singleton;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tle.common.customlinks.entity.CustomLink;
import com.tle.core.entity.dao.impl.AbstractEntityDaoImpl;
import com.tle.core.guice.Bind;
import com.tle.core.security.impl.SecureOnReturn;

@Bind(CustomLinkDao.class)
@Singleton
public class CustomLinkDaoImpl extends AbstractEntityDaoImpl<CustomLink> implements CustomLinkDao
{
	public CustomLinkDaoImpl()
	{
		super(CustomLink.class);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	@SecureOnReturn(priv = "VIEW_CUSTOM_LINK")
	public List<CustomLink> listLinksForUser()
	{
		return enumerateAll();
	}

}
