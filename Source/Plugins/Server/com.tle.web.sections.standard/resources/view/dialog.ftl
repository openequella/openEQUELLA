<#macro dialog section id="" width="" height="" style="" class="" title="" type=""><#t>
	<#local renderer=_choose(section, type)><#t>
	<#if renderer?has_content><#t>
	${renderer.setWidth(width)}<#t>
	${renderer.setHeight(height)}<#t>
	${renderer.setTitle(title)}<#t>
	<@_render section=renderer style=style class=class id=id><#nested></@_render>
	</#if><#t>
</#macro>
