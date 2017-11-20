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

package com.tle.core.item.serializer;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.tle.beans.item.Item;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.common.institution.CurrentInstitution;

@SuppressWarnings("nls")
public class ItemSerializerState
{
	private static final String ID_ALIAS = "stateitemid";

	public static final String OWNER_ALIAS = "owner";
	public static final String COLLECTIONUUID_ALIAS = "coluuid";
	public static final String COLLECTIONID_ALIAS = "colid";
	public static final String STATUS_ALIAS = "status";
	public static final String COLLAB_ALIAS = "collab";
	public static final String SECURITY_ALIAS = "metatargets";

	private final Set<String> categories;
	private final boolean export;

	private final ProjectionList itemProjection;
	private final DetachedCriteria itemQuery;
	private final Set<String> privileges = Sets.newHashSet();

	private Map<Long, Map<String, Object>> itemQueryResults;
	private Set<Long> bundlesToResolve;
	private Map<Long, String> resolvedBundles;
	private boolean ownerQueryAdded;
	private boolean collectionAdded;
	private boolean statusAdded;
	private boolean allCategories;
	private boolean ignorePrivileges;

	private SetMultimap<String, Long> privMultimap;

	private Integer maxResults;

	private Integer firstResult;

	public ItemSerializerState(Set<String> categories, boolean export)
	{
		this.categories = categories;
		this.export = export;
		if( categories.contains(ItemSerializerService.CATEGORY_ALL) )
		{
			allCategories = true;
		}
		itemProjection = Projections.projectionList().add(Projections.id().as(ID_ALIAS));

		itemQuery = DetachedCriteria.forClass(Item.class, "i").setProjection(itemProjection)
			.setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
			.add(Restrictions.eq("institution", CurrentInstitution.get()));
	}

	public Collection<Long> getItemKeys()
	{
		return itemQueryResults.keySet();
	}

	public boolean hasCategory(String category)
	{
		return allCategories || categories.contains(category);
	}

	public void addPrivilege(String priv)
	{
		if( privileges.isEmpty() )
		{
			addOwnerQuery();
			addCollectionQuery();
			addStatusQuery();
			itemProjection.add(Projections.property("metadataSecurityTargets"), SECURITY_ALIAS);
		}
		privileges.add(priv);
	}

	public void addStatusQuery()
	{
		if( !statusAdded )
		{
			itemProjection.add(Projections.property("status"), STATUS_ALIAS);
			statusAdded = true;
		}

	}

	public void addCollectionQuery()
	{
		if( !collectionAdded )
		{
			itemQuery.createAlias("itemDefinition", "col");
			itemProjection.add(Projections.property("col.uuid"), COLLECTIONUUID_ALIAS);
			itemProjection.add(Projections.property("col.id"), COLLECTIONID_ALIAS);
			collectionAdded = true;
		}
	}

	public void addOwnerQuery()
	{
		if( !ownerQueryAdded )
		{
			itemProjection.add(Projections.property("owner"), OWNER_ALIAS);
			ownerQueryAdded = true;
		}
	}

	public void processItemQueryResults(List<Map<String, Object>> results)
	{
		itemQueryResults = new LinkedHashMap<Long, Map<String, Object>>(results.size());
		for( Map<String, Object> result : results )
		{
			Long key = (Long) result.remove(ID_ALIAS);
			itemQueryResults.put(key, result);
		}
	}

	public Map<String, Object> getItemData(Long itemId)
	{
		return itemQueryResults.get(itemId);
	}

	@SuppressWarnings("unchecked")
	public <T> T getData(Long itemkey, String alias) throws NotFoundException
	{
		if( itemQueryResults == null )
		{
			throw new NullPointerException("itemQueryResults");
		}
		Map<String, Object> map = itemQueryResults.get(itemkey);
		if( map == null )
		{
			throw new NotFoundException("No result for itemkey " + itemkey);
		}
		return (T) map.get(alias);
	}

	public void setData(Long itemKey, String alias, Object data)
	{
		itemQueryResults.get(itemKey).put(alias, data);
	}

	public ProjectionList getItemProjection()
	{
		return itemProjection;
	}

	public DetachedCriteria getItemQuery()
	{
		return itemQuery;
	}

	public void addBundleToResolve(Long bundleId)
	{
		if( bundlesToResolve == null )
		{
			bundlesToResolve = Sets.newHashSet();
		}
		if( bundleId != null )
		{
			bundlesToResolve.add(bundleId);
		}
	}

	public void addBundleToResolve(Long itemKey, String alias)
	{
		addBundleToResolve((Long) getData(itemKey, alias));
	}

	public Set<Long> getBundlesToResolve()
	{
		return bundlesToResolve;
	}

	public String getResolvedBundle(Long bundleId)
	{
		if( resolvedBundles == null )
		{
			return null;
		}
		return resolvedBundles.get(bundleId);
	}

	public String getResolvedBundle(Long itemKey, String alias)
	{
		return getResolvedBundle((Long) getData(itemKey, alias));
	}

	public void setResolvedBundles(Map<Long, String> resolvedBundles)
	{
		this.resolvedBundles = resolvedBundles;
	}

	public Set<String> getPrivileges()
	{
		return privileges;
	}

	public void setPrivilegeMap(SetMultimap<String, Long> privMultimap)
	{
		this.privMultimap = privMultimap;
	}

	public boolean hasPrivilege(long itemId, String privilege)
	{
		// jolse said it was ok
		return ignorePrivileges || privMultimap.get(privilege).contains(itemId);
	}

	public Set<Long> getItemIdsWithPrivilege(String privilege)
	{
		return privMultimap.get(privilege);
	}

	public boolean isOwnerQueryAdded()
	{
		return ownerQueryAdded;
	}

	public Integer getMaxResults()
	{
		return maxResults;
	}

	public Integer getFirstResult()
	{
		return firstResult;
	}

	public void setMaxResults(Integer maxResults)
	{
		this.maxResults = maxResults;
	}

	public void setFirstResult(Integer firstResult)
	{
		this.firstResult = firstResult;
	}

	public boolean isIgnorePrivileges()
	{
		return ignorePrivileges;
	}

	public void setIgnorePrivileges(boolean ignorePrivileges)
	{
		this.ignorePrivileges = ignorePrivileges;
	}

	public boolean isExport()
	{
		return export;
	}
}
