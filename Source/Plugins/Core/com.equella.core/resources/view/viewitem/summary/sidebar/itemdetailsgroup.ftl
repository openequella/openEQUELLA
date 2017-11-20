<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>

<h3>${b.key("summary.sidebar.itemdetailsgroup.title")}</h3>
<@div id="adjacentuls" class="itemdetails">
	<ul class="itemdetails-left">
		<#if m.ownerLink?? >
			<li>${b.key("summary.sidebar.itemdetailsgroup.owner")}: <@render m.ownerLink /></li>
		</#if>
		<#if m.collaboratorLinks?? >
			<li>${b.key("summary.sidebar.itemdetailsgroup.collaborators")}: <@renderList list=m.collaboratorLinks separator=", " /></li>
		</#if>
		<li>${b.key("summary.sidebar.itemdetailsgroup.collection")}: <@render m.collectionLink /></li>
		<li>${b.key("summary.sidebar.itemdetailsgroup.version")}: ${m.version} (<@render s.showVersionsLink/>) </li>
		<li>${b.key("summary.sidebar.itemdetailsgroup.status")}: ${m.status}</li>
	</ul>
	<#if m.sections?size gt 0 >
		<@renderAsHtmlList list=m.sections class="itemdetails-right" />
	</#if>
	<div class="prop"></div>
</@div>