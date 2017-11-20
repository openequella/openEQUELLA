<#macro button section id="" style="" class=""><#t/>
	<#local renderer=_choose(section, "button")><#t/>
	<#if renderer?has_content><#t>
	<@_render section=renderer style=style class=class id=id><#nested></@_render>
	</#if><#t>
</#macro>
