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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * See itemResult
 * http://www.imsglobal.org/question/qtiv2p1/imsqti_resultv2p1.html#element10818
 * 
 * @author Aaron
 */
@Entity
@AccessType("field")
public class QtiItemResult extends QtiAbstractResult
{
	private static final long serialVersionUID = 1;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@Index(name = "qtiItemresResultIdx")
	@JoinColumn(insertable = false, updatable = false, name = "assessment_result_id")
	@XStreamOmitField
	private QtiAssessmentResult assessmentResult;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@Index(name = "qtiItemresItemrefIdx")
	private QtiAssessmentItemRef itemRef;

	@Column(nullable = true)
	private Integer sequenceIndex;

	@Column(nullable = true)
	@Lob
	private String candidateComment;

	public QtiAssessmentResult getAssessmentResult()
	{
		return assessmentResult;
	}

	public void setAssessmentResult(QtiAssessmentResult assessmentResult)
	{
		this.assessmentResult = assessmentResult;
	}

	public QtiAssessmentItemRef getItemRef()
	{
		return itemRef;
	}

	public void setItemRef(QtiAssessmentItemRef itemRef)
	{
		this.itemRef = itemRef;
	}

	public Integer getSequenceIndex()
	{
		return sequenceIndex;
	}

	public void setSequenceIndex(Integer sequenceIndex)
	{
		this.sequenceIndex = sequenceIndex;
	}

	public String getCandidateComment()
	{
		return candidateComment;
	}

	public void setCandidateComment(String candidateComment)
	{
		this.candidateComment = candidateComment;
	}
}
