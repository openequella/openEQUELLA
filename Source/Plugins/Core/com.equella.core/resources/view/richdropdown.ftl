<#macro richdropdown section id="" style="" class="" ><#t/>
	<#local renderer=_choose(section, "richdropdown")><#t/>
	<#if renderer?has_content><#t>
	<@_render section=renderer id=id class=class style=style><#nested></@_render>
	</#if><#t>
</#macro>