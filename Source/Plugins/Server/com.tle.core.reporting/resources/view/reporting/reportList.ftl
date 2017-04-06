<#include "/com.tle.web.freemarker@/macro/sections.ftl" />

<div class="area">
	<#if !m.hasReports()>
		<h2>${b.key("list.noreports")}</h2>
	<#else>
		<h2>${b.key("list.select")}</h2>
		<@render section=s.reportTable class="large" />
	</#if>
</div>
