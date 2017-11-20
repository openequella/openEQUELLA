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

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;

import com.google.common.collect.Lists;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.tle.annotation.NonNull;
import com.tle.beans.IdCloneable;
import com.tle.common.qti.entity.enums.QtiBaseType;
import com.tle.common.qti.entity.enums.QtiCardinality;

/**
 * @author Aaron
 */
@Entity
@AccessType("field")
// Need to drop this constraint :( SQL Server doesn't care about the case of
// identifier EQ-596
// @Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"result_id",
// "identifier"})})
public class QtiItemVariable implements Serializable, IdCloneable
{
	/**
	 * Do NOT re-order this enum
	 */
	public enum VariableType
	{
		RESPONSE, OUTCOME, TEMPLATE
	}

	private static final long serialVersionUID = 1;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	/**
	 * Back-ref
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@Index(name = "qtiItemvarResultIdx")
	@JoinColumn(insertable = false, updatable = false, nullable = false, name = "result_id")
	@XStreamOmitField
	private QtiAbstractResult result;

	@Index(name = "qtiItemvarVartypeIdx")
	@Column(nullable = false)
	private int variableType;

	@Index(name = "qtiItemvarIdentIdx")
	@Column(length = 255, nullable = false)
	private String identifier;

	@Column(length = 50, nullable = false)
	private String cardinality = QtiCardinality.SINGLE.toString();

	@Column(length = 50, nullable = true)
	private String baseType;

	// Fucking comment formatting
	/**
	 * <p>
	 * We are not supporting the record type, so each value can be a simple
	 * string instead of a "value" type. ( http
	 * ://www.imsglobal.org/question/qtiv2p1/imsqti_infov2p1.html#element10042 )
	 * </p>
	 * <p>
	 * Note: OutcomeVariable defines a list of values and ResponseVariable
	 * defines a single candidateResponse which itself contains only a list of
	 * values, hence the candidateResponse is rolled into this value list and
	 * put into the base class for OutcomeVariable and ResponseVariable to use.
	 * </p>
	 */
	// TODO: it may be more efficient to turn this into a fully fledged Value
	// type, that way we can change the values of the existing
	// rows without hibernate having to do a delete all values + insert all
	// values whenever they get changed.
	@ElementCollection(fetch = FetchType.EAGER)
	@Fetch(value = FetchMode.JOIN)
	@CollectionTable(name = "qti_item_variable_value", joinColumns = @JoinColumn(name = "qti_item_variable_id"))
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	@Column(name = "value")
	private List<String> values = Lists.newArrayList();

	// --- OutcomeVariable fields ---

	// private String view;

	// private String interpretation;

	// private String longInterpretation

	/**
	 * Float value of -1.0 to 1.0
	 */
	@Column(nullable = true)
	private Double normalMaximum;

	/**
	 * Float value of -1.0 to 1.0
	 */
	@Column(nullable = true)
	private Double normalMinimum;

	// private Double masteryValue;

	// --- Response Variable field ---
	/**
	 * Space delimited field
	 */
	@Column(length = 1024, nullable = true)
	private String choiceSequence;

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

	public VariableType getVariableType()
	{
		return VariableType.values()[variableType];
	}

	public void setVariableType(VariableType variableType)
	{
		this.variableType = variableType.ordinal();
	}

	public QtiAbstractResult getResult()
	{
		return result;
	}

	public void setResult(QtiAbstractResult result)
	{
		this.result = result;
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public void setIdentifier(String identifier)
	{
		this.identifier = identifier;
	}

	@NonNull
	public QtiCardinality getCardinality()
	{
		return QtiCardinality.valueOf(cardinality);
	}

	public void setCardinality(@NonNull QtiCardinality cardinality)
	{
		this.cardinality = cardinality.toString();
	}

	public QtiBaseType getBaseType()
	{
		return QtiBaseType.fromString(baseType);
	}

	public void setBaseType(QtiBaseType baseType)
	{
		this.baseType = (baseType == null ? null : baseType.toString());
	}

	public List<String> getValues()
	{
		return values;
	}

	public void setValues(List<String> values)
	{
		this.values = values;
	}

	public Double getNormalMaximum()
	{
		return normalMaximum;
	}

	public void setNormalMaximum(Double normalMaximum)
	{
		this.normalMaximum = normalMaximum;
	}

	public Double getNormalMinimum()
	{
		return normalMinimum;
	}

	public void setNormalMinimum(Double normalMinimum)
	{
		this.normalMinimum = normalMinimum;
	}

	public String getChoiceSequence()
	{
		return choiceSequence;
	}

	public void setChoiceSequence(String choiceSequence)
	{
		this.choiceSequence = choiceSequence;
	}
}
