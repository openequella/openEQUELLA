<#macro shufflebox section style="" class="" id="" size=0 
	leftlabel="" 
	rightlabel="" 
	allRightButtonText=">>"
	rightButtonText=">"
	allLeftButtonText="<<"
	leftButtonText="<"
	boxWidth=""><#t>
	<#t>
	<#local renderer=_choose(section, "shufflebox")><#t>
	<#if renderer?has_content><#t>
	${renderer.setSize(size)}<#t>
	${renderer.setLeftLabel(leftlabel)}<#t>
	${renderer.setRightLabel(rightlabel)}<#t>
	${renderer.setAllRightButtonText(allRightButtonText)}<#t>
	${renderer.setRightButtonText(rightButtonText)}<#t>
	${renderer.setAllLeftButtonText(allLeftButtonText)}<#t>
	${renderer.setLeftButtonText(leftButtonText)}<#t>
	${renderer.setBoxWidth(boxWidth)}<#t>
	<@_render section=renderer style=style class=class id=id><#nested></@_render>
	</#if><#t>
</#macro>
