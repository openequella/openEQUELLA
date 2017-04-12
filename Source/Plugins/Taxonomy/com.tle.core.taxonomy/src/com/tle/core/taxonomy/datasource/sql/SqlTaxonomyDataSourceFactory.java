package com.tle.core.taxonomy.datasource.sql;

import static com.tle.common.taxonomy.datasource.sql.SqlTaxonomyDataSourceConstants.SQL_DATA_CLASS;
import static com.tle.common.taxonomy.datasource.sql.SqlTaxonomyDataSourceConstants.SQL_JDBC_URL;
import static com.tle.common.taxonomy.datasource.sql.SqlTaxonomyDataSourceConstants.SQL_PASSWORD;
import static com.tle.common.taxonomy.datasource.sql.SqlTaxonomyDataSourceConstants.SQL_USERNAME;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import com.tle.common.taxonomy.Taxonomy;
import com.tle.core.guice.Bind;
import com.tle.core.taxonomy.datasource.TaxonomyDataSource;
import com.tle.core.taxonomy.datasource.TaxonomyDataSourceFactory;
import com.zaxxer.hikari.HikariDataSource;

@Bind
@Singleton
public class SqlTaxonomyDataSourceFactory implements TaxonomyDataSourceFactory
{
	/**
	 * Weakly cache instantiated data sources so that we can share them between
	 * taxonomies using common settings.
	 */
	private final Map<String, WeakReference<HikariDataSource>> dataSourceCache = new HashMap<String, WeakReference<HikariDataSource>>();

	@Override
	public TaxonomyDataSource create(Taxonomy taxonomy) throws Exception
	{
		final Map<String, String> as = taxonomy.getAttributes();

		final String dc = as.get(SQL_DATA_CLASS);
		final String url = as.get(SQL_JDBC_URL);
		final String user = as.get(SQL_USERNAME);
		final String pass = as.get(SQL_PASSWORD);

		final String cacheKey = dc + url + user + pass;

		HikariDataSource source = null;

		synchronized( dataSourceCache )
		{
			WeakReference<HikariDataSource> wrds = dataSourceCache.get(cacheKey);
			if( wrds != null )
			{
				source = wrds.get();
			}

			if( source == null )
			{
				source = new HikariDataSource();
				source.setDriverClassName(dc);
				source.setJdbcUrl(url);
				source.setUsername(user);
				source.setPassword(pass);

				dataSourceCache.put(cacheKey, new WeakReference<>(source));
			}
		}

		return new SqlTaxonomyDataSource(source, as);
	}
}
