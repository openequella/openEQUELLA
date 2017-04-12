<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/link.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<@a.div id="searchform-filteredout">
	<#if m.filteredOutLabel??>
		<div class="searchresults-infobar">${m.filteredOutLabel} <@link s.resetButton/></div>
	</#if>
</@a.div>
