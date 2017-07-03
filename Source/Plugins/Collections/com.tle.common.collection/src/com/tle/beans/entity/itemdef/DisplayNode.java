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

package com.tle.beans.entity.itemdef;

import java.io.Serializable;
import java.util.Objects;

import com.tle.beans.entity.LanguageBundle;
import com.tle.common.Check;

public class DisplayNode implements Serializable
{
	private static final long serialVersionUID = 1;
	private static final String DEFAULT_SPLITTER = ", "; //$NON-NLS-1$

	private static final String SINGLE_MODE = "single"; //$NON-NLS-1$
	private static final String DOUBLE_MODE = "double"; //$NON-NLS-1$

	private static final String TEXT_TYPE = "text"; //$NON-NLS-1$
	private static final String DATE_TYPE = "date"; //$NON-NLS-1$
	private static final String HTML_TYPE = "html"; //$NON-NLS-1$
	private static final String URL_TYPE = "url"; //$NON-NLS-1$

	private String mode;
	private String node;
	private String splitter;
	private String type;
	private LanguageBundle title;
	private Integer truncateLength;

	public DisplayNode()
	{
		splitter = DEFAULT_SPLITTER;
	}

	public LanguageBundle getTitle()
	{
		return title;
	}

	public void setTitle(LanguageBundle title)
	{
		this.title = title;
	}

	public String getNode()
	{
		return node;
	}

	public void setNode(String node)
	{
		this.node = node;
	}

	public boolean hasMode()
	{
		return !Check.isEmpty(mode);
	}

	public boolean isSingleMode()
	{
		return mode == null || SINGLE_MODE.equals(mode);
	}

	public boolean isHTMLType()
	{
		return HTML_TYPE.equals(type);
	}

	public boolean isURLType()
	{
		return URL_TYPE.equals(type);
	}

	public boolean isDoubleMode()
	{
		return DOUBLE_MODE.equals(mode);
	}

	public boolean isTextType()
	{
		return type.equals(TEXT_TYPE);
	}

	public boolean isDateType()
	{
		return type.equals(DATE_TYPE);
	}

	public String getType()
	{
		return type;
	}

	public String getMode()
	{
		return mode;
	}

	public String getSplitter()
	{
		return splitter;
	}

	public void setSplitter(String splitter)
	{
		this.splitter = splitter;
	}

	public void setMode(String mode)
	{
		this.mode = mode;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public Integer getTruncateLength()
	{
		return truncateLength;
	}

	public void setTruncateLength(Integer truncateLength)
	{
		this.truncateLength = truncateLength;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mode == null) ? 0 : mode.hashCode());
		result = prime * result + ((node == null) ? 0 : node.hashCode());
		result = prime * result + ((splitter == null) ? 0 : splitter.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((truncateLength == null) ? 0 : truncateLength.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			return true;
		}

		if( obj == null || getClass() != obj.getClass() )
		{
			return false;
		}

		final DisplayNode other = (DisplayNode) obj;
		if( !Objects.equals(mode, other.mode) )
		{
			return false;
		}

		if( !Objects.equals(node, other.node) )
		{
			return false;
		}

		if( !Objects.equals(splitter, other.splitter) )
		{
			return false;
		}

		if( !Objects.equals(title, other.title) )
		{
			return false;
		}

		if( !Objects.equals(type, other.type) )
		{
			return false;
		}

		if( !Objects.equals(truncateLength, other.truncateLength) )
		{
			return false;
		}

		return true;
	}
}
