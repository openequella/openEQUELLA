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

package com.tle.core.item.edit.impl;

import java.util.Date;
import java.util.List;

import com.tle.beans.item.DrmSettings;
import com.tle.beans.item.DrmSettings.Party;
import com.tle.beans.item.DrmSettings.Usage;
import com.tle.beans.item.Item;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.Triple;
import com.tle.core.item.edit.DRMEditor;
import com.tle.core.item.edit.ItemEditorChangeTracker;

/**
 * @author Aaron
 */
public class DRMEditorImpl implements DRMEditor
{
	private final Item item;
	private DrmSettings drmSettings;
	private final ItemEditorChangeTracker changeTracker;

	public DRMEditorImpl(Item item, ItemEditorChangeTracker changeTracker)
	{
		this.item = item;
		this.drmSettings = item.getDrmSettings();
		this.changeTracker = changeTracker;
	}

	@Override
	public void remove()
	{
		if( item.getDrmSettings() != null )
		{
			changeTracker.editDetected();
		}
		item.setDrmSettings(null);
		drmSettings = null;
	}

	private DrmSettings ensureDrm()
	{
		if( drmSettings == null )
		{
			drmSettings = new DrmSettings();
			item.setDrmSettings(drmSettings);
		}
		return drmSettings;
	}

	@Override
	public void editDrmPageUuid(String drmPageUuid)
	{

		if( Check.isEmpty(drmPageUuid) )
		{
			drmPageUuid = DrmSettings.CUSTOM_SCRIPTED_DRMSETTINGS_PAGE_PLACEHOLDER;
		}
		DrmSettings drm = ensureDrm();
		if( changeTracker.hasBeenEdited(drm.getDrmPageUuid(), drmPageUuid) )
		{
			drm.setDrmPageUuid(drmPageUuid);
		}
	}

	@Override
	public void editHideLicencesFromOwner(boolean val)
	{
		DrmSettings drm = ensureDrm();
		if( changeTracker.hasBeenEdited(drm.isHideLicencesFromOwner(), val) )
		{
			drm.setHideLicencesFromOwner(val);
		}

	}

	@Override
	public void editShowLicenceCount(boolean val)
	{
		DrmSettings drm = ensureDrm();
		if( changeTracker.hasBeenEdited(drm.isShowLicenceCount(), val) )
		{
			drm.setShowLicenceCount(val);
		}
	}

	@Override
	public void editAllowSummary(boolean val)
	{
		DrmSettings drm = ensureDrm();
		if( changeTracker.hasBeenEdited(drm.isAllowSummary(), val) )
		{
			drm.setAllowSummary(val);
		}
	}

	@Override
	public void editOwnerMustAccept(boolean val)
	{
		DrmSettings drm = ensureDrm();
		if( changeTracker.hasBeenEdited(drm.isOwnerMustAccept(), val) )
		{
			drm.setOwnerMustAccept(val);
		}
	}

	@Override
	public void editStudentsMustAcceptIfInCompilation(boolean val)
	{
		DrmSettings drm = ensureDrm();
		if( changeTracker.hasBeenEdited(drm.isStudentsMustAcceptIfInCompilation(), val) )
		{
			drm.setStudentsMustAcceptIfInCompilation(val);
		}
	}

	@Override
	public void editPreviewAllowed(boolean val)
	{
		DrmSettings drm = ensureDrm();
		if( changeTracker.hasBeenEdited(drm.isPreviewAllowed(), val) )
		{
			drm.setPreviewAllowed(val);
		}
	}

	@Override
	public void editAttributionOfOwnership(boolean val)
	{
		DrmSettings drm = ensureDrm();
		if( changeTracker.hasBeenEdited(drm.isAttributionOfOwnership(), val) )
		{
			drm.setAttributionOfOwnership(val);
		}
	}

	@Override
	public void editEnforceAttribution(boolean val)
	{
		DrmSettings drm = ensureDrm();
		if( changeTracker.hasBeenEdited(drm.isEnforceAttribution(), val) )
		{
			drm.setEnforceAttribution(val);
		}
	}

	@Override
	public void editTermsOfAgreement(String terms)
	{
		DrmSettings drm = ensureDrm();
		if( changeTracker.hasBeenEdited(drm.getTermsOfAgreement(), terms) )
		{
			drm.setTermsOfAgreement(terms);
		}
	}

	@Override
	public void editRequireAcceptanceFrom(String expr)
	{
		DrmSettings drm = ensureDrm();
		if( changeTracker.hasBeenEdited(drm.getRequireAcceptanceFrom(), expr) )
		{
			drm.setRequireAcceptanceFrom(expr);
		}
	}

	@Override
	public void editUsages(List<Usage> usages)
	{
		DrmSettings drm = ensureDrm();
		if( changeTracker.hasBeenEdited(drm.getUsagesAsEnum(), usages) )
		{
			drm.setUsagesFromEnum(usages);
		}
	}

	@Override
	public void editDateRange(Date startDate, Date endDate)
	{
		DrmSettings drm = ensureDrm();
		Pair<Date, Date> newRange = null;
		if( startDate != null || endDate != null )
		{
			newRange = new Pair<Date, Date>(startDate, endDate);
		}
		if( changeTracker.hasBeenEdited(drm.getRestrictedToDateRange(), newRange) )
		{
			drm.setRestrictedToDateRange(newRange);
		}
	}

	@Override
	public void editUsersExpression(List<String> users)
	{
		DrmSettings drm = ensureDrm();
		if( changeTracker.hasBeenEdited(drm.getRestrictedToRecipients(), users) )
		{
			drm.setRestrictedToRecipients(users);
		}
	}

	@Override
	public void editEducationalSector(boolean val)
	{
		DrmSettings drm = ensureDrm();
		if( changeTracker.hasBeenEdited(drm.isRestrictToSector(), val) )
		{
			drm.setRestrictToSector(val);
		}
	}

	@Override
	public void editMaximumUsage(int val)
	{
		DrmSettings drm = ensureDrm();
		if( changeTracker.hasBeenEdited(drm.getMaximumUsageCount(), val) )
		{
			drm.setMaximumUsageCount(val);
		}
	}

	@Override
	public void editContentOwners(List<Party> parties)
	{
		DrmSettings drm = ensureDrm();
		if( changeTracker.hasBeenEdited(drm.getContentOwners(), parties) )
		{
			drm.setContentOwners(parties);
		}
	}

	@Override
	public void editNetworks(List<Triple<String, String, String>> networks)
	{
		DrmSettings drm = ensureDrm();
		if( changeTracker.hasBeenEdited(drm.getRestrictedToIpRanges(), networks) )
		{
			drm.setRestrictedToIpRanges(networks);
		}
	}
}
