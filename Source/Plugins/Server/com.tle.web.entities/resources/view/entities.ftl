<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.freemarker@/macro/table.ftl" />
<#import "/com.tle.web.sections.standard@/ajax.ftl" as ajax/>

<@css path="entities.css" hasRtl=true />

<#if !s.noArea && m.displayed> 
<div class="area">
</#if>

	<@ajax.div id=s.ajaxId class="entitylist">
		<#if m.displayed>
			<h2>${m.pageTitle}</h2>

			<#if m.top??>
				<@render m.top />
			</#if>
			
			<@render s.entTable />
			
			<#if m.bottom??>
				<@render m.bottom />
			</#if>
		</#if>
	</@ajax.div>
	
<#if !s.noArea && m.displayed>
</div>
</#if>