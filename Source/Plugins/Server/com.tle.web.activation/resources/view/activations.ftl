<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<@css "activations.css"/>
<@css path="selectionreview.css" plugin="com.tle.web.sections.equella"/>

<div id="activations-list">
	<h2>${b.key("activations.title")}</h2>
	<@button section=s.backButton showAs="prev" />	

	<#if m.activations>
		<@render section=s.activationsTable />
	<#else>
		<p>${b.key('activations.noactivations')}</p>
	</#if>
</div>