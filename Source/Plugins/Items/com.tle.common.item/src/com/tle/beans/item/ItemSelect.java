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

package com.tle.beans.item;

import com.tle.beans.entity.Schema;

import java.util.ArrayList;
import java.util.List;

public class ItemSelect
{
	private boolean attachments;
	private boolean collaborators;
	private boolean notifications;
	private boolean itemdef;
	private boolean badurls;
	private boolean drm;
	private boolean history;
	private boolean moderation;
	private boolean moderationStatuses;
	private boolean acceptances;
	private boolean itemXml;
	private boolean treeNodes;
	private boolean name;
	private boolean description;

	// Itemdef stuff
	private boolean blob;
	private boolean schema;
	private boolean workflow;

	public ItemSelect()
	{
		super();
	}

	public boolean isAttachments()
	{
		return attachments;
	}

	public void setAttachments(boolean attachments)
	{
		this.attachments = attachments;
	}

	public boolean isBadurls()
	{
		return badurls;
	}

	public void setBadurls(boolean badurls)
	{
		this.badurls = badurls;
	}

	public boolean isCollaborators()
	{
		return collaborators;
	}

	public void setCollaborators(boolean collaborators)
	{
		this.collaborators = collaborators;
	}

	public boolean isDrm()
	{
		return drm;
	}

	public void setDrm(boolean drm)
	{
		this.drm = drm;
	}

	public boolean isItemdef()
	{
		return itemdef;
	}

	public void setItemdef(boolean itemdef)
	{
		this.itemdef = itemdef;
	}

	public boolean isNotifications()
	{
		return notifications;
	}

	public void setNotifications(boolean notifications)
	{
		this.notifications = notifications;
	}

	public boolean isHistory()
	{
		return history;
	}

	public void setHistory(boolean history)
	{
		this.history = history;
	}

	public boolean isModeration()
	{
		return moderation;
	}

	public void setModeration(boolean moderation)
	{
		this.moderation = moderation;
	}

	public boolean isAcceptances()
	{
		return acceptances;
	}

	public void setAcceptances(boolean acceptances)
	{
		this.acceptances = acceptances;
	}

	public boolean isSchema()
	{
		return schema;
	}

	public void setSchema(boolean schema)
	{
		this.schema = schema;
	}

	public boolean isWorkflow()
	{
		return workflow;
	}

	public void setWorkflow(boolean workflow)
	{
		this.workflow = workflow;
	}

	public boolean isBlob()
	{
		return blob;
	}

	public void setBlob(boolean blob)
	{
		this.blob = blob;
	}

	public boolean isItemXml()
	{
		return itemXml;
	}

	public void setItemXml(boolean itemXml)
	{
		this.itemXml = itemXml;
	}

	public boolean isTreeNodes()
	{
		return treeNodes;
	}

	public void setTreeNodes(boolean treeNodes)
	{
		this.treeNodes = treeNodes;
	}

	public boolean isName()
	{
		return name;
	}

	public void setName(boolean name)
	{
		this.name = name;
	}

	public boolean isDescription()
	{
		return description;
	}

	public void setDescription(boolean description)
	{
		this.description = description;
	}

	@SuppressWarnings("nls")
	public List<String> listOneToOnes()
	{
		List<String> b = new ArrayList<String>();
		if( isDrm() )
		{
			b.add("drmSettings");
		}
		if( isItemdef() )
		{
			b.add("itemDefinition");
		}
		if( isModeration() )
		{
			b.add("moderation");
		}
		if( isWorkflow() )
		{
			b.add("itemDefinition.workflow");
		}
		if( isSchema() )
		{
			b.add("itemDefinition.schema");
		}
		if( isBlob() )
		{
			b.add("itemDefinition.slow");
		}
		if( isModerationStatuses() )
		{
			b.add("moderation.statuses");
		}
		if( isCollaborators() )
		{
			b.add("collaborators");
		}
		if( isAttachments() )
		{
			b.add("attachments");
		}
		if( isNotifications() )
		{
			b.add("notifications");
		}
		if( isBadurls() )
		{
			b.add("badUrls");
		}
		if( isHistory() )
		{
			b.add("history");
		}
		if( isAcceptances() )
		{
			b.add("acceptances");
		}
		if( isItemXml() )
		{
			b.add("itemXml");
		}
		if( isTreeNodes() )
		{
			b.add("treeNodes");
		}
		if( isName() )
		{
			b.add("name");
		}
		if( isDescription() )
		{
			b.add("description");
		}
		return b;
	}

	public void initialise(Item item)
	{
		if( item != null )
		{
			if( isAttachments() )
			{
				initialiseCollection(item.getAttachmentsUnmodifiable());
			}
			if( isBadurls() )
			{
				initialiseCollection(item.getReferencedUrls());
			}
			if( isHistory() )
			{
				initialiseCollection(item.getHistory());
			}
			if( isAcceptances() )
			{
				initialiseCollection(item.getAcceptances());
			}
			if( isCollaborators() )
			{
				initialiseCollection(item.getCollaborators());
			}
			if( isModerationStatuses() && item.getModeration() != null )
			{
				initialiseCollection(item.getModeration().getStatuses());
			}
			if( isTreeNodes() )
			{
				initialiseCollection(item.getTreeNodes());
			}
		}
	}

	private void initialiseCollection(Iterable<?> col)
	{
		if( col != null )
		{
			// This initialises the stored snapshot
			col.iterator();
		}
	}

	public boolean isModerationStatuses()
	{
		return moderationStatuses;
	}

	public void setModerationStatuses(boolean moderationStatuses)
	{
		this.moderationStatuses = moderationStatuses;
	}
}
