package com.tle.core.taxonomy.datasource;

import com.tle.common.taxonomy.Taxonomy;

public interface TaxonomyDataSourceFactory
{
	TaxonomyDataSource create(Taxonomy taxonomy) throws Exception;
}
