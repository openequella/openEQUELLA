<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.equella@/renderer/linklist.ftl"/>

<div id="query-header" class="area">
	<h2>${s.headerTitle}</h2>
	<@linklist id="searchresults-select" section=s.favouriteType/>
</div>

