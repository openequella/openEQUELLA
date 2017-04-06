<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />

<@css "shop/stores.css" />

<div class="area">
<h2>${b.key('shop.stores.title')}</h2>
<#list m.stores as store>
	<div class="store">
		<#if store.errored>
			<h3>${store.title}</h3>
			<div class="errored">${store.description}</div>
		<#else>
			<@render store.link>${store.icon}</@render>
			<div>${store.description}</div>
		</#if>
	</div>
</#list>
</div>
