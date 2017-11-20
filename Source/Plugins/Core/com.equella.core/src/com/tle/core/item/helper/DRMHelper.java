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

package com.tle.core.item.helper;

import static com.dytech.edge.common.Constants.BLANK;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.item.DrmSettings;
import com.tle.beans.item.DrmSettings.Party;
import com.tle.beans.item.DrmSettings.Usage;
import com.tle.beans.item.Item;
import com.tle.common.Pair;
import com.tle.common.Triple;
import com.tle.common.i18n.CurrentTimeZone;
import com.tle.common.util.Dates;
import com.tle.common.util.LocalDate;
import com.tle.common.util.UtcDate;
import com.tle.core.guice.Bind;

@SuppressWarnings("nls")
@Bind
@Singleton
public class DRMHelper extends AbstractHelper
{
	@Override
	public void load(PropBagEx item, Item bean)
	{
		if( bean.getDrmSettings() != null )
		{
			item.append("/", getOdrlFromDrmSettings(bean.getDrmSettings()));
		}
	}

	@Override
	public void save(PropBagEx xml, Item item, Set<String> handled)
	{
		final PropBagEx rights = xml.getSubtree("rights");
		if( rights != null )
		{
			DrmSettings settings = getDrmSettingsFromOdrl(rights);
			item.setDrmSettings(settings);
		}
		handled.add("rights");
	}

	// ODRL Conversion ///////////////////////////////////////////////////////

	public DrmSettings getDrmSettingsFromOdrl(PropBagEx odrl)
	{
		PropBagEx offer = odrl.getSubtree("offer");
		if( offer == null )
		{
			return null;
		}

		DrmSettings settings = new DrmSettings();
		readParties(offer, settings);
		readPermission(offer.getSubtree("permission"), settings);
		return settings;
	}

	private void readPermission(PropBagEx permission, DrmSettings settings)
	{
		if( permission == null )
		{
			return;
		}

		settings.setOwnerMustAccept(permission.nodeExists("tle_ownerMustAccept"));
		settings.setPreviewAllowed(permission.nodeExists("tle_preview"));
		settings.setEnforceAttribution(permission.nodeExists("tle_attributionIsEnforced"));
		settings.setShowLicenceCount(permission.nodeExists("tle_licenceCount"));
		settings.setAllowSummary(permission.nodeExists("tle_summary"));
		settings.setHideLicencesFromOwner(permission.nodeExists("tle_hideLicencesFromOwner"));
		settings.setEnforceAttribution(permission.nodeExists("tle_attributionIsEnforced"));
		settings.setStudentsMustAcceptIfInCompilation(permission.nodeExists("tle_showLicenceInComposition"));

		readUsages(permission, settings);
		readRequirement(permission.getSubtree("requirement"), settings);
		readConstraint(permission.getSubtree("container/constraint"), settings);
	}

	private void writePermission(PropBagEx permission, DrmSettings settings)
	{
		writeUsages(permission, settings);
		if( settings.isOwnerMustAccept() )
		{
			permission.setNode("tle_ownerMustAccept", BLANK);
		}
		if( settings.isPreviewAllowed() )
		{
			permission.setNode("tle_preview", BLANK);
		}

		if( settings.isEnforceAttribution() )
		{
			permission.setNode("tle_attributionIsEnforced", BLANK);
		}

		if( settings.isShowLicenceCount() )
		{
			permission.setNode("tle_licenceCount", BLANK);
		}

		if( settings.isAllowSummary() )
		{
			permission.setNode("tle_summary", BLANK);
		}

		if( settings.isHideLicencesFromOwner() )
		{
			permission.setNode("tle_hideLicencesFromOwner", BLANK);
		}

		if( settings.isEnforceAttribution() )
		{
			permission.setNode("tle_attributionIsEnforced", BLANK);
		}

		if( settings.isStudentsMustAcceptIfInCompilation() )
		{
			permission.setNode("tle_showLicenceInComposition", BLANK);
		}

		writeRequirement(permission.newSubtree("requirement"), settings);
		writeConstraint(permission.newSubtree("container/constraint"), settings);
	}

	private void readConstraint(PropBagEx constraint, DrmSettings settings)
	{
		if( constraint == null )
		{
			return;
		}

		settings.setRestrictToSector(constraint.getNode("purpose/@type").equals("sectors:educational"));

		List<String> recipList = new ArrayList<String>();
		for( PropBagEx individual : constraint.iterator("individual") )
		{
			recipList.add('u' + individual.getNode("context/uid").substring(4));
		}
		for( PropBagEx group : constraint.iterator("group") )
		{
			recipList.add('g' + group.getNode("context/uid").substring(4));
		}
		settings.setRestrictedToRecipients(recipList.size() > 0 ? recipList : null);

		List<Triple<String, String, String>> ranges = new ArrayList<Triple<String, String, String>>();
		for( PropBagEx iprange : constraint.iterator("network") )
		{
			Triple<String, String, String> range = new Triple<String, String, String>(iprange.getNode("@name"),
				iprange.getNode("range/min"), iprange.getNode("range/max"));
			ranges.add(range);
		}
		settings.setRestrictedToIpRanges(ranges.size() > 0 ? ranges : null);

		settings.setMaximumUsageCount(constraint.getIntNode("count", 0));

		Pair<Date, Date> restricted = null;
		try
		{
			Date start = new UtcDate(constraint.getNode("datetime/start"), Dates.ISO_MIDNIGHT).toDate();
			Date end = new UtcDate(constraint.getNode("datetime/end"), Dates.ISO_MIDNIGHT).toDate();
			restricted = new Pair<Date, Date>(start, end);
		}
		catch( ParseException e )
		{
			// no date
		}
		settings.setRestrictedToDateRange(restricted);
	}

	private void writeConstraint(PropBagEx constraint, DrmSettings settings)
	{
		if( settings.isRestrictToSector() )
		{
			constraint.setNode("purpose/@type", "sectors:educational");
		}

		List<String> recipList = settings.getRestrictedToRecipients();
		if( recipList != null )
		{
			for( String recip : recipList )
			{
				char type = recip.charAt(0);
				String nodename = "individual";
				if( type == 'g' )
				{
					nodename = "group";
				}
				PropBagEx indiv = constraint.newSubtree(nodename);
				indiv.setNode("context/uid", "tle:" + recip.substring(1));
			}
		}

		if( settings.getMaximumUsageCount() > 0 )
		{
			constraint.setNode("count", settings.getMaximumUsageCount());
		}

		List<Triple<String, String, String>> ranges = settings.getRestrictedToIpRanges();
		if( ranges != null )
		{
			for( Triple<String, String, String> range : ranges )
			{
				PropBagEx network = constraint.newSubtree("network");
				network.setNode("@name", range.getFirst());
				network.setNode("range/min", range.getSecond());
				network.setNode("range/max", range.getThird());
			}
		}
		Pair<Date, Date> dateRange = settings.getRestrictedToDateRange();
		if( dateRange != null )
		{
			Date first = dateRange.getFirst();
			if( first != null )
			{
				constraint.setNode("datetime/start",
					new LocalDate(first, CurrentTimeZone.get()).format(Dates.ISO_MIDNIGHT));
			}

			Date second = dateRange.getSecond();
			if( second != null )
			{
				constraint.setNode("datetime/end",
					new LocalDate(second, CurrentTimeZone.get()).format(Dates.ISO_MIDNIGHT));
			}
		}
	}

	private void readRequirement(PropBagEx requirement, DrmSettings settings)
	{
		if( requirement == null )
		{
			return;
		}

		settings.setAttributionOfOwnership(requirement.nodeExists("attribution"));
		String terms = requirement.getNode("accept/context/remark");
		settings.setTermsOfAgreement(terms.length() > 0 ? terms : null);
	}

	private void writeRequirement(PropBagEx requirement, DrmSettings settings)
	{
		if( settings.isAttributionOfOwnership() )
		{
			requirement.setNode("attribution", BLANK);
		}

		String terms = settings.getTermsOfAgreement();
		if( terms != null && terms.length() > 0 )
		{
			requirement.setNode("accept/context/remark", terms);
		}
	}

	private void writeUsages(PropBagEx permission, DrmSettings settings)
	{
		List<Usage> usages = settings.getUsagesAsEnum();
		for( Usage usage : usages )
		{
			permission.setNode(usage.toString(), BLANK);
		}
	}

	private void readUsages(PropBagEx permission, DrmSettings settings)
	{
		List<Usage> usages = new ArrayList<Usage>();
		Usage[] allUsages = Usage.values();
		for( Usage usage : allUsages )
		{
			if( permission.nodeExists(usage.toString()) )
			{
				usages.add(usage);
			}
		}
		settings.setUsagesFromEnum(usages);
	}

	private void readParties(PropBagEx odrl, DrmSettings settings)
	{
		List<Party> parties = new ArrayList<Party>();
		for( PropBagEx party : odrl.iterateAll("party/context") )
		{
			Party aparty = new Party();
			aparty.setEmail(party.getNode("remark"));
			aparty.setName(party.getNode("name"));
			aparty.setOwner(party.isNodeTrue("@owner"));

			String uid = party.getNode("uid");
			if( uid.length() > 4 )
			{
				aparty.setUserID(uid.substring(4));
			}
			parties.add(aparty);
		}
		settings.setContentOwners(parties);
	}

	private void writeParties(PropBagEx odrl, DrmSettings settings)
	{
		List<Party> parties = settings.getContentOwners();
		for( Party party : parties )
		{
			PropBagEx partyXml = odrl.newSubtree("party");
			PropBagEx context = partyXml.newSubtree("context");
			context.setNode("name", party.getName());
			context.setNode("remark", party.getEmail());
			if( party.isOwner() )
			{
				context.setNode("uid", "tle:" + party.getUserID());
				context.setNode("@owner", true);
			}
		}
	}

	public PropBagEx getOdrlFromDrmSettings(DrmSettings settings)
	{
		PropBagEx rights = new PropBagEx().aquireSubtree("rights");
		PropBagEx offer = rights.newSubtree("offer");
		writeParties(offer, settings);
		writePermission(offer.newSubtree("permission"), settings);
		return rights;
	}
}