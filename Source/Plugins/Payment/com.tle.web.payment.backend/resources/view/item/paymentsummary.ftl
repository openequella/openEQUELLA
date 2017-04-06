<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />

<@css "paymentsummary.css" />

<div class="pricing">
	<h3>${b.key('viewitem.summary.prices.title')}</h3>
	
	<div class="tiers">
		<#if m.free || m.purchase || m.subscription>
		
			<#if m.free>
				<h4>${b.key('viewitem.summary.prices.free')}</h4>
			</#if>
			
			<#if m.purchase>
				<h4>${b.key('viewitem.summary.prices.purchase')}</h4>
				<@render s.purchaseTierTable />
			</#if>
			
			<#if m.subscription>
				<h4>${b.key('viewitem.summary.prices.subscription')}</h4>
				<@render s.subscriptionTierTable />
			</#if>
	
		<#else>
			<div>${b.key('viewitem.summary.prices.notiers')}</div>
		</#if>
	</div>
		
	
	<h3>${b.key('viewitem.summary.catalogues.title')}</h3>
	<div class="catalogues">
		<@render s.cataloguesTable />
	</div>
</div>