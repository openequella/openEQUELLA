<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.freemarker@/macro/table.ftl" />
<#import "/com.tle.web.sections.standard@/ajax.ftl" as ajax/>

<div class="area">
	<h2>${m.pageTitle}</h2>

	<@ajax.div id="connectors">
		<@render s.connectorsTable />
	</@ajax.div>
</div>