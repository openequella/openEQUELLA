<#macro textarea section id="" style="" class="" rows=0 cols=0 spellcheck=false><#t>
	<#local renderer=_choose(section, "textarea")><#t>
	<#if renderer?has_content><#t>
	${renderer.setRows(rows)}<#t>
	${renderer.setCols(cols)}<#t>
	${renderer.setSpellCheck(spellcheck)}<#t>
	<@_render section=renderer style=style class=class id=id><#nested></@_render>
	</#if><#t>
</#macro>
