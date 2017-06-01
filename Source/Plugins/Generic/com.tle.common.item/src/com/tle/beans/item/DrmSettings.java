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

package com.tle.beans.item;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Type;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.beans.EntityCloneable;
import com.tle.common.Pair;
import com.tle.common.Triple;

@Entity
@AccessType("field")
public class DrmSettings extends EntityCloneable implements Serializable
{
	private static final long serialVersionUID = 1;

	/**
	 * This value serves as an alternative to null or empty values in cases
	 * where no DRM Page exists, but DRM settings have been applied via a manual
	 * custom script.
	 */
	public static final String CUSTOM_SCRIPTED_DRMSETTINGS_PAGE_PLACEHOLDER = "CustomScriptedDrmSettings";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@OneToOne(mappedBy = "drmSettings")
	private Item item;

	/**
	 * These are the standard elements for ODRL. Simply toString().toLowerCase()
	 * for the xml element name.
	 */
	public enum Usage
	{
		DISPLAY, PRINT, PLAY, EXECUTE, AGGREGATE, MODIFY, EXCERPT, ANNOTATE;

		@Override
		public String toString()
		{
			return super.toString().toLowerCase();
		}
	}

	@Column(length = 40, nullable = false)
	private String drmPageUuid;

	@Column(name = "hlfo")
	private boolean hideLicencesFromOwner;

	@Column(name = "slc")
	private boolean showLicenceCount;

	@Column(name = "allowSum")
	private boolean allowSummary;

	@Column(name = "oma")
	private boolean ownerMustAccept;

	@Column(name = "smaiic")
	private boolean studentsMustAcceptIfInCompilation;

	@Column(name = "pa")
	private boolean previewAllowed;

	@Column(name = "rts")
	private boolean restrictToSector;

	@Column(name = "aoo")
	private boolean attributionOfOwnership;

	@Column(name = "ea")
	private boolean enforceAttribution;

	@SuppressWarnings("unused")
	@Column(name = "cuc")
	@Deprecated
	private int currentUsageCount;

	@Column(name = "muc")
	private int maximumUsageCount;

	@Type(type = "xstream_immutable")
	private List<Party> contentOwners;

	@Type(type = "csv")
	@Column(length = 70)
	private List<String> usages;

	@Type(type = "xstream_immutable")
	@Column(name = "rtorecip")
	private List<String> restrictedToRecipients;

	@Type(type = "xstream_immutable")
	@Column(name = "rtoips")
	private List<Triple<String, String, String>> restrictedToIpRanges;

	@Type(type = "xstream_immutable")
	@Column(name = "rtodates")
	private Pair<Date, Date> restrictedToDateRange;

	@Lob
	@Column(name = "terms")
	private String termsOfAgreement;

	/**
	 * A security rule-style expression indicating whom has to accept this DRM
	 * license. Null indicates everyone.
	 */
	@Lob
	@Column(name = "raf")
	private String requireAcceptanceFrom;

	public DrmSettings()
	{
		super();
	}

	public DrmSettings(Item item)
	{
		this.item = item;
	}

	public Item getItem()
	{
		return item;
	}

	public String getDrmPageUuid()
	{
		return drmPageUuid;
	}

	public void setDrmPageUuid(String drmPageUuid)
	{
		this.drmPageUuid = drmPageUuid;
	}

	public boolean isAllowSummary()
	{
		return allowSummary;
	}

	public void setAllowSummary(boolean allowSummary)
	{
		this.allowSummary = allowSummary;
	}

	public boolean isOwnerMustAccept()
	{
		return ownerMustAccept;
	}

	public void setOwnerMustAccept(boolean ownerMustAccept)
	{
		this.ownerMustAccept = ownerMustAccept;
	}

	public boolean isPreviewAllowed()
	{
		return previewAllowed;
	}

	public void setPreviewAllowed(boolean previewAllowed)
	{
		this.previewAllowed = previewAllowed;
	}

	public boolean isStudentsMustAcceptIfInCompilation()
	{
		return studentsMustAcceptIfInCompilation;
	}

	public void setStudentsMustAcceptIfInCompilation(boolean studentsMustAcceptIfInCompilation)
	{
		this.studentsMustAcceptIfInCompilation = studentsMustAcceptIfInCompilation;
	}

	public List<Party> getContentOwners()
	{
		if( contentOwners == null )
		{
			contentOwners = new ArrayList<Party>();
		}
		return contentOwners;
	}

	public void setContentOwners(List<Party> contentOwners)
	{
		this.contentOwners = ensureArrayList(contentOwners);
	}

	private <T> List<T> ensureArrayList(List<T> list)
	{
		if( list == null )
		{
			return null;
		}
		if( !(list instanceof ArrayList) )
		{
			return Lists.newArrayList(list);
		}
		return list;
	}

	public List<String> getUsages()
	{
		return usages;
	}

	public void setUsages(List<String> usages)
	{
		this.usages = ensureArrayList(usages);
	}

	public List<String> getUsageStrings()
	{
		if( usages == null )
		{
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(usages);
	}

	public List<Usage> getUsagesAsEnum()
	{
		if( usages == null )
		{
			return Collections.emptyList();
		}
		return Lists.transform(usages, new Function<String, Usage>()
		{
			@Override
			public Usage apply(String input)
			{
				return Usage.valueOf(input);
			}
		});
	}

	public void setUsagesFromEnum(List<Usage> usagesEnum)
	{
		if( usages == null )
		{
			usages = Lists.newArrayListWithExpectedSize(usagesEnum.size());
		}
		else
		{
			usages.clear();
		}
		for( Usage usage : usagesEnum )
		{
			usages.add(usage.name());
		}
	}

	public boolean isAttributionOfOwnership()
	{
		return attributionOfOwnership;
	}

	public void setAttributionOfOwnership(boolean attributionOfOwnership)
	{
		this.attributionOfOwnership = attributionOfOwnership;
	}

	public boolean isEnforceAttribution()
	{
		return enforceAttribution;
	}

	public void setEnforceAttribution(boolean enforceAttribution)
	{
		this.enforceAttribution = enforceAttribution;
	}

	public Pair<Date, Date> getRestrictedToDateRange()
	{
		return restrictedToDateRange;
	}

	public void setRestrictedToDateRange(Pair<Date, Date> restrictedToDateRange)
	{
		this.restrictedToDateRange = restrictedToDateRange;
	}

	public List<Triple<String, String, String>> getRestrictedToIpRanges()
	{
		return restrictedToIpRanges;
	}

	public void setRestrictedToIpRanges(List<Triple<String, String, String>> restrictedToIpRanges)
	{
		this.restrictedToIpRanges = ensureArrayList(restrictedToIpRanges);
	}

	public List<String> getRestrictedToRecipients()
	{
		return restrictedToRecipients;
	}

	public void setRestrictedToRecipients(List<String> restrictedToRecipients)
	{
		this.restrictedToRecipients = ensureArrayList(restrictedToRecipients);
	}

	public boolean isRestrictToSector()
	{
		return restrictToSector;
	}

	public void setRestrictToSector(boolean restrictToSector)
	{
		this.restrictToSector = restrictToSector;
	}

	public String getTermsOfAgreement()
	{
		return termsOfAgreement;
	}

	public void setTermsOfAgreement(String termsOfAgreement)
	{
		this.termsOfAgreement = termsOfAgreement;
	}

	public int getMaximumUsageCount()
	{
		return maximumUsageCount;
	}

	public void setMaximumUsageCount(int maximumUsageCount)
	{
		this.maximumUsageCount = maximumUsageCount;
	}

	public static class Party implements Serializable
	{
		private static final long serialVersionUID = 1;

		private String userID;
		private String name;
		private String email;
		private boolean owner;

		public Party()
		{
			super();
		}

		public String getEmail()
		{
			if( email == null )
			{
				email = ""; //$NON-NLS-1$
			}
			return email;
		}

		public void setEmail(String email)
		{
			this.email = email;
		}

		public String getName()
		{
			if( name == null )
			{
				name = ""; //$NON-NLS-1$
			}
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getUserID()
		{
			if( userID == null )
			{
				userID = ""; //$NON-NLS-1$
			}
			return userID;
		}

		public void setUserID(String uid)
		{
			this.userID = uid;
		}

		public boolean isOwner()
		{
			return owner;
		}

		public void setOwner(boolean owner)
		{
			this.owner = owner;
		}
	}

	@Override
	public long getId()
	{
		return id;
	}

	@Override
	public void setId(long id)
	{
		this.id = id;
	}

	public void setItem(Item item)
	{
		this.item = item;
	}

	public boolean isHideLicencesFromOwner()
	{
		return hideLicencesFromOwner;
	}

	public void setHideLicencesFromOwner(boolean hideLicencesFromOwner)
	{
		this.hideLicencesFromOwner = hideLicencesFromOwner;
	}

	public boolean isShowLicenceCount()
	{
		return showLicenceCount;
	}

	public void setShowLicenceCount(boolean showLicenceCount)
	{
		this.showLicenceCount = showLicenceCount;
	}

	public String getRequireAcceptanceFrom()
	{
		return requireAcceptanceFrom;
	}

	public void setRequireAcceptanceFrom(String requireAcceptanceFrom)
	{
		this.requireAcceptanceFrom = requireAcceptanceFrom;
	}

	public boolean addUsage(Usage use)
	{
		String useStr = use.name();
		if( usages == null )
		{
			usages = Lists.newArrayList();
		}
		else if( usages.contains(useStr) )
		{
			return false;
		}
		return usages.add(useStr);
	}

	public boolean removeUsage(Usage use)
	{
		if( usages == null )
		{
			return false;
		}
		return usages.remove(use.name());
	}
}
