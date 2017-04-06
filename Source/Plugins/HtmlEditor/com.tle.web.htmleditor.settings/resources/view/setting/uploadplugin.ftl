<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.standard@/file.ftl">
 
<#if m.canAdd>
	<@settingContainer mandatory=false wide=true>
		<@setting label=b.key('settings.plugins.label.uploadplugin') labelFor=s.uploadPluginFile error=m.errors["plugin"]>
			<@file section=s.uploadPluginFile size=30 />
		</@setting>
	</@settingContainer>
</#if>