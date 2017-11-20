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

package com.tle.web.selection;

import java.io.Serializable;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class SelectionHistory implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String uuid;
	private int version;
	private String selection;
	private String title;
	private long selectedDate;

	public SelectionHistory()
	{
		// for JSON
	}

	public SelectionHistory(SelectedResource resource)
	{
		uuid = resource.getUuid();
		version = resource.getVersion();
		char type = resource.getType();
		if( type == SelectedResource.TYPE_ATTACHMENT )
		{
			selection = "a" + resource.getAttachmentUuid(); //$NON-NLS-1$
		}
		else if( type == SelectedResource.TYPE_PATH )
		{
			selection = "p" + resource.getUrl(); //$NON-NLS-1$
		}
		else if( type == SelectedResource.TYPE_REMOTE )
		{
			selection = "r" + resource.getUrl(); //$NON-NLS-1$
		}
		selectedDate = resource.getSelectedDate();
		title = resource.getTitle();
	}

	@SuppressWarnings("nls")
	public SelectionHistory(ObjectNode obj)
	{
		uuid = obj.path("uuid").asText();
		version = obj.path("version").asInt();
		title = obj.path("title").asText();
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public int getVersion()
	{
		return version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	public String getSelection()
	{
		return selection;
	}

	public void setSelection(String selection)
	{
		this.selection = selection;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public long getSelectedDate()
	{
		return selectedDate;
	}

	public void setSelectedDate(long selectedDate)
	{
		this.selectedDate = selectedDate;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((selection == null) ? 0 : selection.hashCode());
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + version;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			return true;
		}
		if( obj == null )
		{
			return false;
		}
		if( getClass() != obj.getClass() )
		{
			return false;
		}
		SelectionHistory other = (SelectionHistory) obj;
		if( selection == null )
		{
			if( other.selection != null )
			{
				return false;
			}
		}
		else if( !selection.equals(other.selection) )
		{
			return false;
		}
		if( uuid == null )
		{
			if( other.uuid != null )
			{
				return false;
			}
		}
		else if( !uuid.equals(other.uuid) )
		{
			return false;
		}
		if( title == null )
		{
			if( other.title != null )
			{
				return false;
			}
		}
		else if( !title.equals(other.title) )
		{
			return false;
		}
		if( version != other.version )
		{
			return false;
		}
		return true;
	}

}
