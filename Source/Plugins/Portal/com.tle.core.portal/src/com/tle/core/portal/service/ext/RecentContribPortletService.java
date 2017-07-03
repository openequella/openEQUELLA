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

package com.tle.core.portal.service.ext;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import com.tle.common.beans.exception.ValidationError;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.portal.entity.Portlet;
import com.tle.common.portal.entity.impl.PortletRecentContrib;
import com.tle.core.guice.Bind;
import com.tle.core.portal.dao.PortletDao;
import com.tle.core.portal.service.PortletEditingBean;
import com.tle.core.portal.service.PortletServiceExtension;

/**
 * @author aholland
 */
@Bind
@Singleton
public class RecentContribPortletService implements PortletServiceExtension
{
	private static final String KEY_ERROR_VALIDATION_AGE = "com.tle.core.portal.recent.age.error"; //$NON-NLS-1$

	@Inject
	private PortletDao portletDao;

	@Override
	public void loadExtra(Portlet portlet)
	{
		PortletRecentContrib contrib = portletDao.findAnyById(PortletRecentContrib.class, portlet.getId());
		portlet.setExtraData(contrib);
	}

	@Override
	public void edit(Portlet to, PortletEditingBean from)
	{
		PortletRecentContrib contrib = (PortletRecentContrib) from.getExtraData();
		contrib.setId(to.getId());
		contrib.setPortlet(to);
		portletDao.mergeAny(contrib);
	}

	@Override
	public void add(Portlet portlet)
	{
		PortletRecentContrib contrib = (PortletRecentContrib) portlet.getExtraData();
		contrib.setPortlet(portlet);
		contrib.setId(portlet.getId());
		portletDao.saveAny(contrib);
	}

	@Override
	public void deleteExtra(Portlet portlet)
	{
		PortletRecentContrib contrib = portletDao.findAnyById(PortletRecentContrib.class, portlet.getId());
		portletDao.deleteAny(contrib);
	}

	@Override
	@SuppressWarnings("nls")
	public void changeUserId(String fromUserId, String toUserId)
	{
		DetachedCriteria dc = DetachedCriteria.forClass(PortletRecentContrib.class);
		dc.add(Restrictions.eq("userId", fromUserId));
		dc.createCriteria("portlet").add(Restrictions.eq("institution", CurrentInstitution.get()));

		List<PortletRecentContrib> prcs = portletDao.findAnyByCriteria(dc, null, null);
		for( PortletRecentContrib prc : prcs )
		{
			prc.setUserId(toUserId);
			portletDao.saveAny(prc);
		}
	}

	@Override
	public void doValidation(PortletEditingBean newPortlet, List<ValidationError> errors)
	{
		PortletRecentContrib recent = (PortletRecentContrib) newPortlet.getExtraData();
		if( recent.getAgeDays() < 0 )
		{
			errors.add(new ValidationError("age", CurrentLocale.get(KEY_ERROR_VALIDATION_AGE))); //$NON-NLS-1$
		}

	}
}
