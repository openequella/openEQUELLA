<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl" />

<@css "shop/cartbox.css" />

<@render section=s.cartBox class="cartbox">
	<#if m.empty>
		<div class="items">${b.key('shop.cart.label.noitems')}</div>
	<#else>
		<div class="items">${m.thereAreLabel} <@render s.xItemsLink /> ${m.inYourCartLabel}</div>
		
		<div class="totals">
			<div class="carttotal">${b.key('shop.cart.label.carttotal')}</div>
			<#list m.totalLabels as totalLabel><div class="total">${totalLabel}</div></#list>
		</div>
		
		<@button section=s.viewCartButton class="cartbutton" showAs="save" icon="cart" />
	</#if>
</@render>