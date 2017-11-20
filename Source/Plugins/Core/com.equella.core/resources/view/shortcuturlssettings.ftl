<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.freemarker@/macro/table.ftl" />

<div class="area">
	<h2>${b.key("shortcuts.page.title")}</h2>
	<p>${b.key('page.description', m.baseUrl)}</p>
	
	<div id="shortcuturls">
		<@render s.shortcutsTable />
	</div>
</div>
