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

package com.dytech.edge.wizard.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.dytech.common.text.NumberStringComparator;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.tle.core.xstream.mapping.AbstractMapping;

public class DRMPage extends WizardPage
{
	private static final long serialVersionUID = 1;
	public static final String TYPE = "drm"; //$NON-NLS-1$

	private Set<String> usages;

	private String uuid;
	private boolean allowSummary;
	private boolean allowPreview;
	private boolean ownerMustAccept;
	private boolean hideLicencesFromOwner;
	private boolean showLicenceInComposition;
	private boolean showLicenceCount;

	private boolean attributionIsEnforced;

	// Requirements
	private String remark;
	private boolean attribution;

	private Container container;
	private Contributor contributor;

	private String requireAcceptanceFrom;

	public DRMPage()
	{
		attributionIsEnforced = true;
		showLicenceInComposition = true;
	}

	public static class Network implements Serializable
	{
		private static final long serialVersionUID = 1;

		private String name;
		private String min;
		private String max;

		public String getMax()
		{
			return max;
		}

		public void setMax(String max)
		{
			this.max = max;
		}

		public String getMin()
		{
			return min;
		}

		public void setMin(String min)
		{
			this.min = min;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}
	}

	public abstract static class UsersAndGroups implements Serializable
	{
		private static final long serialVersionUID = 1;

		private List<String> users;
		private List<String> groups;

		protected abstract String getPath();

		public List<String> getGroups()
		{
			if( groups == null )
			{
				groups = new ArrayList<String>();
			}
			return groups;
		}

		public void setGroups(List<String> groups)
		{
			this.groups = groups;
		}

		public List<String> getUsers()
		{
			if( users == null )
			{
				users = new ArrayList<String>();
			}
			return users;
		}

		public void setUsers(List<String> users)
		{
			this.users = users;
		}

		public boolean hasRecipients()
		{
			return !getUsers().isEmpty() || !getGroups().isEmpty();
		}
	}

	public static class Container extends UsersAndGroups
	{
		private static final long serialVersionUID = 1;

		private final NetworkSet networks;
		private boolean purpose;
		private int count;

		private Date acceptStart;
		private Date acceptEnd;

		public Container()
		{
			networks = new NetworkSet();
		}

		@Override
		protected String getPath()
		{
			return ""; //$NON-NLS-1$
		}

		public Date getAcceptEnd()
		{
			return acceptEnd;
		}

		public void setAcceptEnd(Date acceptEnd)
		{
			this.acceptEnd = acceptEnd;
		}

		public Date getAcceptStart()
		{
			return acceptStart;
		}

		public void setAcceptStart(Date acceptStart)
		{
			this.acceptStart = acceptStart;
		}

		public int getCount()
		{
			return count;
		}

		public void setCount(int count)
		{
			this.count = count;
		}

		public Set<Network> getNetworks()
		{
			return networks;
		}

		public boolean isPurpose()
		{
			return purpose;
		}

		public void setPurpose(boolean purpose)
		{
			this.purpose = purpose;
		}
	}

	public static class NetworkSet extends TreeSet<Network>
	{
		private static final long serialVersionUID = 1;

		public NetworkSet()
		{
			super(new NumberStringComparator<Network>()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public String convertToString(Network t)
				{
					return t.getName();
				}
			});
		}
	}

	public static class Contributor extends UsersAndGroups
	{
		private static final long serialVersionUID = 1;

		private final NetworkSet networks;
		private String count;
		private boolean sector;
		private boolean attribution;
		private boolean datetime;
		private boolean terms;

		public Contributor()
		{
			networks = new NetworkSet();
		}

		@Override
		protected String getPath()
		{
			return "usersAndGroups"; //$NON-NLS-1$
		}

		public boolean isDatetime()
		{
			return datetime;
		}

		public void setDatetime(boolean datetime)
		{
			this.datetime = datetime;
		}

		public Set<Network> getNetworks()
		{
			return networks;
		}

		public boolean isSector()
		{
			return sector;
		}

		public void setSector(boolean sector)
		{
			this.sector = sector;
		}

		public boolean isTerms()
		{
			return terms;
		}

		public void setTerms(boolean terms)
		{
			this.terms = terms;
		}

		public boolean isAttribution()
		{
			return attribution;
		}

		public void setAttribution(boolean attribution)
		{
			this.attribution = attribution;
		}

		public boolean isCount()
		{
			return count != null;
		}

		public int getCount()
		{
			int c = 0;
			if( isCount() && count.length() > 0 )
			{
				c = Integer.parseInt(count);
			}
			return c;
		}

		public void setIsCount(boolean c)
		{
			if( c )
			{
				count = ""; //$NON-NLS-1$
			}
			else
			{
				c = false;
			}
		}

		public void setCount(int count)
		{
			this.count = Integer.toString(count);
		}
	}

	public static class ExistsMapping extends AbstractMapping
	{
		public ExistsMapping(String name, String node)
		{
			super(name, node);
		}

		@Override
		public void marshal(HierarchicalStreamWriter writer, MarshallingContext context, Object object)
		{
			// Do nothing
		}

		@Override
		protected Object getUnmarshalledValue(Object object, HierarchicalStreamReader reader,
			UnmarshallingContext context)
		{
			return Boolean.TRUE;
		}

		@Override
		public boolean hasValue(Object object)
		{
			Object o = getField(object);
			return o != null && ((Boolean) o).booleanValue();
		}

	}

	public Set<String> getUsages()
	{
		if( usages == null )
		{
			usages = new HashSet<String>();
		}
		return usages;
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	public boolean isAllowPreview()
	{
		return allowPreview;
	}

	public void setAllowPreview(boolean allowPreview)
	{
		this.allowPreview = allowPreview;
	}

	public boolean isAllowSummary()
	{
		return allowSummary;
	}

	public void setAllowSummary(boolean allowSummary)
	{
		this.allowSummary = allowSummary;
	}

	public boolean isAttribution()
	{
		return attribution;
	}

	public void setAttribution(boolean attribution)
	{
		this.attribution = attribution;
	}

	public boolean isAttributionIsEnforced()
	{
		return attributionIsEnforced;
	}

	public void setAttributionIsEnforced(boolean attributionIsEnforced)
	{
		this.attributionIsEnforced = attributionIsEnforced;
	}

	public Container getContainer()
	{
		if( container == null )
		{
			container = new Container();
		}
		return container;
	}

	public void setContainer(Container container)
	{
		this.container = container;
	}

	public Contributor getContributor()
	{
		if( contributor == null )
		{
			contributor = new Contributor();
		}
		return contributor;
	}

	public void setContributor(Contributor contributor)
	{
		this.contributor = contributor;
	}

	public boolean isHideLicencesFromOwner()
	{
		return hideLicencesFromOwner;
	}

	public void setHideLicencesFromOwner(boolean hideLicencesFromOwner)
	{
		this.hideLicencesFromOwner = hideLicencesFromOwner;
	}

	public boolean isOwnerMustAccept()
	{
		return ownerMustAccept;
	}

	public void setOwnerMustAccept(boolean ownerMustAccept)
	{
		this.ownerMustAccept = ownerMustAccept;
	}

	public String getRemark()
	{
		return remark;
	}

	public void setRemark(String remark)
	{
		this.remark = remark;
	}

	public boolean isShowLicenceCount()
	{
		return showLicenceCount;
	}

	public void setShowLicenceCount(boolean showLicenceCount)
	{
		this.showLicenceCount = showLicenceCount;
	}

	public boolean isShowLicenceInComposition()
	{
		return showLicenceInComposition;
	}

	public void setShowLicenceInComposition(boolean showLicenceInComposition)
	{
		this.showLicenceInComposition = showLicenceInComposition;
	}

	public void setUsages(Set<String> usages)
	{
		this.usages = usages;
	}

	public boolean hasRequirement()
	{
		return attribution && remark != null;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public String getRequireAcceptanceFrom()
	{
		return requireAcceptanceFrom;
	}

	public void setRequireAcceptanceFrom(String requireAcceptanceFrom)
	{
		this.requireAcceptanceFrom = requireAcceptanceFrom;
	}
}
