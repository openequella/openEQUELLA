<#ftl strip_whitespace=true strip_text=true />

<#macro visinput id type value 
	style=unspec 
	class=unspec 
	maxlength=-1 
	size=-1 
	name=id 
	onClick=unspec
	onKeyDown=unspec
	onFocus=unspec
	onBlur=unspec
	extra=""
>
	<input id="${id}" name="${name}" type="${type}" value="${value?html}"<#rt/>
 <#if maxlength != -1>maxlength=${maxlength?c}</#if><#rt/>
 <#if size != -1>size=${size?c}</#if><#rt/>
 <#if style != unspec>style="${style}"</#if><#rt/> 
 <#if class != unspec>class="${class}"</#if><#rt/>
 <#if onClick != unspec>onClick="${onClick}"</#if><#rt/>
 <#if onKeyDown != unspec>onkeydown="${onKeyDown}"</#if><#rt/>
 <#if onFocus != unspec>onfocus="${onFocus}"</#if><#rt/>
 <#if onBlur != unspec>onblur="${onBlur}"</#if> ${extra} ><#rt/>
</#macro>