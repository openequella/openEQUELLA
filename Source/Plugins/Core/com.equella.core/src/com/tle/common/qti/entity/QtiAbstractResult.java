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
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.IndexColumn;

import com.google.common.collect.Lists;
import com.tle.beans.IdCloneable;
import com.tle.common.qti.entity.enums.QtiSessionStatus;

/**
 * @author Aaron
 */
@Entity
@AccessType("field")
@Inheritance(strategy = InheritanceType.JOINED)
public class QtiAbstractResult implements Serializable, IdCloneable
{

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Index(name = "qtiResultDateIdx")
	@Column(nullable = false)
	private Date datestamp;

	// This is an itemResult field only. In the case of the assessmentResult it
	// is the _least_ complete value of all itemResults. This case is NOT in the
	// QTI spec.
	@Column(nullable = false)
	private int sessionStatus;

	@IndexColumn(name = "varindex", nullable = false)
	@JoinColumn(name = "result_id", nullable = false)
	@Fetch(value = FetchMode.JOIN)
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private List<QtiItemVariable> itemVariables = Lists.newArrayList();

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

	public Date getDatestamp()
	{
		return datestamp;
	}

	public void setDatestamp(Date datestamp)
	{
		this.datestamp = datestamp;
	}

	public QtiSessionStatus getSessionStatus()
	{
		return QtiSessionStatus.values()[sessionStatus];
	}

	public void setSessionStatus(QtiSessionStatus sessionStatus)
	{
		this.sessionStatus = sessionStatus.ordinal();
	}

	public List<QtiItemVariable> getItemVariables()
	{
		return itemVariables;
	}

	public void setItemVariables(List<QtiItemVariable> itemVariables)
	{
		this.itemVariables = itemVariables;
	}
}
