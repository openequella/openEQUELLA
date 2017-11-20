<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<div class="area">
	<h2>${b.key("databases.title")}</h2>
	<#if m.noSchemas>
		<p>${b.key('databases.nodatabases')}</p>
	<#else>
		<@render class="databases" section=s.table/>
		<@a.div id="migratecontainer" class="migratecontainer">
			<@render s.migrateSelectedButton />
		</@a.div>
	</#if>
	<@render section=s.add class="add" />
</div>
