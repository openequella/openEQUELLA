<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<@css "error.css" />

<#assign TEMP_body>
	<div class="area error">
		<h2>${b.gkey(m.titleKey)}</h2>
	
		<#if m.parts??>
		<#list m.parts as part>
			<#if part??>
			<#if part.title??>
				<h3>${part.title}</h3>
			</#if>
			
			<#if part.text??>
				<p id="${part.id}">${part.text?html}</p>
			</#if>
			</#if>
		</#list>
		</#if>
	
		<h3>${b.key("errors.part.actions.title")}</h3>
		<p><@render s.action /></p>
	</div>
	
	<#if m.children??>
	<div class="children">		
		<@render section=m.children />
	</div>
	</#if>
</#assign>