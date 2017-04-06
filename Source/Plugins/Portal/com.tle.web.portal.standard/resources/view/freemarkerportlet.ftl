<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>

<@css "freemarkerportlet.css"/>

<@div id="${id}freemarkerPortlet" class="freemarkerPortlet">
	<@render section=s.portletDiv>
		<#if m.error??>
			<@render m.error />
		<#else>
			<@render m.markup />
		</#if>
	</@render>
</@div>
