package com.tle.core.taxonomy.impl;

import javax.inject.Singleton;

import com.tle.common.taxonomy.Taxonomy;
import com.tle.core.dao.impl.AbstractEntityDaoImpl;
import com.tle.core.guice.Bind;
import com.tle.core.taxonomy.TaxonomyDao;

@Bind(TaxonomyDao.class)
@Singleton
public class TaxonomyDaoImpl extends AbstractEntityDaoImpl<Taxonomy> implements TaxonomyDao
{
	public TaxonomyDaoImpl()
	{
		super(Taxonomy.class);
	}
}
