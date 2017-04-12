<#macro autocomplete section id="" style="" class="" autoSubmitButton="" placeholder=""><#t/>
	<#local renderer=_choose(section, "autocompletetextfield")><#t/>
	<#if renderer?has_content><#t>
	${renderer.setAutoSubmitButton(autoSubmitButton)}<#t/>
	${renderer.setPlaceholderText(placeholder)}<#t/>
	<@_render section=renderer id=id class=class style=style><#nested></@_render>
	</#if><#t>
</#macro>
