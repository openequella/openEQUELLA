<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />

<div class="area">
	<h2>${b.key('htmledit.settings.title')}</h2>
	
	<@render section=s.settingsTable class="large" />
</div>