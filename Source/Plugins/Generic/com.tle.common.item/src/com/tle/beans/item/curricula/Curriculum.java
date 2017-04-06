/*
 * Created on Aug 9, 2005
 */
package com.tle.beans.item.curricula;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.dytech.common.xml.XMLData;
import com.dytech.common.xml.XMLDataMappings;
import com.dytech.common.xml.mapping.ListMapping;
import com.dytech.common.xml.mapping.NodeMapping;

@SuppressWarnings("nls")
public class Curriculum implements XMLData
{
	private static final long serialVersionUID = 1L;

	private static XMLDataMappings mappings;
	private String uuid;
	private String path;
	private String name;
	private boolean updated;
	private String standard;
	private String description;
	private int version;
	private int minversion;
	private List<Ancestor> ancestors = new ArrayList<Ancestor>();

	public Curriculum()
	{
		super();
	}

	@Override
	public synchronized XMLDataMappings getMappings()
	{
		if( mappings == null )
		{
			mappings = new XMLDataMappings();
			mappings.addNodeMapping(new NodeMapping("name", "name"));
			mappings.addNodeMapping(new NodeMapping("uuid", "@uuid"));
			mappings.addNodeMapping(new NodeMapping("path", "@path"));
			mappings.addNodeMapping(new NodeMapping("updated", "@updated"));
			mappings.addNodeMapping(new NodeMapping("standard", "@standard"));
			mappings.addNodeMapping(new NodeMapping("description", "description"));
			mappings.addNodeMapping(new NodeMapping("version", "@version"));
			mappings.addNodeMapping(new NodeMapping("minversion", "@minversion"));
			// The intent presumably is to return the implementation class, so
			// we ignore the 'loose coupling' Sonar warning.
			mappings.addNodeMapping(new ListMapping("ancestors", "lineage/ancestor", ArrayList.class, Ancestor.class)); // NOSONAR
		}
		return mappings;
	}

	public List<Ancestor> getAncestors()
	{
		return ancestors;
	}

	public void setAncestors(List<Ancestor> ancestors)
	{
		this.ancestors = ancestors;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public int getMinversion()
	{
		return minversion;
	}

	public void setMinversion(int minversion)
	{
		this.minversion = minversion;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getStandard()
	{
		return standard;
	}

	public void setStandard(String standard)
	{
		this.standard = standard;
	}

	public boolean isUpdated()
	{
		return updated;
	}

	public void setUpdated(boolean updated)
	{
		this.updated = updated;
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

	public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public void setUniqueId(String uniqueid)
	{
		int ind = uniqueid.indexOf(':');
		if( ind < 0 )
		{
			uuid = uniqueid;
			standard = null;
		}
		else
		{
			uuid = uniqueid.substring(0, ind);
			standard = uniqueid.substring(ind + 1);
		}
	}

	public String getDisplayName()
	{
		StringBuilder sbuf = new StringBuilder();
		if( updated )
		{
			sbuf.append("(*) ");
		}
		for( Ancestor lineage : ancestors )
		{
			sbuf.append(lineage.getName());
			sbuf.append(" - ");
		}
		sbuf.append(getName());

		if( standard != null )
		{
			sbuf.append(" - " + standard);
		}
		return sbuf.toString();
	}

	public String getUniqueId()
	{
		if( standard == null )
		{
			return uuid;
		}
		return uuid + ':' + standard;
	}

	public static class Ancestor implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String uuid;
		private String name;
		private String code;

		public Ancestor()
		{
			super();
		}

		public Ancestor(String uuid, String name, String code)
		{
			this.uuid = uuid;
			this.name = name;
			this.code = code;
		}

		public String getCode()
		{
			return code;
		}

		public void setCode(String code)
		{
			this.code = code;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
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

	@Override
	public boolean equals(Object object)
	{
		boolean equals = super.equals(object);
		if( !equals && object != null )
		{
			Curriculum curriculum = (Curriculum) object;
			equals = getUniqueId().equals(curriculum.getUniqueId());

		}
		return equals;
	}

	@Override
	public int hashCode()
	{
		return getUniqueId().hashCode();
	}
}
