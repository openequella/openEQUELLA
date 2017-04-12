<#ftl strip_whitespace=true />
<#include "/com.tle.web.sections.standard@/checklist.ftl"/>
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<@settingContainer mandatory=false wide=true>
	<@setting label=b.key('tier.showlist.label.pricesbasedon') rowStyle="pricesbasedon" labelFor=s.flatRate>
		<@render s.flatRate />
	</@setting>
</@settingContainer>