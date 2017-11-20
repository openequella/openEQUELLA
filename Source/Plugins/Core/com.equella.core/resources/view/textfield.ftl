<#macro textfield section id="" style="" class="" password=false hidden=false size=0 maxlength=0 nolabel=false autoSubmitButton="" placeholder=""><#t/>
	<#local renderer=_choose(section, "textfield")><#t/>
	<#if renderer?has_content><#t>
	${renderer.setSize(size)}<#t/>
	${renderer.setMaxLength(maxlength)}<#t/>
	${renderer.setPassword(password)} <#t/>
	${renderer.setHidden(hidden)} <#t/>
	${renderer.setAutoSubmitButton(autoSubmitButton)}<#t/>
	${renderer.setNoLabel(nolabel)}<#t/>
	${renderer.setPlaceholderText(placeholder)}<#t/>
	<@_render section=renderer id=id class=class style=style><#nested></@_render>
	</#if><#t>
</#macro>
