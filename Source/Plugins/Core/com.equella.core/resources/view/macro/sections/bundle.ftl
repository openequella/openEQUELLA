<#ftl strip_whitespace=true strip_text=true />

<#macro bundlekey value global=false params=[]>
	<#if global><#t/>
		<#if params?size gt 0><#t/>
			${b.gkey(value, params)}<#t/>
		<#else><#t/>
			${b.gkey(value)}<#t/>
		</#if><#t/>
	<#else><#t/>
		<#if params?size gt 0><#t/>
			${b.key(value, params)}<#t/>
		<#else><#t/>
			${b.key(value)}<#t/>
		</#if><#t/>
	</#if><#t/>
</#macro>

<#macro bundlekeyjs value global=false params=[]>
	<#if global><#t/>
		<#if params?size gt 0><#t/>
			${b.gkey(value, params)?js_string}<#t/>
		<#else><#t/>
			${b.gkey(value)?js_string}<#t/>
		</#if><#t/>
	<#else><#t/>
		<#if params?size gt 0><#t/>
			${b.key(value, params)?js_string}<#t/>
		<#else><#t/>
			${b.key(value)?js_string}<#t/>
		</#if><#t/>
	</#if><#t/>
</#macro>

<#macro bundle value=0 default="??no_strings_in_bundle??" html=false><#t/>
	<#local val>${b.bundle(value, default)}</#local>
	<#if !html><#t/>
		<#local val=val?html><#t/>
	</#if><#t/>
	${val}<#t/>
</#macro>
