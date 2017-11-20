<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.equella@/styles/altlinks.ftl">

<@css "myresources.css"/>
<div class="alt-links">
	<#list m.searches as search>
		<#assign folder="folder-full">
		<#if search.child>
			<#assign folder="level2 document">
		<#elseif search.hasKids>
			<#assign folder="folder">
		</#if>
		<@render section=search.link class="${altclass(search_index)} ${folder}">${search.label} (${search.count})</@render>
	</#list>
</div>