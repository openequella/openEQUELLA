<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<h2>${b.key('cloud.settings.title')}</h2>
<@settingContainer mandatory=false wide=true>
	<@setting label=b.key('settings.label.disablecloud') 
		section=s.disableCloudCheckbox
		help=b.key('settings.help.disablecloud')  />
</@settingContainer>