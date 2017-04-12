<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>

<@css "itemdetails.css" />
<div class="in-selection">
	<#if m.itemDetails??>
		<@render m.itemDetails />
	</#if>
	
	<#if m.minorActions??>
		<div class="action_list">
			<@render m.minorActions />
		</div>
	</#if>
	<#if m.lockSection??>
		<@render m.lockSection />
	</#if>
</div>