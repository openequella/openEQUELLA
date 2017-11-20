<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/zebra.ftl">

<div class="area">
	<h2>${b.key("title")}</h2>
	<@render section=s.settingsTable class="large" />
</div>