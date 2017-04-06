<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<div id="institution-admin" class="area">
	<h2>${b.key("clusternodes.health")}</h2>
	<#if m.cluster>
		<p>${b.key("clusternodes.ids")}</p>
	</#if>
		<@a.div id='table-ajax'>
			<@render s.clusterNodesTable />
		</@a.div>
	<#if m.cluster>
		<div class="input checkbox">
			<@render s.debugCheck />
		</div>
	</#if>
	
	<br>
	
	<h2>${b.key("clusternodes.tasks.title")}</h2>
	<@render s.tasksTable />
	
	<br>
	<#if m.displayQuotas>
	<h2>${b.key("institutionusage.title")}</h2>
	<@a.div id='usagetable'>
		<@render s.institutionUsageTable />
	</@a.div>
	</#if>
</div>