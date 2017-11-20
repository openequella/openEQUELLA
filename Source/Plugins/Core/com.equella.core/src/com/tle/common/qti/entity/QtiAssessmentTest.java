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

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.IndexColumn;

import com.google.common.collect.Lists;
import com.tle.beans.IdCloneable;
import com.tle.beans.Institution;
import com.tle.beans.item.Item;

/**
 * A QTI 2.x QUIZ a.k.a. AssessmentTest
 * http://www.imsglobal.org/question/qtiv2p1/imsqti_infov2p1.html#element10473
 * 
 * @author Aaron
 */
@Entity
@AccessType("field")
@Table(uniqueConstraints = {@UniqueConstraint(name = "qtiTestUniqueUuid", columnNames = {"institution_id", "uuid"}),
		@UniqueConstraint(name = "qtiTestLocalIdentifier", columnNames = {"item_id", "identifier"})})
public class QtiAssessmentTest implements Serializable, IdCloneable
{
	private static final long serialVersionUID = 1;

	/*
	 * Do not re-order this enum!
	 */
	public enum NavigationMode
	{
		NONLINEAR, LINEAR
	}

	/*
	 * Do not re-order this enum!
	 */
	public enum SubmissionMode
	{
		INDIVIDUAL, SIMULTANEOUS
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Index(name = "qtiTestUuidIndex")
	@Column(length = 40, nullable = false)
	private String uuid;

	/**
	 * Identifier as from original package. Not guaranteed to be globally
	 * unique. Note the QTI spec says this is an unlimited string, yet other
	 * identifiers are limited to 32 chars?
	 */
	@Index(name = "qtiTestIdentifierIndex")
	@Column(length = 1024, nullable = false)
	private String identifier;

	/**
	 * We don't technically need this, it's included in the item, however I want
	 * a unique constraint and easy retrieval of all for institution. Besides,
	 * we probably won't be locked to items in the future.
	 */
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@Index(name = "qtiTestInstitutionIndex")
	private Institution institution;

	@OneToOne(optional = false, fetch = FetchType.LAZY)
	@Index(name = "qtiTestItemIndex")
	private Item item;

	@Column(length = 1024, nullable = false)
	private String xmlPath;

	@Lob
	@Column(nullable = false)
	private String title;

	@OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
	@Fetch(value = FetchMode.SUBSELECT)
	@JoinColumn(name = "test_id", nullable = false)
	@IndexColumn(name = "list_position", nullable = false)
	private List<QtiAssessmentItemRef> questionRefs = Lists.newArrayList();

	// private String toolName;
	// private String toolVersion;
	// private List<QtiOutcomeDeclaration> outcomes;
	// private QtiTimeLimit timeLimit;

	// private QtiOutcomeProcessing outcomeProcessing;
	// private QtiTestFeedback feedback;

	// NOTE! We only support 1 test part, as does JQTI. We store the testPart's
	// fields in the test
	// private List<QtiTestPart> testParts;

	// Test part fields included below:
	// http://www.imsglobal.org/question/qtiv2p1/imsqti_infov2p1.html#element10489
	@Column(length = 256, nullable = false)
	private String testPartIdentifier;

	// linear or nonlinear
	@Column(nullable = false)
	private int navigationMode;
	// individual or simultaneous
	@Column(nullable = false)
	private int submissionMode;

	// Since we only support one part, these are redundant
	// private List<QtiPreCondition> preConditions;
	// private List<QtiBranchRule> branchRules;

	// private QtiItemSessionControl itemSessionControl;
	// This overrides any timeLimit in the test
	// private QtiTimeLimit timeLimit;

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

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public void setIdentifier(String identifier)
	{
		this.identifier = identifier;
	}

	public Institution getInstitution()
	{
		return institution;
	}

	public void setInstitution(Institution institution)
	{
		this.institution = institution;
	}

	public Item getItem()
	{
		return item;
	}

	public void setItem(Item item)
	{
		this.item = item;
	}

	public String getXmlPath()
	{
		return xmlPath;
	}

	public void setXmlPath(String xmlPath)
	{
		this.xmlPath = xmlPath;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public List<QtiAssessmentItemRef> getQuestionRefs()
	{
		return questionRefs;
	}

	public void setQuestionRefs(List<QtiAssessmentItemRef> questionRefs)
	{
		this.questionRefs = questionRefs;
	}

	public String getTestPartIdentifier()
	{
		return testPartIdentifier;
	}

	public void setTestPartIdentifier(String testPartIdentifier)
	{
		this.testPartIdentifier = testPartIdentifier;
	}

	public NavigationMode getNavigationMode()
	{
		return NavigationMode.values()[navigationMode];
	}

	public void setNavigationMode(NavigationMode navigationMode)
	{
		this.navigationMode = navigationMode.ordinal();
	}

	public SubmissionMode getSubmissionMode()
	{
		return SubmissionMode.values()[submissionMode];
	}

	public void setSubmissionMode(SubmissionMode submissionMode)
	{
		this.submissionMode = submissionMode.ordinal();
	}
}
