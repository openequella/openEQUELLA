<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl" />
<@css "changetier.css" />

<#if !s.forBulk>	
<h2>${b.key('viewitem.tier.select.title')}</h2>
<#else>
<h3>${b.key('viewitem.tier.select.title')}</h3>
</#if>

<@settingContainer mandatory=false>
	<#if m.showFree>
		<@setting label=b.key('viewitem.tier.free') section=s.freeBox help=b.key('viewitem.tier.free.help') />
	</#if>
	<#if m.showPurchase>
		<@setting label=b.key('viewitem.tier.purchase') section=s.purchaseTierList help=b.key('viewitem.tier.purchase.help') />
	</#if>
	<#if m.showSubscription>
		<@setting label=b.key('viewitem.tier.subscrition') help=b.key('viewitem.tier.subscrition.help') />
		<div class="table-container">
			<@render s.subscriptionTierTable />
		</div>
	</#if>
</@settingContainer>

<#if !s.forBulk>	
	<div class="button-strip">
		<@button section=s.saveButton showAs="save" />
	</div>
</#if>