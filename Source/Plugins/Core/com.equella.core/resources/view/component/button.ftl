<#macro button section id="" style="" class="" showAs="" trait="" size="" icon="" iconOnly=false><#t/>
	<#local renderer=_choose(section, "button")><#t/>
	<#if renderer?has_content><#t>
		${renderer.freemarkerShowAs(showAs)}<#t>
		${renderer.freemarkerTrait(trait)}<#t>
		${renderer.freemarkerSize(size)}<#t>
		${renderer.freemarkerIcon(icon)}<#t>
		${renderer.setIconOnly(iconOnly)}<#t>
		<@_render section=renderer style=style class=class id=id><#nested></@_render>
	</#if><#t>
</#macro>
