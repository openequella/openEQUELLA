package com.tle.core.legacy.migration;

import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Type;
import org.hibernate.classic.Session;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
public class RemoveObsoleteCollectionSettings extends AbstractHibernateSchemaMigration
{
	private static final String migInfo = PluginServiceImpl.getMyPluginId(RemoveObsoleteCollectionSettings.class) + ".removeobsoletecollectionsettings.title"; //$NON-NLS-1$

	private static final String ITEMDEF_TABLE = "item_definition"; //$NON-NLS-1$

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 6;
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		// You're Joking
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return Collections.emptyList();
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeBaseEntity.class, FakeItemDefinition.class};
	}

	@SuppressWarnings("nls")
	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return helper.getDropColumnSQL(ITEMDEF_TABLE, "icon_path", "icon_uploaded", "search_result_id",
			"thumbnail_height", "thumbnail_max", "type");

	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(migInfo);
	}

	@Entity(name = "ItemDefinition")
	@AccessType("field")
	public static class FakeItemDefinition extends FakeBaseEntity
	{
		@Column(length = 100)
		String iconPath;
		boolean iconUploaded;

		@Type(type = "blankable")
		@Column(length = 20)
		String type;

		int thumbnailMax;
		int thumbnailHeight;

		@Column(length = 16)
		String searchResultId;
	}

	@Entity(name = "BaseEntity")
	@AccessType("field")
	@Inheritance(strategy = InheritanceType.JOINED)
	public static class FakeBaseEntity
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;
	}
}
