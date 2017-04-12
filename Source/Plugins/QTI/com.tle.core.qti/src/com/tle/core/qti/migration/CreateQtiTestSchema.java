package com.tle.core.qti.migration;

import java.util.Set;

import javax.inject.Singleton;

import com.google.common.collect.Sets;
import com.tle.beans.Institution;
import com.tle.common.qti.entity.QtiAssessmentItem;
import com.tle.common.qti.entity.QtiAssessmentItemRef;
import com.tle.common.qti.entity.QtiAssessmentTest;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateCreationFilter;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractCreateMigration;
import com.tle.core.migration.ClassDependencies;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.plugins.impl.PluginServiceImpl;

/**
 * Creates the Test schema, as opposed to the results which will come later.
 * 
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class CreateQtiTestSchema extends AbstractCreateMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(CreateQtiTestSchema.class)
		+ ".migration.createschema.";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "title");
	}

	@Override
	protected HibernateCreationFilter getFilter(HibernateMigrationHelper helper)
	{
		return new TablesOnlyFilter(new String[]{"qti_assessment_test", "qti_assessment_item",
				"qti_assessment_item_ref"});
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		final Set<Class<?>> domainClasses = Sets.newHashSet();
		domainClasses.add(QtiAssessmentItem.class);
		domainClasses.add(QtiAssessmentItemRef.class);
		domainClasses.add(QtiAssessmentTest.class);
		domainClasses.add(Institution.class);
		domainClasses.addAll(ClassDependencies.item());
		return domainClasses.toArray(new Class<?>[domainClasses.size()]);
	}
}
