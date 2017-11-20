<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<h3>${b.key('databases.dialog.main.heading')}</h3>
<p>${b.key('databases.dialog.main.description', b.key('databases.dialog.main.description.' + m.requirementsDb))}</p>

<@settingContainer>
		<@setting label=b.key('databases.dialog.usesystem') help=b.key('databases.dialog.usesystem.help') mandatory=true section=s.useSystem error=m.errors['usesystem'] />
		<@setting label=b.key('databases.dialog.url') help=b.key('databases.dialog.url.' + m.requirementsDb) mandatory=true section=s.url error=m.errors['url'] />
		<@setting label=b.key('databases.dialog.username') help=b.key('databases.dialog.username.' + m.requirementsDb) mandatory=true section=s.username error=m.errors['username'] />
		<@setting label=b.key('databases.dialog.password') mandatory=(m.schemaId lte 0) section=s.password  error=m.errors['password'] />
</@settingContainer>

<h3>${b.key('databases.dialog.reporting.heading')}</h3>
<p>${b.key('databases.dialog.reporting.description')}</p>

<@settingContainer mandatory=false>
		<@setting label=b.key('databases.dialog.url') section=s.reportingUrl />
		<@setting label=b.key('databases.dialog.username') help=b.key('databases.dialog.username.' + m.requirementsDb) section=s.reportingUsername />
		<@setting label=b.key('databases.dialog.password') section=s.reportingPassword />
</@settingContainer>

<h3>${b.key('databases.dialog.otherdetails')}</h3>
<@settingContainer mandatory=false>
		<@setting label=b.key('databases.dialog.description') section=s.description />
</@settingContainer>	