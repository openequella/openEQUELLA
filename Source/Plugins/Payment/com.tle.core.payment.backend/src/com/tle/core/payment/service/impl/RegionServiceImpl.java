package com.tle.core.payment.service.impl;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.edge.common.valuebean.ValidationError;
import com.google.common.collect.Sets;
import com.tle.common.EntityPack;
import com.tle.common.payment.entity.Region;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.guice.Bind;
import com.tle.core.payment.dao.RegionDao;
import com.tle.core.payment.events.RegionDeletionEvent;
import com.tle.core.payment.events.RegionReferencesEvent;
import com.tle.core.payment.service.RegionService;
import com.tle.core.payment.service.session.RegionEditingBean;
import com.tle.core.payment.service.session.RegionEditingSession;
import com.tle.core.security.impl.SecureEntity;
import com.tle.core.services.entity.EntityEditingSession;
import com.tle.core.services.entity.impl.AbstractEntityServiceImpl;

@Bind(RegionService.class)
@Singleton
@SecureEntity(RegionService.ENTITY_TYPE)
public class RegionServiceImpl extends AbstractEntityServiceImpl<RegionEditingBean, Region, RegionService>
	implements
		RegionService
{
	// private final RegionDao regionDao;

	@Inject
	public RegionServiceImpl(RegionDao regionDao)
	{
		super(Node.REGION, regionDao);
		// this.regionDao = regionDao;
	}

	@Override
	public boolean containsCountry(Set<Region> regions, String country)
	{
		if( regions == null )
		{
			return false;
		}
		final Set<String> countries = Sets.newHashSet();
		for( Region region : regions )
		{
			countries.addAll(region.getCountries());
		}
		return countries.contains(country);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <SESSION extends EntityEditingSession<RegionEditingBean, Region>> SESSION createSession(String sessionId,
		EntityPack<Region> pack, RegionEditingBean bean)
	{
		return (SESSION) new RegionEditingSession(sessionId, pack, bean);
	}

	@Override
	protected boolean isUseEditingBean()
	{
		return true;
	}

	@Override
	protected RegionEditingBean createEditingBean()
	{
		return new RegionEditingBean();
	}

	@Override
	protected void populateEditingBean(RegionEditingBean bean, Region entity)
	{
		super.populateEditingBean(bean, entity);

		final RegionEditingBean regionBean = bean;
		final Set<String> countries = regionBean.getCountries();
		countries.clear();

		final Set<String> peristedCountries = entity.getCountries();
		if( peristedCountries != null )
		{
			countries.addAll(peristedCountries);
		}
	}

	@Override
	protected void populateEntity(RegionEditingBean bean, Region entity)
	{
		super.populateEntity(bean, entity);

		final RegionEditingBean regionBean = bean;
		final Set<String> countries = regionBean.getCountries();
		entity.setCountries(Sets.newHashSet(countries));
	}

	@Override
	protected void doValidation(EntityEditingSession<RegionEditingBean, Region> session, Region entity,
		List<ValidationError> errors)
	{

	}

	@Override
	public List<Class<?>> getReferencingClasses(long id)
	{
		final RegionReferencesEvent event = new RegionReferencesEvent(get(id));
		publishEvent(event);
		return event.getReferencingClasses();
	}

	@Override
	protected void deleteReferences(Region entity)
	{
		super.deleteReferences(entity);
		publishEvent(new RegionDeletionEvent(entity));
	}
}
