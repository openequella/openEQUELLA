package com.tle.core.taxonomy.datasource.internal;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.taxonomy.Taxonomy;
import com.tle.core.guice.Bind;
import com.tle.core.taxonomy.TermService;
import com.tle.core.taxonomy.datasource.TaxonomyDataSource;
import com.tle.core.taxonomy.datasource.TaxonomyDataSourceFactory;

@Bind
@Singleton
public class InternalTaxonomyDataSourceFactory implements TaxonomyDataSourceFactory
{
	@Inject
	private TermService termService;

	@Override
	public TaxonomyDataSource create(Taxonomy taxonomy) throws Exception
	{
		return new InternalTaxonomyDataSource(taxonomy, termService);
	}
}
