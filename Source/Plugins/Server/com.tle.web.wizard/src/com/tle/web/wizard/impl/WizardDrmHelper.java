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

package com.tle.web.wizard.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.PropBagIterator;
import com.dytech.edge.wizard.TargetNode;
import com.dytech.edge.wizard.beans.DRMPage;
import com.dytech.edge.wizard.beans.DRMPage.Container;
import com.dytech.edge.wizard.beans.DRMPage.Contributor;
import com.dytech.edge.wizard.beans.DRMPage.Network;
import com.dytech.edge.wizard.beans.DefaultWizardPage;
import com.dytech.edge.wizard.beans.control.Calendar;
import com.dytech.edge.wizard.beans.control.CheckBoxGroup;
import com.dytech.edge.wizard.beans.control.EditBox;
import com.dytech.edge.wizard.beans.control.Group;
import com.dytech.edge.wizard.beans.control.GroupItem;
import com.dytech.edge.wizard.beans.control.Multi;
import com.dytech.edge.wizard.beans.control.RadioGroup;
import com.dytech.edge.wizard.beans.control.ShuffleBox;
import com.dytech.edge.wizard.beans.control.WizardControlItem;
import com.google.common.collect.Sets;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.item.DrmSettings;
import com.tle.beans.item.DrmSettings.Party;
import com.tle.beans.item.DrmSettings.Usage;
import com.tle.common.Check;
import com.tle.common.Format;
import com.tle.common.Pair;
import com.tle.common.Triple;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.CurrentTimeZone;
import com.tle.common.i18n.LangUtils;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.common.util.Dates;
import com.tle.common.util.LocalDate;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.DrmService;
import com.tle.core.services.user.UserService;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;

@Bind
@Singleton
@SuppressWarnings("nls")
public class WizardDrmHelper
{
	// Copied from DRMConfigTab in the admin console
	private static final String BASIC_USAGE_KEY = "drmconfigtab.basicusage";
	private static final String USE_AND_ADAPT_KEY = "drmconfigtab.useandadapt";
	private static final String CUSTOM_PERMISSIONS_KEY = "drmconfigtab.custompermissions";

	private static final TargetNode OWNER_NODE = new TargetNode("item/rights/party/owner", "");
	private static final TargetNode OTHER_NODE = new TargetNode("item/rights/party/other", "");
	private static final TargetNode OTHER_NAME_NODE = new TargetNode("name", "");
	private static final TargetNode OTHER_EMAIL_NODE = new TargetNode("email", "");
	private static final TargetNode USAGE_OPTION_NODE = new TargetNode("item/rights/usage/option", "");
	private static final TargetNode USAGE_PERMISSION_NODE = new TargetNode("item/rights/usage/permission", "");
	private static final TargetNode USERSGROUPS_NODE = new TargetNode("item/rights/constraint/usersandgroups/entity",
		"");
	private static final TargetNode NETWORK_NODE = new TargetNode("item/rights/constraint/networks/network", "");
	private static final TargetNode COUNTSPECIFIED_NODE = new TargetNode("item/rights/countspecified", "");
	private static final TargetNode COUNT_NODE = new TargetNode("item/rights/constraint/count", "");
	private static final TargetNode DATESPECIFIED_NODE = new TargetNode("item/rights/datetimespecified", "");
	private static final TargetNode DATERANGE_NODE = new TargetNode("item/rights/constraint/datetime/range", "");
	private static final TargetNode PURPOSE_NODE = new TargetNode("item/rights/constraint/purpose", "");
	private static final TargetNode ATTRIBUTION_NODE = new TargetNode("item/rights/requirement/attribution", "");
	private static final TargetNode TERMSSPECIFIED_NODE = new TargetNode("item/rights/termsspecified", "");
	private static final TargetNode TERMS_NODE = new TargetNode("item/rights/requirement/accept/context/remark", "");

	private static final Usage[] BASIC_USAGES = {Usage.DISPLAY, Usage.EXECUTE, Usage.PLAY, Usage.PRINT};
	private static final Usage[] ADAPT_USAGES = {Usage.DISPLAY, Usage.EXECUTE, Usage.PLAY, Usage.PRINT, Usage.MODIFY,
			Usage.EXCERPT, Usage.ANNOTATE, Usage.AGGREGATE};

	private static final int SHUFFLE_SIZE = 12;

	static
	{
		PluginResourceHandler.init(WizardDrmHelper.class);
	}

	@PlugKey("drm.")
	private static String KEY_PREFIX;

	@Inject
	private UserService userService;
	@Inject
	private DrmService drmService;

	public List<DefaultWizardPage> generatePages(DRMPage drmPage)
	{
		List<DefaultWizardPage> pages = new ArrayList<DefaultWizardPage>();
		pages.add(generatePage1(drmPage));
		DefaultWizardPage page2 = generatePage2(drmPage.getContributor());
		if( !page2.getControls().isEmpty() )
		{
			pages.add(page2);
		}
		for( DefaultWizardPage page : pages )
		{
			page.setScript(drmPage.getScript());
		}
		return pages;
	}

	private DefaultWizardPage generatePage1(DRMPage drmPage)
	{
		DefaultWizardPage page = new DefaultWizardPage();
		page.setTitle(bundle("page1.title"));

		page.getControls().add(getWhoCreatedItRadioGroup());

		Multi multi = new Multi();
		multi.setTitle(bundle("creator.others.title"));
		multi.setSize1(4);
		multi.getTargetnodes().add(OTHER_NODE);
		// multi.setSelectMultiple(true);
		{
			EditBox name = new EditBox();
			name.setTitle(bundle("creator.others.name"));
			name.getTargetnodes().add(OTHER_NAME_NODE);
			multi.getControls().add(name);

			EditBox email = new EditBox();
			email.setTitle(bundle("creator.others.email"));
			email.getTargetnodes().add(OTHER_EMAIL_NODE);
			multi.getControls().add(email);
		}
		multi.setScript("return xml.get('item/rights/party/owner') == 'everyone' || "
			+ "xml.get('item/rights/party/owner') == 'others';");
		page.getControls().add(multi);

		if( !Check.isEmpty(drmPage.getUsages()) )
		{
			RadioGroup rights = new RadioGroup();
			rights.setReload(true);
			rights.setTitle(bundle("rightsprovided.title"));
			rights.getTargetnodes().add(USAGE_OPTION_NODE);

			List<WizardControlItem> rightsItems = rights.getItems();

			if( drmPage.getUsages().contains(CurrentLocale.get(KEY_PREFIX + BASIC_USAGE_KEY)) )
			{
				addItem(rightsItems, "rightsprovided.basic", "basic");
			}

			if( drmPage.getUsages().contains(CurrentLocale.get(KEY_PREFIX + USE_AND_ADAPT_KEY)) )
			{
				addItem(rightsItems, "rightsprovided.adapt", "adapt");
			}

			if( drmPage.getUsages().contains(CurrentLocale.get(KEY_PREFIX + CUSTOM_PERMISSIONS_KEY)) )
			{
				addItem(rightsItems, "rightsprovided.custom", "custom");
			}

			if( !rightsItems.isEmpty() )
			{
				rightsItems.get(0).setDefault("true");
			}

			page.getControls().add(rights);

			if( drmPage.getUsages().contains("Custom Permissions") )
			{
				// If not the only usage use script
				String script = null;
				if( drmPage.getUsages().size() > 1 )
				{
					script = "return xml.get('item/rights/usage/option') == 'custom'";
				}

				CheckBoxGroup group2 = new CheckBoxGroup();
				group2.setTitle(bundle("rightsprovided.custom.usagetitle"));
				group2.getTargetnodes().add(USAGE_PERMISSION_NODE);
				group2.setScript(script);
				addItems(group2.getItems(), "rightsprovided.custom.", "display", "print", "play", "execute"); //$NON-NLS-3$
				page.getControls().add(group2);

				CheckBoxGroup group3 = new CheckBoxGroup();
				group3.setTitle(bundle("rightsprovided.custom.reusetitle"));
				group3.getTargetnodes().add(USAGE_PERMISSION_NODE);
				group3.setScript(script);
				addItems(group3.getItems(), "rightsprovided.custom.", "modify", "excerpt", "annotate", "aggregate"); //$NON-NLS-3$
				page.getControls().add(group3);
			}
		}

		return page;
	}

	private RadioGroup getWhoCreatedItRadioGroup()
	{
		RadioGroup group = new RadioGroup();
		group.setTitle(bundle("creator.who.title"));
		group.getTargetnodes().add(OWNER_NODE);
		addItems(group.getItems(), "creator.who.", "myself", "everyone", "others"); //$NON-NLS-3$ //$NON-NLS-4$
		group.setReload(true);
		return group;
	}

	private void addItems(List<WizardControlItem> items, String baseKey, String... values)
	{
		for( String value : values )
		{
			addItem(items, baseKey + value, value);
		}

		if( !items.isEmpty() )
		{
			items.get(0).setDefault("true");
		}
	}

	private void addItem(List<WizardControlItem> items, String key, String value)
	{
		LanguageBundle name = bundle(key);
		items.add(new WizardControlItem(name, value));
	}

	private DefaultWizardPage generatePage2(Contributor contributor)
	{
		DefaultWizardPage page = new DefaultWizardPage();
		page.setTitle(bundle("page2.title"));

		if( contributor.hasRecipients() )
		{
			ShuffleBox box = new ShuffleBox();
			box.setTitle(bundle("restrictusers.title"));
			box.setDescription(bundle("restrictusers.description"));
			box.getTargetnodes().add(USERSGROUPS_NODE);
			box.setSize1(SHUFFLE_SIZE);
			for( String user : contributor.getUsers() )
			{
				String name;
				try
				{
					name = Format.format(userService.getInformationForUser(user));
				}
				catch( Exception ex )
				{
					name = "<unknown>";
				}
				box.getItems().add(new WizardControlItem(bundle("restrictusers.user", name), "u" + user));
			}

			for( String group : contributor.getGroups() )
			{
				String name;
				try
				{
					name = Format.format(userService.getInformationForGroup(group));
				}
				catch( Exception ex )
				{
					name = "<unknown>";
				}
				box.getItems().add(new WizardControlItem(bundle("restrictusers.group", name), "g" + group));
			}

			page.getControls().add(box);
		}

		if( !contributor.getNetworks().isEmpty() )
		{
			ShuffleBox nbox = new ShuffleBox();
			nbox.setTitle(bundle("restrictip.title"));
			nbox.setDescription(bundle("restrictip.description"));
			nbox.getTargetnodes().add(NETWORK_NODE);
			nbox.setSize1(SHUFFLE_SIZE);
			for( Network n : contributor.getNetworks() )
			{
				String name = n.getName();
				nbox.getItems().add(
					new WizardControlItem(bundle(null, name), name + "::" + n.getMin() + "::" + n.getMax()));
			}
			page.getControls().add(nbox);
		}

		if( contributor.isCount() )
		{
			Group group = new Group();
			group.getTargetnodes().add(COUNTSPECIFIED_NODE);
			{
				GroupItem item = new GroupItem();
				item.setValue("true");
				item.setName(bundle("restrictaccess.title"));
				{
					EditBox box = new EditBox();
					box.setMandatory(true);
					box.setSize1(35);
					box.setTitle(bundle("restrictaccess.count"));
					box.getTargetnodes().add(COUNT_NODE);
					box.getItems().add(new WizardControlItem(null, Integer.toString(contributor.getCount())));
					item.getControls().add(box);
				}
				group.getGroups().add(item);
			}
			page.getControls().add(group);
		}

		if( contributor.isDatetime() )
		{
			Group group = new Group();
			group.getTargetnodes().add(DATESPECIFIED_NODE);
			{
				GroupItem item = new GroupItem();
				item.setValue("true");
				item.setName(bundle("restrictdate.title"));
				{
					Calendar cal = new Calendar();
					cal.setRange(true);
					cal.setTitle(bundle("restrictdate.range.title"));
					cal.setMandatory(true);
					cal.getTargetnodes().add(DATERANGE_NODE);
					item.getControls().add(cal);
				}
				group.getGroups().add(item);
			}
			page.getControls().add(group);
		}

		if( contributor.isSector() )
		{
			CheckBoxGroup group = new CheckBoxGroup();
			group.getTargetnodes().add(PURPOSE_NODE);
			group.getItems().add(new WizardControlItem(bundle("restrictsector.title"), "sectors:educational"));
			page.getControls().add(group);
		}

		if( contributor.isAttribution() )
		{
			CheckBoxGroup group = new CheckBoxGroup();
			group.getTargetnodes().add(ATTRIBUTION_NODE);
			group.getItems().add(new WizardControlItem(bundle("requireattribution.title"), "true"));
			page.getControls().add(group);
		}

		if( contributor.isTerms() )
		{
			Group group = new Group();
			group.getTargetnodes().add(TERMSSPECIFIED_NODE);
			{
				GroupItem item = new GroupItem();
				item.setValue("true");
				item.setName(bundle("requireterms.title"));
				{
					EditBox box = new EditBox();
					box.setMandatory(true);
					box.setSize2(5);
					box.setTitle(bundle("requireterms.terms"));
					box.getTargetnodes().add(TERMS_NODE);
					item.getControls().add(box);
				}
				group.getGroups().add(item);
			}
			page.getControls().add(group);
		}

		return page;
	}

	private static LanguageBundle bundle(String key, Object... args)
	{
		return LangUtils.createTempLangugageBundle(key == null ? null : KEY_PREFIX + key, args);
	}

	public void writeItemxmlFromDrmSettings(PropBagEx itemxml, DrmSettings settings)
	{
		writeOwners(itemxml, settings);
		writeUsages(itemxml, settings);
		writeAccessControl(itemxml, settings);
	}

	private void writeAccessControl(PropBagEx itemxml, DrmSettings settings)
	{
		List<String> recipients = settings.getRestrictedToRecipients();
		if( recipients != null )
		{
			for( String recip : recipients )
			{
				USERSGROUPS_NODE.addNode(itemxml, recip);
			}
		}
		List<Triple<String, String, String>> ipranges = settings.getRestrictedToIpRanges();
		if( ipranges != null )
		{
			for( Triple<String, String, String> range : ipranges )
			{
				NETWORK_NODE.addNode(itemxml, range.getFirst() + "::" + range.getSecond() + "::" + range.getThird());
			}
		}
		int maxUsage = settings.getMaximumUsageCount();
		if( maxUsage > 0 )
		{
			COUNTSPECIFIED_NODE.addNode(itemxml, "true");
			COUNT_NODE.addNode(itemxml, Integer.toString(maxUsage));
		}
		Pair<Date, Date> dateRange = settings.getRestrictedToDateRange();
		if( dateRange != null )
		{
			DATESPECIFIED_NODE.addNode(itemxml, "true");
			Date start = dateRange.getFirst();
			if( start != null )
			{
				DATERANGE_NODE
					.addNode(itemxml, new LocalDate(start, CurrentTimeZone.get()).format(Dates.ISO_DATE_ONLY));
			}
			Date end = dateRange.getSecond();
			if( end != null )
			{
				DATERANGE_NODE.addNode(itemxml, new LocalDate(end, CurrentTimeZone.get()).format(Dates.ISO_DATE_ONLY));
			}
		}
		if( settings.isRestrictToSector() )
		{
			PURPOSE_NODE.addNode(itemxml, "sectors:educational");
		}
		if( settings.isAttributionOfOwnership() )
		{
			ATTRIBUTION_NODE.addNode(itemxml, "true");
		}
		String terms = settings.getTermsOfAgreement();
		if( terms != null )
		{
			TERMSSPECIFIED_NODE.addNode(itemxml, "true");
			TERMS_NODE.addNode(itemxml, terms);
		}
	}

	private void writeUsages(PropBagEx itemxml, DrmSettings settings)
	{
		Set<Usage> usages = Sets.newHashSet(settings.getUsagesAsEnum());
		String type = "custom";
		if( matchesExactly(usages, BASIC_USAGES) )
		{
			type = "basic";
		}
		if( matchesExactly(usages, ADAPT_USAGES) )
		{
			type = "adapt";
		}

		USAGE_OPTION_NODE.addNode(itemxml, type);
		if( type.equals("custom") )
		{
			for( Usage usage : usages )
			{
				USAGE_PERMISSION_NODE.addNode(itemxml, usage.toString());
			}
		}
	}

	private boolean matchesExactly(Set<Usage> usages, Usage... match)
	{
		if( usages.size() != match.length )
		{
			return false;
		}
		for( Usage usage : match )
		{
			if( !usages.contains(usage) )
			{
				return false;
			}
		}
		return true;
	}

	private void writeOwners(PropBagEx itemxml, DrmSettings settings)
	{
		List<Party> contentOwners = settings.getContentOwners();
		boolean owner = false;
		boolean other = false;
		for( Party party : contentOwners )
		{
			boolean thisOwner = party.isOwner();
			owner |= thisOwner;
			other |= !thisOwner;
			if( !thisOwner )
			{
				PropBagEx otherxml = OTHER_NODE.addNode(itemxml, "");
				OTHER_NAME_NODE.addNode(otherxml, party.getName());
				OTHER_EMAIL_NODE.addNode(otherxml, party.getEmail());
			}
		}
		String type = "myself";
		if( !owner )
		{
			type = "others";
		}
		else if( other )
		{
			type = "everyone";
		}
		OWNER_NODE.addNode(itemxml, type);
	}

	public DrmSettings readItemxmlIntoDrmSettings(PropBagEx itemxml, DRMPage page, String ownerUuid)
	{
		DrmSettings settings = new DrmSettings();
		drmService.mergeSettings(settings, page);
		readOwners(itemxml, settings, ownerUuid);
		readUsages(itemxml, settings);
		readAccessControl(itemxml, page, settings);
		settings.setDrmPageUuid(page.getUuid());
		return settings;
	}

	private void readAccessControl(PropBagEx itemxml, DRMPage page, DrmSettings settings)
	{
		Contributor contributor = page.getContributor();
		Container container = page.getContainer();

		List<String> recipients = new ArrayList<String>();
		if( contributor.hasRecipients() )
		{
			Collection<String> recips = USERSGROUPS_NODE.getValues(itemxml);
			recipients.addAll(recips);
			settings.setRestrictedToRecipients(recipients.size() > 0 ? recipients : null);
		}

		List<Triple<String, String, String>> ipranges = new ArrayList<Triple<String, String, String>>();
		if( !contributor.getNetworks().isEmpty() )
		{
			Collection<String> nets = NETWORK_NODE.getValues(itemxml);
			for( String network : nets )
			{
				int ind = network.indexOf("::");
				String name = network.substring(0, ind);
				int ind2 = network.indexOf("::", ind + 2);
				String min = network.substring(ind + 2, ind2);
				String max = network.substring(ind2 + 2);
				ipranges.add(new Triple<String, String, String>(name, min, max));
			}
			settings.setRestrictedToIpRanges(ipranges.size() > 0 ? ipranges : null);
		}

		int max = 0;
		if( contributor.isCount() )
		{
			if( COUNTSPECIFIED_NODE.getVal(itemxml, 0).equals("true") )
			{
				COUNTSPECIFIED_NODE.addNode(itemxml, "true");
				try
				{
					max = Integer.parseInt(COUNT_NODE.getVal(itemxml, 0));
				}
				catch( NumberFormatException nfe )
				{
					// ignore number format
				}
			}
		}
		else
		{
			max = container.getCount();
		}
		settings.setMaximumUsageCount(max);

		Pair<Date, Date> dateRange = null;
		if( contributor.isDatetime() )
		{
			if( DATESPECIFIED_NODE.getVal(itemxml, 0).equals("true") )
			{
				dateRange = new Pair<Date, Date>();
				try
				{
					dateRange.setFirst(new LocalDate(DATERANGE_NODE.getVal(itemxml, 0), Dates.ISO_DATE_ONLY,
						CurrentTimeZone.get()).toDate());
					dateRange.setSecond(new LocalDate(DATERANGE_NODE.getVal(itemxml, 1), Dates.ISO_DATE_ONLY,
						CurrentTimeZone.get()).toDate());
				}
				catch( ParseException e )
				{
					dateRange = null;
				}
			}
			settings.setRestrictedToDateRange(dateRange);
		}

		if( contributor.isSector() )
		{
			settings.setRestrictToSector(PURPOSE_NODE.getVal(itemxml, 0).equals("sectors:educational"));
		}
		else
		{
			settings.setRestrictToSector(container.isPurpose());
		}
		if( contributor.isAttribution() )
		{
			settings.setAttributionOfOwnership(ATTRIBUTION_NODE.getVal(itemxml, 0).equals("true"));
		}
		else
		{
			settings.setAttributionOfOwnership(page.isAttribution());
		}
		String terms = null;
		if( contributor.isTerms() )
		{
			if( TERMSSPECIFIED_NODE.getVal(itemxml, 0).equals("true") )
			{
				terms = TERMS_NODE.getVal(itemxml, 0);
			}
		}
		else
		{
			terms = page.getRemark();
		}
		settings.setTermsOfAgreement((terms != null && terms.length() > 0) ? terms : null);
	}

	private void readUsages(PropBagEx itemxml, DrmSettings settings)
	{
		List<Usage> usages = new ArrayList<Usage>();
		String type = USAGE_OPTION_NODE.getVal(itemxml, 0);
		if( type.equals("basic") )
		{
			usages.addAll(Arrays.asList(BASIC_USAGES));
		}
		else if( type.equals("adapt") )
		{
			usages.addAll(Arrays.asList(ADAPT_USAGES));
		}
		else
		{
			Collection<String> vals = USAGE_PERMISSION_NODE.getValues(itemxml);
			for( String usageVal : vals )
			{
				usages.add(Usage.valueOf(usageVal.toUpperCase()));
			}
		}
		settings.setUsagesFromEnum(usages);
	}

	private void readOwners(PropBagEx itemxml, DrmSettings settings, String ownerUuid)
	{
		List<Party> parties = new ArrayList<Party>();
		String type = OWNER_NODE.getVal(itemxml, 0);
		if( type.equals("myself") || type.equals("everyone") )
		{
			Party party = new Party();
			UserBean user = userService.getInformationForUser(ownerUuid);
			if( user != null )
			{
				party.setUserID(ownerUuid);
				party.setOwner(true);
				party.setEmail(user.getEmailAddress());
				party.setName(Format.format(user));
				parties.add(party);
			}
		}

		if( !type.equals("myself") )
		{
			PropBagIterator iter = OTHER_NODE.iterate(itemxml);
			while( iter.hasNext() )
			{
				Party party = new Party();
				PropBagEx other = iter.next();
				party.setName(OTHER_NAME_NODE.getVal(other, 0));
				party.setEmail(OTHER_EMAIL_NODE.getVal(other, 0));
				parties.add(party);
			}
		}
		settings.setContentOwners(parties);
	}
}
