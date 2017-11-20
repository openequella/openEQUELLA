<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#include "/com.tle.web.sections.standard@/calendar.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<@css "activations.css"/>

<h2>${b.key("editactivation.title")}</h2>
<div>
	<@settingContainer>
		<@setting label=b.key('editactivation.name')>${_userformat("$LoggedInUser")}</@setting>
		<@a.div id="courseajax">
			<@setting label=b.key("editactivation.course") mandatory=true>
				<span>(${m.course.code}) ${b.bundle(m.course.name)}</span><@button section=s.selectCourse />
			</@setting>
		</@a.div>
		<@setting label=b.key("editactivation.activatefrom") mandatory=true labelFor=s.fromDate>
			<@calendar section=s.fromDate notAfter=s.untilDate />
		</@setting>
		<@setting label=b.key("editactivation.activateuntil") mandatory=true labelFor=s.untilDate >
			<@calendar section=s.untilDate notBefore=s.fromDate />
		</@setting>
		<#if m.error??>
		<span class="date-error">${m.error}</span>
		</#if>
		<div class="button-strip">
		<@button section=s.saveButton showAs="save" />
		<@button section=s.cancelButton />
		</div>
	</@settingContainer>
</div>