<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<@css "formdialog.css"/>

<@settingContainer mandatory=false>
	<#list m.controls as control>
		<@setting label=control.label section=control.control help=control.help />
	</#list>
</@settingContainer>
