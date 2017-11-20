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

package com.tle.common.activecache.settings;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.dytech.devlib.PropBagEx;
import com.tle.common.settings.ConfigurationProperties;
import com.tle.common.settings.annotation.Property;
import com.tle.common.settings.annotation.PropertyBag;
import com.tle.core.xstream.TLEXStream;
import com.tle.core.xstream.XMLData;
import com.tle.core.xstream.XMLDataMappings;
import com.tle.core.xstream.mapping.ListMapping;
import com.tle.core.xstream.mapping.NodeMapping;

/**
 * @author Nicholas Read
 */
public class CacheSettings implements ConfigurationProperties
{
	private static final long serialVersionUID = 1;
	private static TLEXStream xstream = TLEXStream.instance();

	@Property(key = "cache.enabled")
	private boolean enabled;
	@PropertyBag(key = "cache.groups")
	private PropBagEx groups;

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public Node getGroups()
	{
		if( groups == null )
		{
			return null;
		}
		return (Node) xstream.fromXML(groups, Node.class);
	}

	public void setGroups(Node groups)
	{
		this.groups = xstream.toPropBag(groups, "groups"); //$NON-NLS-1$
	}

	public static class Node implements XMLData
	{
		private static final long serialVersionUID = 1L;

		private static final XMLDataMappings smappings;

		static
		{
			smappings = new XMLDataMappings();
			smappings.addNodeMapping(new NodeMapping("name", "@name"));
			smappings.addNodeMapping(new NodeMapping("id", "@id"));
			smappings.addNodeMapping(new NodeMapping("uuid", "@uuid"));
			smappings.addNodeMapping(new ListMapping("includes", "include", ArrayList.class, Query.class));
			smappings.addNodeMapping(new ListMapping("excludes", "exclude", ArrayList.class, Query.class));
			smappings.addNodeMapping(new ListMapping("nodes", "user", ArrayList.class, Node.class)
			{
				@Override
				public boolean hasValue(Object object)
				{
					return false;
				}
			});
			smappings.addNodeMapping(new ListMapping("nodes", "group", ArrayList.class, Node.class));
		}

		private List<Query> includes;
		private List<Query> excludes;
		private List<Node> nodes;

		private String id;
		private String name;
		private String uuid;

		/**
		 * For serialisation only!
		 */
		public Node()
		{
			//
		}

		public Node(String string, boolean group)
		{
			if( group )
			{
				name = string;
			}
			else
			{
				id = string;
			}
			setUuid(UUID.randomUUID().toString());
		}

		@Override
		public XMLDataMappings getMappings()
		{
			return smappings;
		}

		public boolean isGroup()
		{
			return name != null;
		}

		public boolean isUser()
		{
			return id != null;
		}

		public List<Query> getExcludes()
		{
			if( excludes == null )
			{
				excludes = new ArrayList<Query>();
			}
			return excludes;
		}

		public void setExcludes(List<Query> excludes)
		{
			this.excludes = excludes;
		}

		public List<Query> getIncludes()
		{
			if( includes == null )
			{
				includes = new ArrayList<Query>();
			}
			return includes;
		}

		public void setIncludes(List<Query> includes)
		{
			this.includes = includes;
		}

		public List<Node> getNodes()
		{
			if( nodes == null )
			{
				nodes = new ArrayList<Node>();
			}
			return nodes;
		}

		public void setNodes(List<Node> nodes)
		{
			this.nodes = nodes;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getId()
		{
			return id;
		}

		public void setId(String id)
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
	}

	public static class Query implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private long itemdef;
		private String uuid;
		private String script;

		public Query()
		{
			script = "";
		}

		public Query(long id, String script)
		{
			itemdef = id;
			this.script = script;
		}

		public long getItemdef()
		{
			return itemdef;
		}

		public void setItemdef(long itemdef)
		{
			this.itemdef = itemdef;
		}

		public String getScript()
		{
			return script;
		}

		public void setScript(String script)
		{
			this.script = script;
		}

		public void setUuid(String uuid)
		{
			this.uuid = uuid;
		}

		public String getUuid()
		{
			return uuid;
		}
	}
}
