<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />

<@css path="shop/catalogues.css" hasRtl=true />

<div class="area">
	<@render section=s.backLink class="return-link" />
	
	<div class="storeTitle"><@render m.icon /><h2>${m.storeName}</h2></div>
	
	<p class="text">${b.key('shop.browse.description')}</p>
	
	<h2>${b.key('shop.browse.catalogues')}</h2>
	<div id="catalogue_list" class="indent">
		<ul>
			<#if m.catalogues?size gt 0>
				<#list m.catalogues as catalogue>
				<li>
					<@render catalogue.link>${catalogue.name} ( ${catalogue.count} )</@render>
					<br>
					<#if catalogue.description??>
						<span>${catalogue.description}</span>
					</#if>
				</li>
				</#list>
			<#else>
				<li>${b.key('shop.browse.catalogues.none')}</li>
			</#if>
			
		</ul>
	</div>
</div>