package com.tle.core.payment.storefront.service.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dytech.edge.common.valuebean.ValidationError;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.Check;
import com.tle.common.EntityPack;
import com.tle.common.i18n.LangUtils;
import com.tle.common.payment.storefront.entity.Store;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.events.listeners.ItemDefinitionDeletionListener;
import com.tle.core.events.listeners.ItemDefinitionReferencesListener;
import com.tle.core.guice.Bind;
import com.tle.core.payment.storefront.constants.StoreFrontConstants;
import com.tle.core.payment.storefront.dao.StoreDao;
import com.tle.core.payment.storefront.service.StoreService;
import com.tle.core.payment.storefront.service.session.StoreEditingBean;
import com.tle.core.payment.storefront.service.session.StoreEditingSession;
import com.tle.core.payment.storefront.settings.StoreFrontSettings;
import com.tle.core.security.impl.SecureEntity;
import com.tle.core.security.impl.SecureOnReturn;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.services.entity.EntityEditingSession;
import com.tle.core.services.entity.impl.AbstractEntityServiceImpl;

@Bind(StoreService.class)
@Singleton
@SecureEntity(Store.ENTITY_TYPE)
public class StoreServiceImpl extends AbstractEntityServiceImpl<StoreEditingBean, Store, StoreService>
	implements
		StoreService,
		ItemDefinitionDeletionListener,
		ItemDefinitionReferencesListener
{
	@Inject
	private StoreDao storeDao;
	@Inject
	private ConfigurationService configService;

	@Inject
	public StoreServiceImpl(StoreDao dao)
	{
		super(Node.STORE, dao);
		this.storeDao = dao;
	}

	@Override
	protected void doValidation(EntityEditingSession<StoreEditingBean, Store> session, Store entity,
		List<ValidationError> errors)
	{
		// do nothing by default
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <SESSION extends EntityEditingSession<StoreEditingBean, Store>> SESSION createSession(String sessionId,
		EntityPack<Store> pack, StoreEditingBean bean)
	{
		return (SESSION) new StoreEditingSession(sessionId, pack, bean);
	}

	@Override
	protected boolean isUseEditingBean()
	{
		return true;
	}

	@Override
	protected StoreEditingBean createEditingBean()
	{
		return new StoreEditingBean();
	}

	@Override
	protected void populateEditingBean(StoreEditingBean seb, Store entity)
	{
		super.populateEditingBean(seb, entity);
		seb.setStoreUrl(entity.getStoreUrl());
		seb.setClientId(entity.getClientId());
		seb.setEnabled(!entity.isDisabled());
		seb.setToken(entity.getToken());
		seb.setLastHarvest(entity.getLastHarvest());
	}

	@Override
	protected void populateEntity(StoreEditingBean seb, Store entity)
	{
		super.populateEntity(seb, entity);
		entity.setStoreUrl(seb.getStoreUrl());
		entity.setClientId(seb.getClientId());
		entity.setToken(seb.getToken());
		entity.setLastHarvest(seb.getLastHarvest());
	}

	@SecureOnReturn(priv = StoreFrontConstants.PRIV_BROWSE_STORE)
	@Override
	public List<Store> enumerateBrowsable()
	{
		List<Store> sortedStores = storeDao.enumerateEnabled();
		if( sortedStores.size() > 1 )
		{
			Collections.sort(sortedStores, new Comparator<Store>()
			{
				@Override
				public int compare(Store store1, Store store2)
				{
					return getName(store1).compareToIgnoreCase(getName(store2));
				}

				private String getName(Store store)
				{
					return LangUtils.getString(store.getName());
				}
			});
		}
		return sortedStores;
	}

	@Override
	@Transactional
	public List<Store> findAll()
	{
		return storeDao.findAllByCriteria(getInstitutionCriterion());
	}

	@Override
	@Transactional
	public void updateHarvestDate(Store store)
	{
		store.setLastHarvest(new Date());
		storeDao.saveOrUpdate(store);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void removeReferences(ItemDefinition itemDefinition)
	{
		String uuidOfDeleted = itemDefinition.getUuid();
		if( !Check.isEmpty(uuidOfDeleted) )
		{
			StoreFrontSettings settings = configService.getProperties(new StoreFrontSettings());
			if( uuidOfDeleted.equals(settings.getCollection()) )
			{
				settings.setCollection(null);
				configService.setProperties(settings);
			}
		}
	}

	@Override
	public void addItemDefinitionReferencingClasses(ItemDefinition collection, List<Class<?>> classes)
	{
		StoreFrontSettings settings = configService.getProperties(new StoreFrontSettings());
		if( settings.getCollection().equals(collection.getUuid()) )
		{
			classes.add(StoreFrontSettings.class);
		}
	}

	@Override
	public boolean storeHasHistory(Store store)
	{
		Long count = storeDao.countOrderPartsForStore(store);
		return count > 0;
	}

	@Override
	public boolean isUrlRegistered(String url, long id)
	{
		if( !url.endsWith("/") )
		{
			url = url + "/";
		}

		List<Store> allStores = enumerate();
		for( Store store : allStores )
		{
			if( store.getId() == id )
			{
				continue;
			}
			String storeUrl = store.getStoreUrl();
			if( !storeUrl.endsWith("/") )
			{
				storeUrl = storeUrl + "/";
			}

			if( storeUrl.equals(url) )
			{
				return true;
			}
		}
		return false;
	}
}
