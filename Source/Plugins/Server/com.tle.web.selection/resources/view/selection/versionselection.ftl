<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<@css "versionselection.css" />

<div class="versionselection">
	<#if m.tables?size == 0>
		<#if m.parentFolderId??>
			<p>${b.key('checkout.noselections.location')}</p>
		<#else>
			<p>${b.key('checkout.noselections')}</p>
		</#if>
	<#else>
		<#list m.tables as table>
			<h2>${table.first}</h2>
			<@render table.second />
			<br>
		</#list>
	</#if>
</div>