<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<div class="area">
	<h2>${b.key("version.checklatest")}</h2>
	<@a.div id="latestVersion">
	    <#if m.onLatestVersion>
            ${b.key("version.latest")}
        <#elseif m.versionInfoUrl??>
            ${b.key("version.new", [m.versionInfoUrl])}
        <#else>
    	    ${b.key("version.checking")}
	    </#if>
	</@a.div>
    <br>
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