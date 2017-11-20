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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.tle.beans.IdCloneable;
import com.tle.beans.Institution;

/**
 * A QTI 2.x QUESTION a.k.a. AssessmentItem
 * http://www.imsglobal.org/question/qtiv2p1/imsqti_infov2p1.html#element10012
 * 
 * @author Aaron
 */
@Entity
@AccessType("field")
@Table(uniqueConstraints = {@UniqueConstraint(name = "qtiItemUniqueUuid", columnNames = {"institution_id", "uuid"})})
public class QtiAssessmentItem implements Serializable, IdCloneable
{
	private static final long serialVersionUID = 1;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Index(name = "qtiQuestionUuidIndex")
	@Column(length = 40, nullable = false)
	private String uuid;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@Index(name = "qtiQstInstitutionIndex")
	private Institution institution;

	/**
	 * Identifier as from original package. Note the QTI spec says this is an
	 * unlimited string, yet other identifiers are limited to 32 chars?
	 */
	@Index(name = "qtiQuestionIdentifierIndex")
	@Column(length = 1024, nullable = false)
	private String identifier;

	@Lob
	@Column(nullable = false)
	private String title;

	@Column(length = 256, nullable = true)
	private String label;

	@Column(length = 10, nullable = true)
	private String lang;

	/**
	 * Currently a LOB of the itemBody XML. The reality is, this should be a
	 * BodyElement class.
	 * http://www.imsglobal.org/question/qtiv2p1/imsqti_infov2p1
	 * .html#element10110
	 */
	@Lob
	private String itemBody;

	@Column
	private boolean adaptive;

	@Column
	private boolean timeDependent;

	// more advanced fields follow. we currently read these out of the XML as
	// this all JQTI supports, and to implement the whole domain of QTI objects
	// would take forever and a day.
	//@formatter:off
//	private String toolName;
//	private String toolVersion;
//	private List<QtiResponseDeclaration> responseDeclaration;
//	private List<QtiOutcomeDeclaration> outcomeDeclaration;
//	private List<QtiTemplateDeclaration> templateDeclaration;
//	private QtiTemplateProcessing templateProcessing;
//	private List<QtiStylesheet> stylesheets;
//	private QtiItemBody itemBody;
//	private QtiResponseProcessing responseProcessing;
//	private List<QtiModalFeedback> modalFeedback;
	//@formatter:on

	@Override
	public void setId(long id)
	{
		this.id = id;
	}

	@Override
	public long getId()
	{
		return id;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public Institution getInstitution()
	{
		return institution;
	}

	public void setInstitution(Institution institution)
	{
		this.institution = institution;
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public void setIdentifier(String identifier)
	{
		this.identifier = identifier;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public String getLang()
	{
		return lang;
	}

	public void setLang(String lang)
	{
		this.lang = lang;
	}

	public String getItemBody()
	{
		return itemBody;
	}

	public void setItemBody(String itemBody)
	{
		this.itemBody = itemBody;
	}

	public boolean isAdaptive()
	{
		return adaptive;
	}

	public void setAdaptive(boolean adaptive)
	{
		this.adaptive = adaptive;
	}

	public boolean isTimeDependent()
	{
		return timeDependent;
	}

	public void setTimeDependent(boolean timeDependent)
	{
		this.timeDependent = timeDependent;
	}
}
