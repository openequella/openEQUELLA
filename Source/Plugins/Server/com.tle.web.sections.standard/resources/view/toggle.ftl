<#include "/com.tle.web.freemarker@/macro/sections.ftl" />

<#macro toggle section checkedText="" uncheckedText="" style="" class="" id=""><#t/>
	<#local renderer=_choose(section, "toggle")><#t/>
	<#if renderer?has_content><#t>
	${renderer.setCheckedText(checkedText)}<#t/>
	${renderer.setUncheckedText(uncheckedText)}<#t/>
	<@_render section=renderer style=style class=class id=id><#nested></@_render>
	</#if><#t>
</#macro>
