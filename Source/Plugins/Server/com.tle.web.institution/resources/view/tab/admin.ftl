<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<div class="area">
	<h2>${b.key("institutions.admin.heading")}</h2>
	<#if !m.hasInstitutions()>
		<p>${b.key("institutions.admin.noinstitutions")}</p>
	<#else>
		<@render s.institutionsTable />
	</#if>
</div>