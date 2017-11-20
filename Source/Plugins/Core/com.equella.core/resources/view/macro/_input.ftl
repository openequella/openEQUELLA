<#ftl strip_whitespace=true strip_text=true />

<#macro input id type value name=id attributes="">
	<input id="${id?html}" name="${name?html}" type="${type}" value="${value?html}" ${attributes}>
</#macro>