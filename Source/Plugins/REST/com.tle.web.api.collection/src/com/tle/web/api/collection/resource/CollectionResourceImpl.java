package com.tle.web.api.collection.resource;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.UriInfo;

import com.google.common.collect.Lists;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.Check;
import com.tle.common.interfaces.BaseEntityReference;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.SecurityConstants;
import com.tle.core.guice.Bind;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.core.services.entity.ItemDefinitionService;
import com.tle.web.api.baseentity.serializer.BaseEntitySerializer;
import com.tle.web.api.collection.CollectionBeanSerializer;
import com.tle.web.api.collection.interfaces.CollectionResource;
import com.tle.web.api.collection.interfaces.beans.AllCollectionsSecurityBean;
import com.tle.web.api.collection.interfaces.beans.CollectionBean;
import com.tle.web.api.entity.resource.AbstractBaseEntityResource;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.api.schema.interfaces.SchemaResource;

@SuppressWarnings("nls")
@Bind(EquellaCollectionResource.class)
@Singleton
public class CollectionResourceImpl
	extends
		AbstractBaseEntityResource<ItemDefinition, AllCollectionsSecurityBean, CollectionBean>
	implements
		EquellaCollectionResource
{
	@Inject
	private ItemDefinitionService collectionService;
	@Inject
	private CollectionBeanSerializer collectionSerializer;

	// Override the EPS definition of it. We add a new priv param
	@Override
	public SearchBean<CollectionBean> list(UriInfo uriInfo)
	{
		throw new Error();
	}

	@Override
	public SearchBean<CollectionBean> list(UriInfo uriInfo, String privilege)
	{
		final SearchBean<CollectionBean> result = new SearchBean<CollectionBean>();
		final List<CollectionBean> colbeans = Lists.newArrayList();
		final String priv = (Check.isEmpty(privilege) ? "LIST_COLLECTION" : privilege);
		Collection<ItemDefinition> collections = getEntityService().enumerate();
		collections = aclManager.filterNonGrantedObjects(Collections.singleton(priv), collections);

		for( ItemDefinition col : collections )
		{
			colbeans.add(serialize(col, null, false));
		}
		result.setResults(colbeans);
		result.setStart(0);
		result.setAvailable(collections.size());
		result.setLength(colbeans.size());
		return result;
	}

	@Override
	protected CollectionBean serialize(ItemDefinition entity, Object data, boolean heavy)
	{
		final CollectionBean bean = super.serialize(entity, data, heavy);
		if( heavy )
		{
			//add schema links
			final BaseEntityReference schema = bean.getSchema();
			if( schema != null )
			{
				final Map<String, String> schemaLinks = Collections.singletonMap("self",
					getSelfUri(SchemaResource.class, schema.getUuid()).toString());
				schema.set("links", schemaLinks);
			}
		}
		return bean;
	}

	@Override
	protected int getSecurityPriority()
	{
		return SecurityConstants.PRIORITY_COLLECTION;
	}

	@Override
	protected Class<CollectionResource> getResourceClass()
	{
		return CollectionResource.class;
	}

	@Override
	protected AbstractEntityService<?, ItemDefinition> getEntityService()
	{
		return collectionService;
	}

	@Override
	protected BaseEntitySerializer<ItemDefinition, CollectionBean> getSerializer()
	{
		return collectionSerializer;
	}

	@Override
	protected Node[] getAllNodes()
	{
		return new Node[]{Node.ALL_COLLECTIONS, Node.GLOBAL_ITEM_STATUS};
	}

	@Override
	protected AllCollectionsSecurityBean createAllSecurityBean()
	{
		return new AllCollectionsSecurityBean();
	}
}
