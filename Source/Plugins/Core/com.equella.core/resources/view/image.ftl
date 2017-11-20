<#macro image section src width="" height="" style="" class="" id=""><#t>
	<#local renderer=_choose(section, "image")><#t>
	<#if renderer?has_content><#t>
	${renderer.setSource(src)}<#t>
	<@_render section=renderer style=style class=class id=id><#nested></@_render>
	</#if><#t>
</#macro>
