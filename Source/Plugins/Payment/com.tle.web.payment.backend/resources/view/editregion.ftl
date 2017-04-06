<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/shufflebox.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl" />

<@setting label=b.key('region.preset.initial') help=b.key('region.preset.initial.help') section=s.prepopulateList />

<@setting label=b.key('region.select') help=b.key('region.select.help')>
	<@div id="countries">
		<@shufflebox section=s.countryList />
	</@div>
</@setting>