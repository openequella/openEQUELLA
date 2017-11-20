<#macro link section style="" class="" id=""><#t>
	<#local renderer=_choose(section, "link")><#t>
	<#if renderer?has_content><#t>
	<@_render section=renderer style=style class=class id=id><#nested></@_render>
	</#if><#t>
</#macro>
