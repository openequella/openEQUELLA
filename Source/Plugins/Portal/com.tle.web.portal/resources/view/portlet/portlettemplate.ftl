<#ftl />
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>


<@render section=s.box class="portlet_${m.type}_content ${m.style}">
	<#if m.content??><@render m.content /></#if>
</@render>
