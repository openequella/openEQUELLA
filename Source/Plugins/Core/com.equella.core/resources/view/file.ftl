<#macro file section style="" class="" size=0 renderBar=true renderFile=true id=""><#t/>
	<#local renderer=_choose(section, "file")><#t/>
	<#if renderer?has_content><#t>
	${renderer.setParts(renderBar, renderFile)}<#t/>
	${renderer.setSize(size)}<#t/>
	<@_render section=renderer style=style class=class id=id><#nested></@_render>
	</#if><#t>
</#macro>