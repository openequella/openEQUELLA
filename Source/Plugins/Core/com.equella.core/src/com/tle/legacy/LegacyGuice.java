/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.legacy;

import com.google.inject.AbstractModule;
import com.tle.cal.service.CALService;
import com.tle.cal.web.service.CALWebServiceImpl;
import com.tle.common.scripting.service.ScriptingService;
import com.tle.core.accessibility.AccessibilityModeService;
import com.tle.core.activation.service.ActivationService;
import com.tle.core.auditlog.AuditLogService;
import com.tle.core.cloudproviders.CloudProviderRegistrationService;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.dynacollection.DynaCollectionService;
import com.tle.core.encryption.EncryptionService;
import com.tle.core.events.services.EventService;
import com.tle.core.facetedsearch.service.FacetedSearchClassificationService;
import com.tle.core.favourites.service.BookmarkService;
import com.tle.core.favourites.service.FavouriteSearchService;
import com.tle.core.fedsearch.FederatedSearchService;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.hierarchy.HierarchyService;
import com.tle.core.i18n.BundleCache;
import com.tle.core.i18n.service.LanguageService;
import com.tle.core.institution.InstitutionService;
import com.tle.core.item.edit.ItemEditorService;
import com.tle.core.item.edit.attachment.AbstractAttachmentEditor;
import com.tle.core.item.helper.ItemHelper;
import com.tle.core.item.serializer.ItemSerializerService;
import com.tle.core.item.serializer.impl.AttachmentSerializerProvider;
import com.tle.core.item.service.DrmService;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.service.ItemCommentService;
import com.tle.core.jackson.ObjectMapperService;
import com.tle.core.lti13.service.LtiPlatformService;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.newentity.service.EntityService;
import com.tle.core.oauth.service.OAuthService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.powersearch.PowerSearchService;
import com.tle.core.replicatedcache.ReplicatedCacheService;
import com.tle.core.replicatedcache.dao.ReplicatedCacheDao;
import com.tle.core.search.service.impl.SearchPrivilegeTreeProvider;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.UrlService;
import com.tle.core.services.user.UserPreferenceService;
import com.tle.core.services.user.UserService;
import com.tle.core.services.user.UserSessionService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.core.usermanagement.standard.dao.TLEUserDao;
import com.tle.freetext.FreetextIndex;
import com.tle.integration.oidc.OidcSettingsPrivilegeTreeProvider;
import com.tle.mycontent.service.MyContentService;
import com.tle.web.api.item.ItemLinkService;
import com.tle.web.api.search.service.ExportService;
import com.tle.web.contentrestrictions.ContentRestrictionsPrivilegeTreeProvider;
import com.tle.web.coursedefaults.CourseDefaultsSettingsPrivilegeTreeProvider;
import com.tle.web.customdateformat.DateFormatSettingsPrivilegeTreeProvider;
import com.tle.web.customisation.ThemePrivilegeTreeProvider;
import com.tle.web.diagnostics.security.DiagnosticsSettingsPrivilegeTreeProvider;
import com.tle.web.freemarker.BasicFreemarkerFactory;
import com.tle.web.google.analytics.GoogleAnalyticsPrivilegeTreeProvider;
import com.tle.web.google.api.privileges.GoogleApiSettingsPrivilegeTreeProvider;
import com.tle.web.harvesterskipdrmsettings.HarvesterSkipDrmSettingsPrivilegeTreeProvider;
import com.tle.web.htmleditor.settings.HtmlEditorSettingsPrivilegeTreeProvider;
import com.tle.web.integration.service.IntegrationService;
import com.tle.web.language.LanguageSettingsPrivilegeTreeProvider;
import com.tle.web.loggedinusers.LoggedInUsersPrivilegeTreeProvider;
import com.tle.web.login.LoginSettingsPrivilegeTreeProvider;
import com.tle.web.loginnotice.LoginNoticeEditorPrivilegeTreeProvider;
import com.tle.web.lti13.platforms.security.LTI13PlatformsSettingsPrivilegeTreeProvider;
import com.tle.web.mail.MailSettingsPrivilegeTreeProvider;
import com.tle.web.manualdatafixes.ManualDataFixesPrivilegeTreeProvider;
import com.tle.web.mimetypes.MimeSearchPrivilegeTreeProvider;
import com.tle.web.navigation.MenuService;
import com.tle.web.oaiidentifier.OaiIdentifierSettingsPrivilegeTreeProvider;
import com.tle.web.portal.service.PortletWebService;
import com.tle.web.quickcontributeandversion.QuickContributeAndVersionSettingsPrivilegeTreeProvider;
import com.tle.web.remotecaching.RemoteCachingPrivilegeTreeProvider;
import com.tle.web.scheduler.ScheduledTasksPrivilegeTreeProvider;
import com.tle.web.sections.SectionsController;
import com.tle.web.sections.registry.TreeRegistry;
import com.tle.web.selection.SelectionService;
import com.tle.web.shortcuturls.ShortcutUrlsSettingsPrivilegeTreeProvider;
import com.tle.web.template.TemplateFilter;
import com.tle.web.viewable.impl.ViewableItemFactory;
import com.tle.web.viewitem.service.ItemXsltService;
import com.tle.web.viewurl.ViewItemService;
import com.tle.web.viewurl.ViewItemUrlFactory;
import com.tle.web.viewurl.attachments.AttachmentResourceService;
import com.tle.web.workflow.tasks.ModerationService;
import javax.inject.Inject;
import javax.inject.Provider;

public class LegacyGuice extends AbstractModule {

  @Inject public static AccessibilityModeService accessibilityModeService;

  @Inject public static ActivationService activationService;

  @Inject public static AttachmentResourceService attachmentResourceService;

  @Inject public static AttachmentSerializerProvider attachmentSerializerProvider;

  @Inject public static AuditLogService auditLogService;

  @Inject public static BasicFreemarkerFactory basicFreemarkerFactory;

  @Inject public static BookmarkService bookmarkService;

  @Inject public static BundleCache bundleCache;

  @Inject public static CALService calService;

  @Inject public static CALWebServiceImpl calWebService;

  @Inject public static CloudProviderRegistrationService cloudProviderRegistrationService;

  @Inject public static ConfigurationService configService;

  @Inject public static ContentRestrictionsPrivilegeTreeProvider contentRestricPrivProvider;

  @Inject public static CourseDefaultsSettingsPrivilegeTreeProvider courseDefPrivProvider;

  @Inject public static DateFormatSettingsPrivilegeTreeProvider datePrivProvider;

  @Inject public static DiagnosticsSettingsPrivilegeTreeProvider diagnosticPrivProvider;

  @Inject public static DrmService drmService;

  @Inject public static DynaCollectionService dynaCollectionService;

  @Inject public static EncryptionService encryptionService;

  @Inject public static EntityService entityService;

  @Inject public static EventService eventService;

  @Inject public static ExportService exportService;

  @Inject public static FacetedSearchClassificationService facetedSearchClassificationService;

  @Inject public static FavouriteSearchService favouriteSearchService;

  @Inject public static FederatedSearchService federatedSearchService;

  @Inject public static FileSystemService fileSystemService;

  @Inject public static FreetextIndex freetextIndex;

  @Inject public static FreeTextService freeTextService;

  @Inject public static GoogleAnalyticsPrivilegeTreeProvider analyticsPrivProvider;

  @Inject public static GoogleApiSettingsPrivilegeTreeProvider googlePrivProvider;

  @Inject public static HarvesterSkipDrmSettingsPrivilegeTreeProvider harvesterPrivProvider;

  @Inject public static HtmlEditorSettingsPrivilegeTreeProvider htmlEditorPrivProvider;

  @Inject public static InstitutionService institutionService;

  @Inject public static HierarchyService hierarchyService;

  @Inject public static ItemCommentService itemCommentService;

  @Inject public static ItemDefinitionService itemDefinitionService;

  @Inject public static ItemEditorService itemEditorService;

  @Inject public static ItemHelper itemHelper;

  @Inject public static ItemLinkService itemLinkService;

  @Inject public static ItemSerializerService itemSerializerService;

  @Inject public static ItemService itemService;

  @Inject public static ItemXsltService itemXsltService;

  @Inject public static LanguageService languageService;

  @Inject public static LtiPlatformService ltiPlatformService;

  @Inject public static LTI13PlatformsSettingsPrivilegeTreeProvider ltiPrivProvider;

  @Inject public static LanguageSettingsPrivilegeTreeProvider langPrivProvider;

  @Inject public static LoggedInUsersPrivilegeTreeProvider liuPrivProvider;

  @Inject
  public static LoginNoticeEditorPrivilegeTreeProvider loginNoticeEditorPrivilegeTreeProvider;

  @Inject public static LTI13PlatformsSettingsPrivilegeTreeProvider lti13PrivilegeTreeProvider;

  @Inject public static OidcSettingsPrivilegeTreeProvider oidcSettingsPrivilegeTreeProvider;

  @Inject public static LoginSettingsPrivilegeTreeProvider loginPrivProvider;

  @Inject public static MailSettingsPrivilegeTreeProvider mailPrivProvider;

  @Inject public static ManualDataFixesPrivilegeTreeProvider manualFixPrivProvider;

  @Inject public static MenuService menuService;

  @Inject public static MimeSearchPrivilegeTreeProvider mimePrivProvider;

  @Inject public static MimeTypeService mimeTypeService;

  @Inject public static ModerationService moderationService;

  @Inject public static MyContentService myContentService;

  @Inject public static OAuthService oAuthService;

  @Inject public static OaiIdentifierSettingsPrivilegeTreeProvider oaiPrivProvider;

  @Inject public static ObjectMapperService objectMapperService;

  @Inject public static PluginTracker<AbstractAttachmentEditor> attachEditorTracker;

  @Inject public static PortletWebService portletWebService;

  @Inject public static PowerSearchService powerSearchService;

  @Inject public static Provider<IntegrationService> integrationService;

  @Inject public static Provider<SelectionService> selectionService;

  @Inject
  public static QuickContributeAndVersionSettingsPrivilegeTreeProvider quickContribPrivProvider;

  @Inject public static RemoteCachingPrivilegeTreeProvider remoteCachePrivProvider;

  @Inject public static ReplicatedCacheDao replicatedCacheDao;

  @Inject public static ReplicatedCacheService replicatedCacheService;

  @Inject public static ScheduledTasksPrivilegeTreeProvider scheduledPrivProvider;

  @Inject public static ScriptingService scriptingService;

  @Inject public static SearchPrivilegeTreeProvider searchPrivProvider;

  @Inject public static SectionsController sectionsController;

  @Inject public static ShortcutUrlsSettingsPrivilegeTreeProvider shortcutPrivProvider;

  @Inject public static TLEAclManager aclManager;

  @Inject public static TLEUserDao tleUserDao;

  @Inject public static TemplateFilter templateFilter;

  @Inject public static ThemePrivilegeTreeProvider themePrivProvider;

  @Inject public static TreeRegistry treeRegistry;

  @Inject public static UrlService urlService;

  @Inject public static UserPreferenceService userPreferenceService;

  @Inject public static UserService userService;

  @Inject public static UserSessionService userSessionService;

  @Inject public static ViewItemService viewItemService;

  @Inject public static ViewItemUrlFactory viewItemUrlFactory;

  @Inject public static ViewableItemFactory viewableItemFactory;

  @Override
  protected void configure() {
    requestStaticInjection(LegacyGuice.class);
  }
}
