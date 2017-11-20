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

package com.tle.web.api.item.interfaces.beans;

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.web.api.interfaces.beans.AbstractExtendableBean;

/**
 * @author Aaron
 */
public class DrmOptionsBean extends AbstractExtendableBean
{
	private String drmPageUuid;
	private boolean hideLicencesFromOwner;
	private boolean showLicenceCount;
	private boolean allowSummary;
	private boolean ownerMustAccept;
	private boolean studentsMustAcceptIfInCompilation;
	private boolean previewAllowed;
	private boolean attributionOfOwnership;
	private boolean enforceAttribution;
	private List<DrmPartyBean> contentOwners;
	private List<String> usages;
	private String termsOfAgreement;
	private String requireAcceptanceFrom;
	// Just a way of grouping fields
	private DrmRestrictionBean restriction;
	private List<DrmAcceptanceBean> acceptances;

	public String getDrmPageUuid()
	{
		return drmPageUuid;
	}

	public void setDrmPageUuid(String drmPageUuid)
	{
		this.drmPageUuid = drmPageUuid;
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

	public boolean isStudentsMustAcceptIfInCompilation()
	{
		return studentsMustAcceptIfInCompilation;
	}

	public void setStudentsMustAcceptIfInCompilation(boolean studentsMustAcceptIfInCompilation)
	{
		this.studentsMustAcceptIfInCompilation = studentsMustAcceptIfInCompilation;
	}

	public boolean isPreviewAllowed()
	{
		return previewAllowed;
	}

	public void setPreviewAllowed(boolean previewAllowed)
	{
		this.previewAllowed = previewAllowed;
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

	public List<DrmPartyBean> getContentOwners()
	{
		return contentOwners;
	}

	public void setContentOwners(List<DrmPartyBean> contentOwners)
	{
		this.contentOwners = contentOwners;
	}

	public List<String> getUsages()
	{
		return usages;
	}

	public void setUsages(List<String> usages)
	{
		this.usages = usages;
	}

	public String getTermsOfAgreement()
	{
		return termsOfAgreement;
	}

	public void setTermsOfAgreement(String termsOfAgreement)
	{
		this.termsOfAgreement = termsOfAgreement;
	}

	public String getRequireAcceptanceFrom()
	{
		return requireAcceptanceFrom;
	}

	public void setRequireAcceptanceFrom(String requireAcceptanceFrom)
	{
		this.requireAcceptanceFrom = requireAcceptanceFrom;
	}

	public DrmRestrictionBean getRestriction()
	{
		return restriction;
	}

	public void setRestriction(DrmRestrictionBean restriction)
	{
		this.restriction = restriction;
	}

	public List<DrmAcceptanceBean> getAcceptances()
	{
		return acceptances;
	}

	public void setAcceptances(List<DrmAcceptanceBean> acceptances)
	{
		this.acceptances = acceptances;
	}

	@XmlRootElement
	public static class DrmRestrictionBean
	{
		private List<DrmNetworkBean> network;
		private Date startDate;
		private Date endDate;
		private List<String> usersExpression;
		private boolean educationalSector;
		private int maximumUsage;

		public List<DrmNetworkBean> getNetwork()
		{
			return network;
		}

		public void setNetwork(List<DrmNetworkBean> network)
		{
			this.network = network;
		}

		public Date getStartDate()
		{
			return startDate;
		}

		public void setStartDate(Date startDate)
		{
			this.startDate = startDate;
		}

		public Date getEndDate()
		{
			return endDate;
		}

		public void setEndDate(Date endDate)
		{
			this.endDate = endDate;
		}

		public List<String> getUsersExpression()
		{
			return usersExpression;
		}

		public void setUsersExpression(List<String> usersExpression)
		{
			this.usersExpression = usersExpression;
		}

		public boolean isEducationalSector()
		{
			return educationalSector;
		}

		public void setEducationalSector(boolean educationalSector)
		{
			this.educationalSector = educationalSector;
		}

		public int getMaximumUsage()
		{
			return maximumUsage;
		}

		public void setMaximumUsage(int maximumUsage)
		{
			this.maximumUsage = maximumUsage;
		}
	}
}
