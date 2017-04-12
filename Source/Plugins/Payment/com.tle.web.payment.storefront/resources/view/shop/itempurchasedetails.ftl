<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/radio.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl" />
<#include "/com.tle.web.sections.standard@/checklist.ftl"/>
<#include "/com.tle.web.sections.standard@/calendar.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>
<#include "/com.tle.web.sections.standard@/list.ftl" />
<#import "/com.tle.web.sections.equella@/macro/message.ftl" as message/>

<#if m.totalMoneyLabel??>
 
<h3><@bundlekey "shop.viewitem.pricing.title" /></h3>

<div class="pricing">

	<#if m.otherPurchasers>
		<div class="purchaseoptiondiv">
			<@message.info>
				${m.purchasedWarning} <#if m.purchasedOutrightFlatRate>${m.dateRenderable}</#if>
			</@message.info>
			
			<#if !m.purchasedOutrightFlatRate>
				<@render s.purchasedBeforeTable />
			</#if>
		</div>
	</#if>
	
	<#if m.errorMap['cart.error']?? >
		<@message.error>
			${m.errorMap['cart.error']}
		</@message.error>
	</#if>	
	
		<@boollist section=s.pricingType; opt, state>
			<#if m.multiplePricingOptions>
				<div class="purchaseoption input radio"><@render state /></div> 
			<#else>
				<div class="purchaseoption input radio">${state.label}</div>
			</#if>
			
			<#if opt.value == "purchase">
				<@a.div id="purchase_div" class="purchaseoptiondiv indent">
					<p>
						${m.purchaseMoneyLabel}
						<#if !m.shoppingCartAccessible && m.purchasePerUser>
							<@bundlekey "shop.viewitem.pricing.perUser" />
						</#if>
					</p>	
					
					<#if m.shoppingCartAccessible && m.purchasePerUser>
						<#if m.errorMap['invalid.number.out']?? >
							<@message.error label="${m.errorMap['invalid.number.out']}" />
						</#if>	
						<p>
							<label for="${s.numberOfUsersPurchase}">
								<@bundlekey "shop.viewitem.pricing.subscribe.users" /> 
							</label>	
							<@render section=s.numberOfUsersPurchase class="numusers" />
						</p>
					</#if>
				</@a.div>

			<#elseif opt.value == "subscribe">
				<@a.div id="subscription_div" class="purchaseoptiondiv indent">
					<#if m.shoppingCartAccessible && m.subscribePerUser>
						<#if m.errorMap['invalid.number.sub']?? >
							<@message.error label="${m.errorMap['invalid.number.sub']}" />
						</#if>	
						<p>
							<label for="${s.numberOfUsersSubscribe}">
								<@bundlekey "shop.viewitem.pricing.subscribe.users" />
							</label>
							<@render section=s.numberOfUsersSubscribe class="numusers" />
						</p>
					</#if>
					
					<#if m.shoppingCartAccessible>
						<p class="morespaces"><@bundlekey "shop.viewitem.pricing.subscribe.label.duration" /></p>
					</#if>
					
					<#if m.errorMap['no.duration']??>
						<@message.error label="${m.errorMap['no.duration']}" />
					</#if>
					<@render s.pricingTable />					
					
					<#if m.shoppingCartAccessible>
							<@a.div id="show_calendar">
								<p class="morespaces"><@bundlekey "shop.viewitem.pricing.subscribe.date" /></p>
								<#if m.errorMap['no.date']??>
									<@message.error label="${m.errorMap['no.date']}"/>
								</#if>
								
								<@boollist section=s.startDate; opt, state>
								    <#if opt.value =="otherdate">
										<div class="startdateoption calendar">
											<div class="input radio"><@render state /></div>
											<div class="calendarwrap"><@calendar section=s.otherDate /></div>
										</div>
									<#else>
										<div class="startdateoption">
											<div class="input radio"><@render state /></div>
										</div>
									</#if>
								</@boollist>
							</@a.div>
						</#if>	
				</@a.div>	
			</#if>			
		</@boollist>
		
	
	<#if !m.freeItem>
		<hr>
	</#if>
	
	<#if m.shoppingCartAccessible>
		<#if !m.freeItem>
			<@a.div id="updateTotal" class="total">
				<span>${m.totalMoneyLabel}</span>
			</@a.div>
		</#if>
		
		<div class="bottom">
			<#if m.inCart>
				<@button section=s.removeFromCartButton showAs="delete" />
			<#else>
				<@button section=s.addToCartButton showAs="save" icon="cart" />
			</#if>
		</div>
	<#else>
		<br>
	</#if>
	
</div>
</#if>