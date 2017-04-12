package com.tle.core.payment.migration;

import java.util.Set;

import javax.inject.Singleton;

import com.google.common.collect.Sets;
import com.tle.common.payment.entity.TaxType;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateCreationFilter;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractCreateMigration;
import com.tle.core.migration.ClassDependencies;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.plugins.impl.PluginServiceImpl;

@SuppressWarnings("nls")
@Bind
@Singleton
public class CreateTaxTypeSchema extends AbstractCreateMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(CreateTaxTypeSchema.class)
		+ ".migration.createtaxtype.";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "title");
	}

	@Override
	protected HibernateCreationFilter getFilter(HibernateMigrationHelper helper)
	{
		return new TablesOnlyFilter(new String[]{"tax_type"});
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		final Set<Class<?>> domainClasses = Sets.newHashSet(ClassDependencies.baseEntity());
		domainClasses.add(TaxType.class);
		return domainClasses.toArray(new Class<?>[domainClasses.size()]);
	}
}
