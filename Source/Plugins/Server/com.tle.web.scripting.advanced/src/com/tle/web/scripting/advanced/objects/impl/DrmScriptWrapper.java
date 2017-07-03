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

package com.tle.web.scripting.advanced.objects.impl;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.beans.item.DrmSettings;
import com.tle.beans.item.DrmSettings.Party;
import com.tle.beans.item.DrmSettings.Usage;
import com.tle.beans.item.Item;
import com.tle.common.Check;
import com.tle.common.Format;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.services.user.UserService;
import com.tle.web.scripting.advanced.objects.DrmScriptObject;
import com.tle.web.scripting.advanced.types.DrmPartyScriptType;
import com.tle.web.scripting.advanced.types.DrmSettingsScriptType;
import com.tle.web.scripting.impl.AbstractScriptWrapper;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public class DrmScriptWrapper extends AbstractScriptWrapper implements DrmScriptObject
{
	private static final long serialVersionUID = 1L;

	private final DefaultDrmSettingsScriptType drm;
	private final UserService userService;
	private final Item item;

	public DrmScriptWrapper(UserService userService, Item item, DrmSettings drmSettings)
	{
		this.userService = userService;
		this.item = item;
		this.drm = new DefaultDrmSettingsScriptType(drmSettings);
	}

	@Override
	public DrmSettingsScriptType getSettings()
	{
		return drm;
	}

	@Override
	public DrmPartyScriptType createPartyFromUserId(String userId, boolean owner)
	{
		Party partyAndPartyAndYeah = new Party();
		partyAndPartyAndYeah.setUserID("tle:" + userId);

		UserBean user = userService.getInformationForUser(userId);
		if( user != null )
		{
			partyAndPartyAndYeah.setEmail(user.getEmailAddress());
			partyAndPartyAndYeah.setName(Format.format(user, Format.DEFAULT_USER_BEAN_FORMAT));
		}
		partyAndPartyAndYeah.setOwner(owner);

		return new DefaultDrmPartyScriptType(partyAndPartyAndYeah);
	}

	@Override
	public DrmPartyScriptType createParty(String name, String emailAddress, boolean owner)
	{
		Party partyAndPartyAndYeah = new Party();
		partyAndPartyAndYeah.setName(name);
		partyAndPartyAndYeah.setEmail(emailAddress);
		partyAndPartyAndYeah.setOwner(owner);
		return new DefaultDrmPartyScriptType(partyAndPartyAndYeah);
	}

	@Override
	public void scriptExit()
	{
		super.scriptExit();

		// if there are any values set in the local drm object, preserve them
		DrmSettings localSettings = drm.getSettings();

		if( drm.isChanged() )
		{
			if( Check.isEmpty(localSettings.getDrmPageUuid()) )
			{
				localSettings.setDrmPageUuid(DrmSettings.CUSTOM_SCRIPTED_DRMSETTINGS_PAGE_PLACEHOLDER);
			}
			if( item.getDrmSettings() == null )
			{
				item.setDrmSettings(localSettings);
			}
		}
	}

	public class DefaultDrmSettingsScriptType implements DrmSettingsScriptType
	{
		private static final long serialVersionUID = 1L;

		private final DrmSettings settings;
		private List<DrmPartyScriptType> owners;
		private boolean changed;

		public DefaultDrmSettingsScriptType(DrmSettings settings)
		{
			this.settings = settings;
		}

		protected DrmSettings getSettings()
		{
			return settings;
		}

		protected boolean isChanged()
		{
			return changed;
		}

		protected void setChanged()
		{
			changed = true;
		}

		@Override
		@SuppressWarnings("deprecation")
		public List<DrmPartyScriptType> getContentOwners()
		{
			if( owners == null )
			{
				owners = Lists
					.newArrayList(Lists.transform(settings.getContentOwners(), new Function<Party, DrmPartyScriptType>()
					{
						@Override
						public DrmPartyScriptType apply(Party party)
						{
							return new DefaultDrmPartyScriptType(DefaultDrmSettingsScriptType.this, party);
						}
					}));
			}
			return owners;
		}

		@Override
		public void setContentOwners(List<DrmPartyScriptType> contentOwners)
		{
			changed = true;
			owners = contentOwners;

			List<Party> partyContentOwners = Lists
				.newArrayList(Lists.transform(contentOwners, new Function<DrmPartyScriptType, Party>()
				{
					@Override
					public Party apply(DrmPartyScriptType drmPartyScriptType)
					{
						DefaultDrmPartyScriptType scriptParty = ((DefaultDrmPartyScriptType) drmPartyScriptType);
						scriptParty.setOwnerSettings(DefaultDrmSettingsScriptType.this);
						return scriptParty.getWrapped();
					}
				}));
			settings.setContentOwners(partyContentOwners);
		}

		@Override
		public void addContentOwner(DrmPartyScriptType party)
		{
			changed = true;
			DefaultDrmPartyScriptType scriptParty = (DefaultDrmPartyScriptType) party;
			scriptParty.setOwnerSettings(this);
			if( owners != null )
			{
				owners.add(party);
			}
			settings.getContentOwners().add(scriptParty.getWrapped());
		}

		@Override
		public int getMaximumUsageCount()
		{
			return settings.getMaximumUsageCount();
		}

		@Override
		public void setMaximumUsageCount(int maximumUsageCount)
		{
			changed = true;
			settings.setMaximumUsageCount(maximumUsageCount);
		}

		@Override
		public String getRequireAcceptanceFrom()
		{
			return settings.getRequireAcceptanceFrom();
		}

		@Override
		public void setRequireAcceptanceFrom(String requireAcceptanceFrom)
		{
			changed = true;
			settings.setRequireAcceptanceFrom(requireAcceptanceFrom);
		}

		@Override
		public List<String> getRestrictedToRecipients()
		{
			return settings.getRestrictedToRecipients();
		}

		@Override
		public void setRestrictedToRecipients(List<String> restrictedToRecipients)
		{
			changed = true;
			settings.setRestrictedToRecipients(restrictedToRecipients);
		}

		@Override
		public String getTermsOfAgreement()
		{
			return settings.getTermsOfAgreement();
		}

		@Override
		public void setTermsOfAgreement(String termsOfAgreement)
		{
			changed = true;
			settings.setTermsOfAgreement(termsOfAgreement);
		}

		@Override
		public List<String> getUsages()
		{
			return settings.getUsageStrings();
		}

		@Override
		@SuppressWarnings("deprecation")
		public void setUsages(List<String> usages)
		{
			changed = true;
			List<Usage> usageUsages = Lists.newArrayList(Lists.transform(usages, new Function<String, Usage>()
			{
				@Override
				public Usage apply(String string)
				{
					return Usage.valueOf(string.toUpperCase());
				}
			}));
			settings.setUsagesFromEnum(usageUsages);
		}

		@Override
		public boolean addUsage(String usage)
		{
			Usage use = Usage.valueOf(usage.toUpperCase());
			boolean added = settings.addUsage(use);
			if( added )
			{
				changed = true;
			}
			return added;
		}

		@Override
		public boolean removeUsage(String usage)
		{
			changed = true;
			Usage use = Usage.valueOf(usage.toUpperCase());
			return settings.removeUsage(use);
		}

		@Override
		public boolean isAllowSummary()
		{
			return settings.isAllowSummary();
		}

		@Override
		public void setAllowSummary(boolean allowSummary)
		{
			changed = true;
			settings.setAllowSummary(allowSummary);
		}

		@Override
		public boolean isAttributionOfOwnership()
		{
			return settings.isAttributionOfOwnership();
		}

		@Override
		public void setAttributionOfOwnership(boolean attributionOfOwnership)
		{
			changed = true;
			settings.setAttributionOfOwnership(attributionOfOwnership);
		}

		@Override
		public boolean isEnforceAttribution()
		{
			return settings.isEnforceAttribution();
		}

		@Override
		public void setEnforceAttribution(boolean enforceAttribution)
		{
			changed = true;
			settings.setEnforceAttribution(enforceAttribution);
		}

		@Override
		public boolean isHideLicencesFromOwner()
		{
			return settings.isHideLicencesFromOwner();
		}

		@Override
		public void setHideLicencesFromOwner(boolean hideLicencesFromOwner)
		{
			changed = true;
			settings.setHideLicencesFromOwner(hideLicencesFromOwner);
		}

		@Override
		public boolean isOwnerMustAccept()
		{
			return settings.isOwnerMustAccept();
		}

		@Override
		public void setOwnerMustAccept(boolean ownerMustAccept)
		{
			changed = true;
			settings.setOwnerMustAccept(ownerMustAccept);
		}

		@Override
		public boolean isPreviewAllowed()
		{
			return settings.isPreviewAllowed();
		}

		@Override
		public void setPreviewAllowed(boolean previewAllowed)
		{
			changed = true;
			settings.setPreviewAllowed(previewAllowed);
		}

		@Override
		public boolean isRestrictToSector()
		{
			return settings.isRestrictToSector();
		}

		@Override
		public void setRestrictToSector(boolean restrictToSector)
		{
			changed = true;
			settings.setRestrictToSector(restrictToSector);
		}

		@Override
		public boolean isShowLicenceCount()
		{
			return settings.isShowLicenceCount();
		}

		@Override
		public void setShowLicenceCount(boolean showLicenceCount)
		{
			changed = true;
			settings.setShowLicenceCount(showLicenceCount);
		}

		@Override
		public boolean isStudentsMustAcceptIfInCompilation()
		{
			return settings.isStudentsMustAcceptIfInCompilation();
		}

		@Override
		public void setStudentsMustAcceptIfInCompilation(boolean studentsMustAcceptIfInCompilation)
		{
			changed = true;
			settings.setStudentsMustAcceptIfInCompilation(studentsMustAcceptIfInCompilation);
		}
	}

	public static class DefaultDrmPartyScriptType implements DrmPartyScriptType
	{
		private static final long serialVersionUID = 1L;

		private final Party party;
		private DefaultDrmSettingsScriptType ownerSettings;
		private boolean changed;

		public DefaultDrmPartyScriptType(DefaultDrmSettingsScriptType ownerSettings, Party party)
		{
			this(party);
			this.ownerSettings = ownerSettings;
		}

		public DefaultDrmPartyScriptType(Party party)
		{
			this.party = party;
		}

		protected void setOwnerSettings(DefaultDrmSettingsScriptType ownerSettings)
		{
			this.ownerSettings = ownerSettings;
			if( changed )
			{
				ownerSettings.setChanged();
			}
		}

		protected boolean isChanged()
		{
			return changed;
		}

		protected void setChanged()
		{
			changed = true;
			if( ownerSettings != null )
			{
				ownerSettings.setChanged();
			}
		}

		@Override
		public String getEmail()
		{
			return party.getEmail();
		}

		@Override
		public String getName()
		{
			return party.getName();
		}

		@Override
		public String getUserID()
		{
			return party.getUserID();
		}

		@Override
		public boolean isOwner()
		{
			return party.isOwner();
		}

		@Override
		public void setEmail(String email)
		{
			setChanged();
			party.setEmail(email);
		}

		@Override
		public void setName(String name)
		{
			setChanged();
			party.setName(name);
		}

		@Override
		public void setOwner(boolean owner)
		{
			setChanged();
			party.setOwner(owner);
		}

		@Override
		public void setUserID(String uid)
		{
			setChanged();
			party.setUserID(uid);
		}

		/**
		 * Never use in scripts!
		 * 
		 * @return
		 */
		protected Party getWrapped()
		{
			return party;
		}
	}
}
