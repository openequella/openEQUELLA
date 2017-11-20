<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>

<div class="area error">
	<h2><@bundlekey "error.title"/></h2>
	
	<h3><@bundlekey "error.label.reason"/></h3>
	<p>${m.reason!""?html}</p>
	
	<#if m.showErrorHelp>
		<h3><@bundlekey "error.title.help"/></h3>
		<p><@bundlekey "error.message.help"/></p>
	</#if>
</div>