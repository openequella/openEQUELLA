<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>

<@css "licences.css" />

<div class="area">
	<h2>${b.key("licences.title")}</h2>
	<p>${b.key("licences.thirdPartyPreamble")}</p>
	<@render section=s.licenceTable />

</div>
