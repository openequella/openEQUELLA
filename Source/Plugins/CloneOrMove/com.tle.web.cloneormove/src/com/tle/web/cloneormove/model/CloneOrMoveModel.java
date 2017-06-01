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

package com.tle.web.cloneormove.model;

import com.tle.beans.entity.Schema;
import com.tle.web.sections.annotations.Bookmarked;

/**
 * @author aholland
 */
public class CloneOrMoveModel
{
	@Bookmarked
	private boolean hideClone;
	@Bookmarked
	private boolean hideCloneNoAttachments;
	@Bookmarked
	private boolean hideMove;

	private boolean move;

	private boolean showCloneOptions;
	private boolean allowCollectionChange;
	private String submitLabel;

	private Schema sourceSchema; // purely informational
	private Schema destSchema; // purely informational

	public boolean isShowCloneOptions()
	{
		return showCloneOptions;
	}

	public void setShowCloneOptions(boolean showCloneOptions)
	{
		this.showCloneOptions = showCloneOptions;
	}

	public boolean isAllowCollectionChange()
	{
		return allowCollectionChange;
	}

	public void setAllowCollectionChange(boolean allowCollectionChange)
	{
		this.allowCollectionChange = allowCollectionChange;
	}

	public boolean isSchemaChanged()
	{
		return sourceSchema != null && destSchema != null && !sourceSchema.getUuid().equals(destSchema.getUuid());
	}

	public Schema getSourceSchema()
	{
		return sourceSchema;
	}

	public void setSourceSchema(Schema sourceSchema)
	{
		this.sourceSchema = sourceSchema;
	}

	public Schema getDestSchema()
	{
		return destSchema;
	}

	public void setDestSchema(Schema destSchema)
	{
		this.destSchema = destSchema;
	}

	public boolean isHideClone()
	{
		return hideClone;
	}

	public void setHideClone(boolean hideClone)
	{
		this.hideClone = hideClone;
	}

	public boolean isMove()
	{
		return move;
	}

	public void setMove(boolean move)
	{
		this.move = move;
	}

	public boolean isHideCloneNoAttachments()
	{
		return hideCloneNoAttachments;
	}

	public void setHideCloneNoAttachments(boolean hideCloneNoAttachments)
	{
		this.hideCloneNoAttachments = hideCloneNoAttachments;
	}

	public boolean isHideMove()
	{
		return hideMove;
	}

	public void setHideMove(boolean hideMove)
	{
		this.hideMove = hideMove;
	}

	public String getSubmitLabel()
	{
		return submitLabel;
	}

	public void setSubmitLabel(String submitLabel)
	{
		this.submitLabel = submitLabel;
	}
}
