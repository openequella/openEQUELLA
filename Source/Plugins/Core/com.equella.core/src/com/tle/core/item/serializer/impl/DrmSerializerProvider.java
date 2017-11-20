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

package com.tle.core.item.serializer.impl;

import java.util.Date;
import java.util.List;

import javax.inject.Singleton;

import org.hibernate.criterion.Projections;

import com.google.common.collect.Lists;
import com.tle.beans.item.DrmSettings;
import com.tle.beans.item.DrmSettings.Party;
import com.tle.common.Pair;
import com.tle.common.Triple;
import com.tle.core.guice.Bind;
import com.tle.core.item.serializer.ItemSerializerProvider;
import com.tle.core.item.serializer.ItemSerializerState;
import com.tle.core.item.serializer.XMLStreamer;
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;
import com.tle.web.api.item.interfaces.beans.DrmBean;
import com.tle.web.api.item.interfaces.beans.DrmNetworkBean;
import com.tle.web.api.item.interfaces.beans.DrmOptionsBean;
import com.tle.web.api.item.interfaces.beans.DrmOptionsBean.DrmRestrictionBean;
import com.tle.web.api.item.interfaces.beans.DrmPartyBean;

@Bind
@Singleton
@SuppressWarnings("nls")
public class DrmSerializerProvider implements ItemSerializerProvider
{
	private static final String CATEGORY_DRM = "drm";
	private static final String ALIAS_DRM = "drm";

	@Override
	public void prepareItemQuery(ItemSerializerState state)
	{
		if( state.hasCategory(CATEGORY_DRM) )
		{
			state.getItemProjection().add(Projections.property("drmSettings"), ALIAS_DRM);
		}
	}

	@Override
	public void performAdditionalQueries(ItemSerializerState state)
	{
		// if( state.hasCategory(CATEGORY_DRM) )
		// {
		// No! There could be hundreds
		//
		// final Multimap<Long, DrmAcceptance> drmAcceptances = itemDao
		// .getDrmAcceptancesForItemIds(state.getItemKeys());
		// for( Long itemId : drmAcceptances.keySet() )
		// {
		// state.setData(itemId, KEY_DRM_ACCEPTANCES,
		// drmAcceptances.get(itemId));
		// }
		// }
	}

	@Override
	public void writeXmlResult(XMLStreamer xml, ItemSerializerState state, long itemId)
	{
		if( state.hasCategory(CATEGORY_DRM) )
		{
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public void writeItemBeanResult(EquellaItemBean equellaItemBean, ItemSerializerState state, long itemId)
	{
		if( state.hasCategory(CATEGORY_DRM) )
		{
			final DrmSettings drm = (DrmSettings) state.getItemData(itemId).get(ALIAS_DRM);
			final DrmBean equellaDrmBean = new DrmBean();
			if( drm != null )
			{
				DrmOptionsBean options = new DrmOptionsBean();
				equellaDrmBean.setOptions(options);
				options.setAllowSummary(drm.isAllowSummary());
				options.setAttributionOfOwnership(drm.isAttributionOfOwnership());
				options.setDrmPageUuid(drm.getDrmPageUuid());
				options.setEnforceAttribution(drm.isEnforceAttribution());
				options.setHideLicencesFromOwner(drm.isHideLicencesFromOwner());
				options.setOwnerMustAccept(drm.isOwnerMustAccept());
				options.setPreviewAllowed(drm.isPreviewAllowed());
				options.setRequireAcceptanceFrom(drm.getRequireAcceptanceFrom());
				options.setShowLicenceCount(drm.isShowLicenceCount());
				options.setStudentsMustAcceptIfInCompilation(drm.isStudentsMustAcceptIfInCompilation());
				options.setTermsOfAgreement(drm.getTermsOfAgreement());
				options.setUsages(drm.getUsageStrings());

				// Party party party
				final List<Party> parties = drm.getContentOwners();
				if( parties != null && !parties.isEmpty() )
				{
					final List<DrmPartyBean> partyBeans = Lists.newArrayList();
					for( Party party : parties )
					{
						partyBeans.add(new DrmPartyBean(party.getUserID(), party.getName(), party.getEmail(), party
							.isOwner()));
					}
					options.setContentOwners(partyBeans);
				}

				final DrmRestrictionBean restrict = new DrmRestrictionBean();
				restrict.setEducationalSector(drm.isRestrictToSector());

				final Pair<Date, Date> restrictedToDateRange = drm.getRestrictedToDateRange();
				if( restrictedToDateRange != null )
				{
					restrict.setStartDate(restrictedToDateRange.getFirst());
					restrict.setEndDate(restrictedToDateRange.getSecond());
				}
				restrict.setMaximumUsage(drm.getMaximumUsageCount());

				final List<Triple<String, String, String>> restrictedToIpRanges = drm.getRestrictedToIpRanges();
				if( restrictedToIpRanges != null && !restrictedToIpRanges.isEmpty() )
				{
					final List<DrmNetworkBean> networkBeans = Lists.newArrayList();
					for( Triple<String, String, String> triple : restrictedToIpRanges )
					{
						networkBeans.add(new DrmNetworkBean(triple.getFirst(), triple.getSecond(), triple.getThird()));
					}
					restrict.setNetwork(networkBeans);
				}
				restrict.setNetwork(null);
				restrict.setUsersExpression(drm.getRestrictedToRecipients());

				options.setRestriction(restrict);

				// No! There could be hundreds
				//
				// final List<DrmAcceptance> drmAcceptances =
				// state.getData(itemId,
				// KEY_DRM_ACCEPTANCES);
				// final List<DrmAcceptanceBean> drmAcceptanceBeans =
				// Lists.newArrayList();
				// if( !Check.isEmpty(drmAcceptances) )
				// {
				// for( DrmAcceptance assept : drmAcceptances )
				// {
				// UserBean userBean = new UserBean();
				// userBean.setId(assept.getUser());
				// drmAcceptanceBeans.add(new DrmAcceptanceBean(userBean,
				// assept.getDate()));
				// }
				// }
				// options.setAcceptances(drmAcceptanceBeans);

			}
			equellaItemBean.setDrm(equellaDrmBean);
		}
	}
}
