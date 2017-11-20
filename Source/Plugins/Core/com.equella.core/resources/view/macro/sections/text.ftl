<#ftl strip_whitespace=true strip_text=true />

<#macro wrap maxlength maxwords=-1>
	<#local nested><#nested></#local><#t/>
	${t.wrap(nested, maxlength, maxwords)}<#t/>
</#macro>

<#macro highlight words=[] class="highlight">
	<#local nested><#nested></#local><#t/>
	<#if words?size gt 0><#t/>
		${t.highlight(nested, words)}<#t/>
	<#else><#t/>
		${nested}<#t/>
	</#if><#t/>
</#macro>

<#macro nl2br>
	<#local content>
		<#nested><#t>
	</#local>
	${content?replace("\n", "<br>")}
</#macro> 