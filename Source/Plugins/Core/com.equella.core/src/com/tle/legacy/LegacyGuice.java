package com.tle.legacy;

import com.google.inject.AbstractModule;
import com.tle.core.search.service.impl.SearchPrivilegeTreeProvider;
import com.tle.core.security.TLEAclManager;
import com.tle.web.contentrestrictions.ContentRestrictionsPrivilegeTreeProvider;
import com.tle.web.coursedefaults.CourseDefaultsSettingsPrivilegeTreeProvider;
import com.tle.web.customdateformat.DateFormatSettingsPrivilegeTreeProvider;
import com.tle.web.customisation.ThemePrivilegeTreeProvider;
import com.tle.web.diagnostics.security.DiagnosticsSettingsPrivilegeTreeProvider;
import com.tle.web.google.analytics.GoogleAnalyticsPrivilegeTreeProvider;
import com.tle.web.google.api.privileges.GoogleApiSettingsPrivilegeTreeProvider;
import com.tle.web.harvesterskipdrmsettings.HarvesterSkipDrmSettingsPrivilegeTreeProvider;
import com.tle.web.htmleditor.settings.HtmlEditorSettingsPrivilegeTreeProvider;
import com.tle.web.language.LanguageSettingsPrivilegeTreeProvider;
import com.tle.web.loggedinusers.LoggedInUsersPrivilegeTreeProvider;
import com.tle.web.login.LoginSettingsPrivilegeTreeProvider;
import com.tle.web.mail.MailSettingsPrivilegeTreeProvider;
import com.tle.web.manualdatafixes.ManualDataFixesPrivilegeTreeProvider;
import com.tle.web.mimetypes.MimeSearchPrivilegeTreeProvider;
import com.tle.web.oaiidentifier.OaiIdentifierSettingsPrivilegeTreeProvider;
import com.tle.web.portal.service.PortletWebService;
import com.tle.web.quickcontributeandversion.QuickContributeAndVersionSettingsPrivilegeTreeProvider;
import com.tle.web.remotecaching.RemoteCachingPrivilegeTreeProvider;
import com.tle.web.scheduler.ScheduledTasksPrivilegeTreeProvider;
import com.tle.web.shortcuturls.ShortcutUrlsSettingsPrivilegeTreeProvider;

import javax.inject.Inject;

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

    @Override
    protected void configure()
    {
        requestStaticInjection(LegacyGuice.class);
    }
}
