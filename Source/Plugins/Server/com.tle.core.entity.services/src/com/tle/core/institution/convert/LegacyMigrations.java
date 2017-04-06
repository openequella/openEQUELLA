package com.tle.core.institution.convert;

import java.util.Collection;
import java.util.Map;

import com.dytech.common.text.NumberStringComparator;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;

@SuppressWarnings("nls")
public final class LegacyMigrations
{
	private LegacyMigrations()
	{
		throw new Error();
	}

	private static TreeMultimap<String, String> buildXmlMigrations()
	{
		TreeMultimap<String, String> xmlMigs = TreeMultimap.create(new NumberStringComparator<String>(),
			Ordering.natural());

		add31Migrators(xmlMigs);
		add32Migrators(xmlMigs);
		add40Migrators(xmlMigs);
		add41Migrators(xmlMigs);
		add50Migrators(xmlMigs);

		return xmlMigs;
	}

	private static TreeMultimap<String, String> buildItemXmlMigrations()
	{
		TreeMultimap<String, String> itemXmlMigs = TreeMultimap.create(new NumberStringComparator<String>(),
			Ordering.natural());

		add31ItemMigrators(itemXmlMigs);
		add32ItemMigrators(itemXmlMigs);
		add40ItemMigrators(itemXmlMigs);
		add41ItemMigrators(itemXmlMigs);
		add50ItemMigrators(itemXmlMigs);

		return itemXmlMigs;
	}

	private static TreeMultimap<String, String> buildPostReadMigrations()
	{
		TreeMultimap<String, String> postReadMigs = TreeMultimap.create(new NumberStringComparator<String>(),
			Ordering.natural());

		add31PostReadMigrators(postReadMigs);
		add32PostReadMigrators(postReadMigs);
		add40PostReadMigrators(postReadMigs);
		add41PostReadMigrators(postReadMigs);
		add50PostReadMigrators(postReadMigs);

		return postReadMigs;
	}

	private static void add31PostReadMigrators(Multimap<String, String> postReadMigs)
	{
		postReadMigs.put("3.1", "com.tle.ims.migration.IMSNavigationCreation");
		postReadMigs.put("3.1", "com.tle.core.institution.migration.EnsureAttachmentUUID");
		postReadMigs.put("3.1", "com.tle.core.institution.migration.SearchDetailsUpdate");
		postReadMigs.put("3.1", "com.tle.core.institution.migration.ConfigPropertyChange");
		postReadMigs.put("3.1", "com.tle.core.institution.migration.AclPriorityChange");
	}

	private static void add32PostReadMigrators(Multimap<String, String> postReadMigs)
	{
		postReadMigs.put("3.2", "com.tle.cal.migration.CalPrivMigrator");
		postReadMigs.put("3.2.22212", "com.tle.core.institution.migration.v32.RemoveEmailTemplatePrivMigrator");
	}

	private static void add40PostReadMigrators(Multimap<String, String> postReadMigs)
	{
		postReadMigs.put("4.0", "com.tle.web.myresource.converter.MyResourceACLMigrator");
		postReadMigs.put("4.0", "com.tle.core.institution.migration.v40.EditHierarchyPrivilegeMigrator");
	}

	private static void add41PostReadMigrators(Multimap<String, String> postReadMigs)
	{
		postReadMigs.put("4.1", "com.tle.core.institution.migration.v41.SavedSearchesMigrator");
		postReadMigs.put("4.1", "com.tle.core.workflow.migrate.RemoveOrphanedWorkflowStatus");
	}

	private static void add50PostReadMigrators(Multimap<String, String> postReadMigs)
	{
		postReadMigs.put("5.0", "com.tle.core.portal.migration.v50.forimport.PortletAclXmlMigration");
	}

	private static void add31ItemMigrators(Multimap<String, String> itemXmlMigs)
	{
		itemXmlMigs.put("3.1", "com.tle.core.institution.migration.DisallowOldStyleCalItems");
		itemXmlMigs.put("3.1", "com.tle.core.institution.migration.RemoveDeprecatedItemParts");
		itemXmlMigs.put("3.1", "com.tle.core.institution.migration.LanguageBundleMigration");
	}

	private static void add32ItemMigrators(Multimap<String, String> itemXmlMigs)
	{
		itemXmlMigs.put("3.2", "com.tle.cal.migration.CalItemXmlConvert");
		itemXmlMigs.put("3.2", "com.tle.core.institution.migration.v32.UpdateItemNavigationTree");
		itemXmlMigs.put("3.2", "com.tle.core.institution.migration.v32.UpdateHistoryEventMigrator");
	}

	private static void add40ItemMigrators(Multimap<String, String> itemXmlMigs)
	{
		itemXmlMigs.put("4.0", "com.tle.web.myresource.converter.MyResourceConverter");
		itemXmlMigs.put("4.0.21947", "com.tle.core.activation.migration.MigrateActivations");
	}

	private static void add41ItemMigrators(Multimap<String, String> itemXmlMigs)
	{
		itemXmlMigs.put("4.1", "com.tle.core.workflow.migrate.WorkflowItemXmlMigrator");
	}

	private static void add50ItemMigrators(Multimap<String, String> itemXmlMigs)
	{
		itemXmlMigs.put("5.0", "com.tle.core.legacy.migration.v50.RemoveAssemblerAndActivityWizardItemsFix");
		itemXmlMigs.put("5.0", "com.tle.core.legacy.migration.v50.RemoveExistingThumbnailsXml");
		itemXmlMigs.put("5.0.27550", "com.tle.core.notification.institution.NotificationConverter");
		itemXmlMigs.put("5.0.27876",
			"com.tle.core.wizard.controls.universal.migration.v50.ReplaceDeletedControlsItemXmlMigration");
		itemXmlMigs.put("5.0.28454", "com.tle.core.institution.migration.v41.CommentUuidXmlMigration");

	}

	private static void add31Migrators(Multimap<String, String> xmlMigs)
	{
		xmlMigs.put("3.1", "com.tle.core.institution.migration.RemoveDeprecatedFedSearches");
		xmlMigs.put("3.1", "com.tle.core.institution.migration.TleUserSuspendedRemover");
		xmlMigs.put("3.1", "com.tle.core.institution.migration.SetEmailTemplateUuidsChange");
		xmlMigs.put("3.1", "com.tle.core.institution.migration.RemoveDeprecatedItemDefinitionParts");
		xmlMigs.put("3.1", "com.tle.core.institution.migration.LanguageBundleMigration");
	}

	private static void add32Migrators(Multimap<String, String> xmlMigs)
	{
		xmlMigs.put("3.2", "com.tle.cal.migration.CalPageMigration");
		xmlMigs.put("3.2", "com.tle.cal.migration.CalHierarchyMigrator");
		xmlMigs.put("3.2", "com.tle.core.institution.migration.v32.ConvertFedSearchAttributesToBaseEntityAttributes");
		xmlMigs.put("3.2", "com.tle.core.institution.migration.v32.RenameSchemaTransformations");

		xmlMigs.put("3.2.21273", "com.tle.core.institution.migration.v32.ChangeBlackZ3950UsernameToGuest");
	}

	private static void add40Migrators(Multimap<String, String> xmlMigs)
	{
		xmlMigs.put("4.0", "com.tle.core.mimetypes.institution.MimeMigrator");
		xmlMigs.put("4.0", "com.tle.web.myresource.converter.MyResourceSchemaMigrator");
		xmlMigs.put("4.0", "com.tle.core.institution.migration.v40.MigrateSystemField");
	}

	private static void add41Migrators(Multimap<String, String> xmlMigs)
	{
		xmlMigs.put("4.1", "com.tle.core.institution.migration.v41.EnsureItemFolder");
		xmlMigs.put("4.1.24325", "com.tle.core.institution.migration.v41.DisplayTemplateXmlMigrator");
		xmlMigs.put("4.1.24503", "com.tle.core.workflow.migrate.WorkflowXmlMigrator");
		xmlMigs.put("4.1.24503", "com.tle.core.wizard.institution.migration.v41.WizardLayoutXmlMigrator");
		xmlMigs.put("4.1.24520", "com.tle.core.taxonomy.institution.migration.TempTaxonomyXmlMigrator");
		xmlMigs.put("4.1.24520", "com.tle.core.taxonomy.institution.migration.TaxonomyNodeToTermXmlMigrator");
		xmlMigs.put("4.1.25700", "com.tle.core.institution.migration.v41.DisplayTemplateLocationXmlMigrator");
	}

	private static void add50Migrators(Multimap<String, String> xmlMigs)
	{
		xmlMigs.put("5.0", "com.tle.web.viewitem.migration.xml.v50.RemoveViewPackageContentXmlMigration");
		xmlMigs.put("5.0", "com.tle.core.legacy.migration.v50.RemoveNavigationGroupsAndLinksXml");
		xmlMigs.put("5.0", "com.tle.core.legacy.migration.v50.RemoveAssemblerAndActivityWizardEntitiesFix");
		xmlMigs.put("5.0", "com.tle.core.legacy.migration.v50.AddCommentsSectionToItemSummarySectionsXml");
		xmlMigs.put("5.0", "com.tle.core.legacy.migration.v50.ConvertXsltTemplateFileToStringXml");
		xmlMigs.put("5.0", "com.tle.core.legacy.migration.v50.RemoveHierarchyColumnsXml");
		xmlMigs.put("5.0", "com.tle.web.hierarchy.migration.SavedSearchToFavouriteSearchXml");
		xmlMigs.put("5.0", "com.tle.core.mimetypes.institution.RerunMimeMigrator");

		xmlMigs.put("5.0.27421", "com.tle.core.legacy.migration.v50.RemoveObsoleteCollectionSettingsXml");
		xmlMigs.put("5.0.27876",
			"com.tle.core.wizard.controls.universal.migration.v50.ReplaceDeletedControlsXmlMigration");
		xmlMigs.put("5.0.28174", "com.tle.core.legacy.migration.v50.RemoveCourseMigrationXml");
		xmlMigs.put("5.0.28276", "com.tle.core.legacy.migration.v50.RemovePopupFixViewerAndConvertPrettyViewerXml");

		xmlMigs.put("5.0.29456", "com.tle.core.fedsearch.migrations.RemoveGoogleAndEdnaAndDSMFedSearchMigrationXml");
	}

	public static Map<String, Collection<String>> getXmlMigrationsForVersion(String version)
	{
		return buildXmlMigrations().asMap().headMap(version, true);
	}

	public static Map<String, Collection<String>> getItemXmlMigrationsForVersion(String version)
	{
		return buildItemXmlMigrations().asMap().headMap(version, true);
	}

	public static Map<String, Collection<String>> getPostReadMigrationsForVersion(String version)
	{
		return buildPostReadMigrations().asMap().headMap(version, true);
	}
}
