<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>

<div id="cloudy">
	<@render s.countSpan><#if s.active>${b.key('resultstab.active.title')}<#else>${b.key('resultstab.inactive.blanksearch.title')}</#if></@render> 
</div>