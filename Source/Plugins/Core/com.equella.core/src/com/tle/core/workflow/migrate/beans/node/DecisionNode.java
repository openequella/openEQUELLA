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

package com.tle.core.workflow.migrate.beans.node;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;

import org.hibernate.annotations.AccessType;

import com.tle.beans.entity.LanguageBundle;

@Entity(name = "WorkflowDecision")
@AccessType("field")
@DiscriminatorValue("d")
public class DecisionNode extends WorkflowNode
{
	private static final long serialVersionUID = 1;

	@Lob
	private String script;
	@Column(length = 40)
	private String collectionUuid;

	public DecisionNode(LanguageBundle name)
	{
		super(name);
		script = ""; //$NON-NLS-1$
	}

	public DecisionNode()
	{
		this(null);
	}

	@Override
	public char getType()
	{
		return 'd';
	}

	public String getScript()
	{
		return script;
	}

	public void setScript(String script)
	{
		this.script = script;
	}

	@Override
	public boolean canAddChildren()
	{
		return true;
	}

	@Override
	public boolean canHaveSiblingRejectPoints()
	{
		return true;
	}

	public String getCollectionUuid()
	{
		return collectionUuid;
	}

	public void setCollectionUuid(String collectionUuid)
	{
		this.collectionUuid = collectionUuid;
	}
}
