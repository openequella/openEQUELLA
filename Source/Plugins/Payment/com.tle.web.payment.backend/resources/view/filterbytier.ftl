<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/ajax.ftl" />

<@div id="tier-filter">
	<div class="filter">
		<#if m.showMembership>
			<h3>${b.key("search.filter.membership")}</h4>
				<div class="input checkbox">
					<@render s.manualBox />
				</div>
				<div class="input checkbox">
					<@render s.autoBox />
				</div>
			<hr>
		</#if>

		<#if m.showFree || m.showSubscription || m.showPurchase>
			<h3>${b.key("search.filter.pricing")}</h4>
			<#if m.showFree>
				<div class="input checkbox">
					<@render s.freeBox />
				</div>
				<hr>
			</#if>
			<#if m.showSubscription>
				<h3>${b.key("search.filter.subscription")}</h3>
				<div class="input select">
					<@render s.subscriptionTierList />
				</div>
				<hr>
			</#if>
			<#if m.showPurchase>
				<h3>${b.key("search.filter.purchase")}</h3>
				<div class="input select">
					<@render s.purchaseTierList />
				</div>
			</#if>
		</#if>
	</div>
</@div>