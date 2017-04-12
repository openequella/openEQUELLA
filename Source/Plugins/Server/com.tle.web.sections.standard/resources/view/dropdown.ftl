<#macro dropdown section style="" class="" size=0 id=""><#t/>
	<#local renderer=_choose(section, "dropdown")><#t/>
	<#if renderer?has_content><#t/>
	${renderer.setSize(size)}<#t/>
	<@_render section=renderer style=style class=class id=id><#nested></@_render>
	</#if><#t/>
</#macro>
