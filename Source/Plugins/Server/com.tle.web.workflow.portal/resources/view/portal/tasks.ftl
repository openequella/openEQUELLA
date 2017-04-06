<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.equella@/styles/altlinks.ftl">

<@css "tasks.css"/>
<div class="alt-links">
	<#list m.tasks as search>
		<#assign folder="folder">
		<#if search.secondLevel>
			<#assign folder="level2 document">
		</#if>
		<@render section=search.link class="${altclass(search_index)} ${folder}">${search.label} (${search.count})</@render>
	</#list>
</div>