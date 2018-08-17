/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import com.tle.core.activation.service.ActivationService;
import com.tle.core.events.services.EventService;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.i18n.service.LanguageService;
import com.tle.core.institution.InstitutionService;
import com.tle.core.item.standard.service.ItemCommentService;
import com.tle.core.jackson.ObjectMapperService;
import com.tle.core.notification.NotificationService;
import com.tle.core.search.service.impl.SearchPrivilegeTreeProvider;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.UrlService;
import com.tle.core.services.user.UserPreferenceService;
import com.tle.core.services.user.UserService;
import com.tle.core.services.user.UserSessionService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.core.usermanagement.standard.dao.TLEUserDao;
import com.tle.core.xml.service.XmlService;
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
import com.tle.web.viewurl.attachments.AttachmentResourceService;
import com.tle.web.workflow.tasks.ModerationService;

import javax.inject.Inject;
import javax.inject.Provider;

public class LegacyGuice extends AbstractModule {

    @Inject
    public static TLEAclManager aclManager;

    @Inject
    public static ShortcutUrlsSettingsPrivilegeTreeProvider shortcutPrivProvider;

    @Inject
    public static LanguageSettingsPrivilegeTreeProvider langPrivProvider;

    @Inject
    public static GoogleAnalyticsPrivilegeTreeProvider analyticsPrivProvider;

    @Inject
    public static GoogleApiSettingsPrivilegeTreeProvider googlePrivProvider;

    @Inject
    public static SearchPrivilegeTreeProvider searchPrivProvider;

    @Inject
    public static LoginSettingsPrivilegeTreeProvider loginPrivProvider;

    @Inject
    public static QuickContributeAndVersionSettingsPrivilegeTreeProvider quickContribPrivProvider;

    @Inject
    public static ManualDataFixesPrivilegeTreeProvider manualFixPrivProvider;

    @Inject
    public static OaiIdentifierSettingsPrivilegeTreeProvider oaiPrivProvider;

    @Inject
    public static HarvesterSkipDrmSettingsPrivilegeTreeProvider harvesterPrivProvider;

    @Inject
    public static ScheduledTasksPrivilegeTreeProvider scheduledPrivProvider;

    @Inject
    public static MimeSearchPrivilegeTreeProvider mimePrivProvider;

    @Inject
    public static DateFormatSettingsPrivilegeTreeProvider datePrivProvider;

    @Inject
    public static ThemePrivilegeTreeProvider themePrivProvider;

    @Inject
    public static HtmlEditorSettingsPrivilegeTreeProvider htmlEditorPrivProvider;

    @Inject
    public static DiagnosticsSettingsPrivilegeTreeProvider diagnosticPrivProvider;

    @Inject
    public static MailSettingsPrivilegeTreeProvider mailPrivProvider;

    @Inject
    public static RemoteCachingPrivilegeTreeProvider remoteCachePrivProvider;

    @Inject
    public static LoggedInUsersPrivilegeTreeProvider liuPrivProvider;

    @Inject
    public static CourseDefaultsSettingsPrivilegeTreeProvider courseDefPrivProvider;

    @Inject
    public static ContentRestrictionsPrivilegeTreeProvider contentRestricPrivProvider;

    @Inject
    public static PortletWebService portletWebService;

    @Inject
    public static InstitutionService institutionService;

    @Inject
    public static EventService eventService;

    @Inject
    public static UserSessionService userSessionService;

    @Inject
    public static UserPreferenceService userPreferenceService;

    @Inject
    public static UserService userService;

    @Inject
    public static LanguageService languageService;

    @Inject
    public static ConfigurationService configService;

    @Inject
    public static TreeRegistry treeRegistry;

    @Inject
    public static SectionsController sectionsController;

    @Inject
    public static TLEUserDao tleUserDao;

    @Inject
    public static UrlService urlService;

    @Inject
    public static MenuService menuService;

    @Inject
    public static ViewableItemFactory viewableItemFactory;

    @Inject
    public static ModerationService moderationService;

    @Inject
    public static FreeTextService freeTextService;

    @Inject
    public static TemplateFilter templateFilter;

    @Inject
    public static ObjectMapperService objectMapperService;

    @Inject
    public static AttachmentResourceService attachmentResourceService;

    @Inject
    public static ItemXsltService itemXsltService;

    @Inject
    public static ScriptingService scriptingService;

    @Inject
    public static BasicFreemarkerFactory basicFreemarkerFactory;

    @Inject
    public static ItemCommentService itemCommentService;

    @Inject
    public static CALService calService;

    @Inject
    public static ActivationService activationService;

    @Inject
    public static CALWebServiceImpl calWebService;

    @Inject
    public static ViewItemService viewItemService;

    @Override
    protected void configure()
    {
        requestStaticInjection(LegacyGuice.class);
    }
}
