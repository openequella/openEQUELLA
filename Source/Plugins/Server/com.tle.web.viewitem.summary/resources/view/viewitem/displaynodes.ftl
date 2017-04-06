<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.freemarker@/macro/table.ftl">

<div class="displayNodes">
	<#list m.entries as entry>
		<div class="displayNode<#if entry.fullspan>Full<#else>Half</#if><#if entry.style??> ${entry.style}</#if>">
			<h3>${entry.title}</h3>
			<p><@wrap maxlength=entry.truncateLength maxwords=200><@render entry.value /></@wrap></p>
		</div>
	</#list>
</div>