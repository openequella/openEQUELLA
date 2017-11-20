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

package com.tle.core.remoterepo.merlot.service.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.dytech.devlib.PropBagEx;
import com.tle.core.fedsearch.GenericRecord;
import com.tle.core.fedsearch.RemoteRepoSearchResult;

/**
 * @author aholland
 */
public class MerlotSearchResult extends RemoteRepoSearchResult implements GenericRecord
{
	private static final long serialVersionUID = 1L;

	private String authorName;
	private String detailUrl;
	private List<String> categories;
	private String community;
	private List<String> languages;
	private String materialType;
	private String technicalRequirements;
	private List<String> audiences;
	private String creativeCommons;
	private Date modifiedDate;
	private String submitter;
	private String copyright;
	private String cost;
	private String section508Compliant;
	private String sourceAvailable;
	private String peerReviewUrl;
	private float peerReviewRating;
	private String commentsUrl;
	private float commentsRating;
	private int commentsCount;
	private String personalCollectionsUrl;
	private int personalCollectionsCount;
	private String learningExercisesUrl;
	private int learningExercisesCount;
	private PropBagEx xml;

	public MerlotSearchResult(int index)
	{
		super(index);
	}

	public String getAuthorName()
	{
		return authorName;
	}

	public void setAuthorName(String authorName)
	{
		this.authorName = authorName;
	}

	public String getDetailUrl()
	{
		return detailUrl;
	}

	public void setDetailUrl(String detailUrl)
	{
		this.detailUrl = detailUrl;
	}

	public String getCreativeCommons()
	{
		return creativeCommons;
	}

	public void setCreativeCommons(String creativeCommons)
	{
		this.creativeCommons = creativeCommons;
	}

	public Date getModifiedDate()
	{
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate)
	{
		this.modifiedDate = modifiedDate;
	}

	public String getSubmitter()
	{
		return submitter;
	}

	public void setSubmitter(String submitter)
	{
		this.submitter = submitter;
	}

	public String getCopyright()
	{
		return copyright;
	}

	public void setCopyright(String copyright)
	{
		this.copyright = copyright;
	}

	public String getCost()
	{
		return cost;
	}

	public void setCost(String cost)
	{
		this.cost = cost;
	}

	public String getSection508Compliant()
	{
		return section508Compliant;
	}

	public void setSection508Compliant(String section508Compliant)
	{
		this.section508Compliant = section508Compliant;
	}

	public String getSourceAvailable()
	{
		return sourceAvailable;
	}

	public void setSourceAvailable(String sourceAvailable)
	{
		this.sourceAvailable = sourceAvailable;
	}

	public String getPeerReviewUrl()
	{
		return peerReviewUrl;
	}

	public void setPeerReviewUrl(String peerReviewUrl)
	{
		this.peerReviewUrl = peerReviewUrl;
	}

	public float getPeerReviewRating()
	{
		return peerReviewRating;
	}

	public void setPeerReviewRating(float peerReviewRating)
	{
		this.peerReviewRating = peerReviewRating;
	}

	public String getCommentsUrl()
	{
		return commentsUrl;
	}

	public void setCommentsUrl(String commentsUrl)
	{
		this.commentsUrl = commentsUrl;
	}

	public float getCommentsRating()
	{
		return commentsRating;
	}

	public void setCommentsRating(float commentsRating)
	{
		this.commentsRating = commentsRating;
	}

	public int getCommentsCount()
	{
		return commentsCount;
	}

	public void setCommentsCount(int commentsCount)
	{
		this.commentsCount = commentsCount;
	}

	public String getPersonalCollectionsUrl()
	{
		return personalCollectionsUrl;
	}

	public void setPersonalCollectionsUrl(String personalCollectionsUrl)
	{
		this.personalCollectionsUrl = personalCollectionsUrl;
	}

	public int getPersonalCollectionsCount()
	{
		return personalCollectionsCount;
	}

	public void setPersonalCollectionsCount(int personalCollectionsCount)
	{
		this.personalCollectionsCount = personalCollectionsCount;
	}

	public String getLearningExercisesUrl()
	{
		return learningExercisesUrl;
	}

	public void setLearningExercisesUrl(String learningExercisesUrl)
	{
		this.learningExercisesUrl = learningExercisesUrl;
	}

	public int getLearningExercisesCount()
	{
		return learningExercisesCount;
	}

	public void setLearningExercisesCount(int learningExercisesCount)
	{
		this.learningExercisesCount = learningExercisesCount;
	}

	public List<String> getCategories()
	{
		return categories;
	}

	public void setCategories(List<String> categories)
	{
		this.categories = categories;
	}

	public String getCommunity()
	{
		return community;
	}

	public void setCommunity(String community)
	{
		this.community = community;
	}

	public List<String> getLanguages()
	{
		return languages;
	}

	public void setLanguages(List<String> languages)
	{
		this.languages = languages;
	}

	public String getMaterialType()
	{
		return materialType;
	}

	public void setMaterialType(String materialType)
	{
		this.materialType = materialType;
	}

	public String getTechnicalRequirements()
	{
		return technicalRequirements;
	}

	public void setTechnicalRequirements(String technicalRequirements)
	{
		this.technicalRequirements = technicalRequirements;
	}

	public List<String> getAudiences()
	{
		return audiences;
	}

	public void setAudiences(List<String> audiences)
	{
		this.audiences = audiences;
	}

	@Override
	public String getIsbn()
	{
		return null;
	}

	@Override
	public String getIssn()
	{
		return null;
	}

	@Override
	public String getLccn()
	{
		return null;
	}

	@Override
	public String getUri()
	{
		return getUrl();
	}

	@Override
	public Collection<String> getAuthors()
	{
		return Collections.singleton(getAuthorName());
	}

	public void setXml(PropBagEx xml)
	{
		this.xml = xml;
	}

	@Override
	public PropBagEx getXml()
	{
		return xml;
	}

	@Override
	public String getPhysicalDescription()
	{
		return getDescription();
	}

	@Override
	public String getType()
	{
		return getMaterialType();
	}
}
