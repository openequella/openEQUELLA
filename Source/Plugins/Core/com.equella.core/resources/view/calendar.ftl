<#macro calendar section style="" class="" notBefore="" notAfter="" id=""><#t/>
	<#local renderer=_choose(section, "calendar")><#t/>
	<#if renderer?has_content><#t>
	${renderer.setNotAfter(notAfter)}<#t/>
	${renderer.setNotBefore(notBefore)}<#t/>
	<@_render section=renderer style=style class=class id=id><#nested></@_render>
	</#if><#t>
</#macro>