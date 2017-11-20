<#macro pager section style="" class="" currentPageClass="" otherPageClass=""><#t/>
	<#local renderer=_choose(section, "pager")><#t/>
	<#if renderer?has_content><#t>
	${renderer.setCurrentPageCssClass(currentPageClass)}<#t/>
	${renderer.setOtherPageCssClass(otherPageClass)}<#t/>
	<@_render section=renderer style=style class=class id=id/>
	</#if><#t/>
</#macro>