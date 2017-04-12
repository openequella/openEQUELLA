<#macro checklist section style="" class="" list=false showBulkOps=false id=""><#t/>
	<#local renderer=_choose(section, "checklist")><#t/>
	<#if renderer?has_content><#t>
	${renderer.setAsList(list)}<#t/>
	${renderer.setShowBulkOps(showBulkOps)}<#t/>
	<@_render section=renderer style=style class=class id=id><#nested></@_render>
	</#if><#t>
</#macro>
