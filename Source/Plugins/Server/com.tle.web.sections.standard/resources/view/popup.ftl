<#macro popup section width height target="" style="" class="" id=""><#t>
	<#local renderer=_choose(section, "popup")><#t>
	<#if renderer?has_content><#t>
	${renderer.setWidth(width)}<#t>
	${renderer.setHeight(height)}<#t>
	${renderer.setTarget(target)}<#t>
	<@_render section=renderer id=id class=class style=style><#nested></@_render>
	</#if><#t>
</#macro>
