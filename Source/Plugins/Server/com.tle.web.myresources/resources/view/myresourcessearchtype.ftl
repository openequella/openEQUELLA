<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.equella@/renderer/linklist.ftl"/>

<div id="query-header" class="area">
	<h2>${s.headerTitle}</h2>
	<@linklist id="searchresults-select" section=s.searchType/>
	<@render s.resetFiltersSection/>
	<#if m.queryActions??>
		<div class="queryactions">
			<#list m.queryActions as action>
				<div class="query-action"><@render action /></div>
			</#list>			
		</div>
	</#if>
</div>