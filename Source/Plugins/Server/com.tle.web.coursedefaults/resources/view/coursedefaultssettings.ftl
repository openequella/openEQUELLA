<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/calendar.ftl"/>
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#include "/com.tle.web.sections.standard@/link.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>

<div class="area">
	<h2>${b.key('coursedefaults.title')}</h2>
	<@settingContainer mandatory=false>
		<@setting label=b.key('coursedefaults.startdate') labelFor=s.startDate >
			<@calendar section=s.startDate notAfter=s.endDate />
		</@>
		<@setting label=b.key('coursedefaults.enddate') labelFor=s.endDate >
			<@calendar section=s.endDate notBefore=s.startDate />
		</@>
	</@>

	<@div id="clear">
		<#if m.showClearLink>
			<@link section=s.clearButton />
		</#if>
	</@div>

	<div class="button-strip">
		<@button section=s.saveButton showAs="save" />
	</div>
</div>
