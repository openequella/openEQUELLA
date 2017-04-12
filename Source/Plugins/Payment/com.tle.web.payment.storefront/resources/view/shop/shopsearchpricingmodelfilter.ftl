<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/checklist.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<#if !m.hideSection>
	<@div class="filter" id=s.ajaxDiv>		
		<label for="price">
			<h3>${b.gkey("com.tle.web.payment.storefront.store.price.filter.subtitle")}</h3>
		</label>
		<div class="input">
			<div class="input select">
				<@render s.filterOptions />
			</div>
		</div>
		<@div id="price-clear">
			<#if m.showClearButton>
				<@button section=s.clearButton class="clear-filter" showAs="delete">${b.key("store.price.filter.clear")}</@button>
			</#if>
		</@div>
	</@div>
</#if>