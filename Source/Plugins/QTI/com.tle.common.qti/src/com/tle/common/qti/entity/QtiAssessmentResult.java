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

package com.tle.common.qti.entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.IndexColumn;

import com.google.common.collect.Lists;

/**
 * See assessmentResult
 * http://www.imsglobal.org/question/qtiv2p1/imsqti_resultv2p1.html#element10803
 * <p>
 * Additionally, since there is a one-to-one relationship between
 * assessmentResult and context, as well as testResult, I've rolled those fields
 * into the same object
 * </p>
 * <p>
 * See context http://www.imsglobal.org/question/qtiv2p1/imsqti_resultv2p1.html#
 * element10807
 * </p>
 * <p>
 * See testResult
 * http://www.imsglobal.org/question/qtiv2p1/imsqti_resultv2p1.html#element10814
 * </p>
 * 
 * @author Aaron
 */
@Entity
@AccessType("field")
public class QtiAssessmentResult extends QtiAbstractResult
{
	private static final long serialVersionUID = 1;

	// No mention of what the max length of this is
	@Index(name = "qtiAssessresultResIdx")
	@Column(length = 1024, nullable = false)
	private String resourceLinkId;

	// --- context fields ---
	// http://www.imsglobal.org/question/qtiv2p1/imsqti_resultv2p1.html#element10807

	// Aka context.sourcedId
	// Spec says this can be up to 2048, but Oracle bitches about 2000+.
	// Let's be realistic about it
	@Index(name = "qtiAssessresultUserIdx")
	@Column(length = 255, nullable = false)
	private String userId;

	// tool_consumer_instance_guid
	@Index(name = "qtiAssessresultLmsIdx")
	@Column(length = 255)
	private String lmsInstanceId;

	// private List<String> sessionIdentifiers;

	// --- testResult fields ---
	@Index(name = "qtiAssessresultTestIdx")
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private QtiAssessmentTest test;

	@IndexColumn(name = "resindex", nullable = false)
	@JoinColumn(name = "assessment_result_id", nullable = false)
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<QtiItemResult> itemResults = Lists.newArrayList();

	/**
	 * XML representation of the TestSessionState as returned by
	 * TestSessionStateXmlMarshaller
	 */
	@Lob
	private String testSessionState;

	public String getResourceLinkId()
	{
		return resourceLinkId;
	}

	public void setResourceLinkId(String resourceLinkId)
	{
		this.resourceLinkId = resourceLinkId;
	}

	public String getLmsInstanceId()
	{
		return lmsInstanceId;
	}

	public void setLmsInstanceId(String lmsInstanceId)
	{
		this.lmsInstanceId = lmsInstanceId;
	}

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public QtiAssessmentTest getTest()
	{
		return test;
	}

	public void setTest(QtiAssessmentTest test)
	{
		this.test = test;
	}

	public List<QtiItemResult> getItemResults()
	{
		return itemResults;
	}

	public void setItemResults(List<QtiItemResult> itemResults)
	{
		this.itemResults = itemResults;
	}

	public String getTestSessionState()
	{
		return testSessionState;
	}

	public void setTestSessionState(String testSessionState)
	{
		this.testSessionState = testSessionState;
	}
}
