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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.tle.beans.IdCloneable;

/**
 * A QTI 2.x question _reference_ (essentially a join between a test and a
 * question, but with metadata)
 * 
 * @author Aaron
 */
@Entity
@AccessType("field")
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"test_id", "uuid"}),
		@UniqueConstraint(columnNames = {"test_id", "identifier"})})
public class QtiAssessmentItemRef implements Serializable, IdCloneable
{
	private static final long serialVersionUID = 1;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Index(name = "qtiQRefUuidIndex")
	@Column(length = 40, nullable = false)
	private String uuid;

	/**
	 * Identifier as from original package. Not guaranteed to be globally
	 * unique, but guaranteed to be test unique.
	 */
	@Index(name = "qtiQRefIdentifierIndex")
	@Column(length = 255, nullable = false)
	private String identifier;

	// A.k.a href
	@Column(length = 1024, nullable = false)
	private String xmlPath;

	// technically unrestricted length...
	@Column(length = 1024)
	private String category;

	@Index(name = "qtiQRefTestIndex")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "test_id", insertable = false, updatable = false, nullable = false)
	private QtiAssessmentTest test;

	// TODO: Cascade everything but delete?
	@Index(name = "qtiQRefQuestionIndex")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	// @JoinColumn(name = "question_id", insertable = false, updatable = false,
	// nullable = false)
	private QtiAssessmentItem question;

	// more advanced fields follow. we currently read these out of the XML as
	// this all JQTI supports, and to implement the whole domain of QTI objects
	// would take forever and a day.

	// AssessmentItemRef fields
	// http://www.imsglobal.org/question/qtiv2p1/imsqti_infov2p1.html#element10520
	// private List<QtiWeight> weights;
	// private List<QtiVariableMapping> variableMappings;
	// private List<QtiTemplateDefault> templateDefaults;

	// sectionPart fields
	// http://www.imsglobal.org/question/qtiv2p1/imsqti_infov2p1.html#element10504
	// private boolean required;
	// private boolean fixed;
	// private List<PreCondition> preConditions;
	// private List<BranchRule> branchRules;
	// private ItemSessionControl sessionControl;
	// private TimeLimits timeLimits;

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

	public String getXmlPath()
	{
		return xmlPath;
	}

	public void setXmlPath(String xmlPath)
	{
		this.xmlPath = xmlPath;
	}

	public String getCategory()
	{
		return category;
	}

	public void setCategory(String category)
	{
		this.category = category;
	}

	public QtiAssessmentTest getTest()
	{
		return test;
	}

	public void setTest(QtiAssessmentTest test)
	{
		this.test = test;
	}

	public QtiAssessmentItem getQuestion()
	{
		return question;
	}

	public void setQuestion(QtiAssessmentItem question)
	{
		this.question = question;
	}
}
