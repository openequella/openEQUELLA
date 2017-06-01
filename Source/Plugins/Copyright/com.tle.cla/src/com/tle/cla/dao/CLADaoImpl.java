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

package com.tle.cla.dao;

import javax.inject.Singleton;

import com.tle.beans.cla.CLAHolding;
import com.tle.beans.cla.CLAPortion;
import com.tle.beans.cla.CLASection;
import com.tle.core.copyright.dao.AbstractCopyrightDao;
import com.tle.core.guice.Bind;

@Bind(CLADao.class)
@Singleton
@SuppressWarnings("nls")
public class CLADaoImpl extends AbstractCopyrightDao<CLAHolding, CLAPortion, CLASection> implements CLADao
{

	@Override
	protected String getHoldingEntity()
	{
		return "CLAHolding";
	}

	@Override
	protected String getPortionEntity()
	{
		return "CLAPortion";
	}

	@Override
	protected String getSectionEntity()
	{
		return "CLASection";
	}

	@Override
	protected String getPortionTable()
	{
		return "cla_portion";
	}

	@Override
	protected String getHoldingTable()
	{
		return "cla_holding";
	}

}