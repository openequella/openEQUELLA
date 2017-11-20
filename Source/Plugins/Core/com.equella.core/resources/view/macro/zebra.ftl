<#ftl strip_whitespace=true />
<#macro zebra section id="" style="" class="" wrap=false filter=-1><#t/>
	<#local renderer=_choose(section, "zebra")><#t/>
	<#if renderer?has_content><#t>
	${renderer.setFilterThreshold(filter)}<#t/>
	${renderer.setWrap(wrap)}<#t/>
	<@_render section=renderer style=style class=class id=id></@_render><#t/>
	</#if><#t>
</#macro>