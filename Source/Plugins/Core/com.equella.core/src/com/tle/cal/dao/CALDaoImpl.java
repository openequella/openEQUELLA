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

package com.tle.cal.dao;

import javax.inject.Singleton;

import com.tle.beans.cal.CALHolding;
import com.tle.beans.cal.CALPortion;
import com.tle.beans.cal.CALSection;
import com.tle.core.copyright.dao.AbstractCopyrightDao;
import com.tle.core.guice.Bind;

@Bind(CALDao.class)
@Singleton
@SuppressWarnings("nls")
public class CALDaoImpl extends AbstractCopyrightDao<CALHolding, CALPortion, CALSection> implements CALDao
{
	@Override
	protected String getHoldingEntity()
	{
		return "CALHolding";
	}

	@Override
	protected String getPortionEntity()
	{
		return "CALPortion";
	}

	@Override
	protected String getSectionEntity()
	{
		return "CALSection";
	}

	@Override
	protected String getHoldingTable()
	{
		return "cal_holding";
	}

	@Override
	protected String getPortionTable()
	{
		return "cal_portion";
	}

}