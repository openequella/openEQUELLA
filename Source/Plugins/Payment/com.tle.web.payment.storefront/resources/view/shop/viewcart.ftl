<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/textarea.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#import "/com.tle.web.sections.equella@/macro/message.ftl" as message/>

<@css path="shop/viewcart.css" hasRtl=true />

<div class="area order">
	<h2>${m.heading}</h2>
	<div class="orderinner">
				
	<#if m.empty>
		<div class="cartTotal">
			<div class="float-left">${b.key('shop.viewcart.label.empty')}</div> 
			<div> ${m.totalLabel}</div>
		</div>
	<#else>
	
		<#if m.totalLabel??>
			<div class="cartTotal topTotal">
				<div>${m.totalLabel}</div>
			</div>
			<#list m.totalTax as tax>
				<div class="cartTotal">
					<div class="totalTax">${tax.modifier} ${tax.amount} ${tax.code}</div>
				</div>
			</#list>
		</#if>
		
		<#if m.errorMessage??>
			<@message.error>
				${m.errorMessage}
			</@message.error>
		</#if>
		
		<#list m.stores as store>
			<div class="storeSection">
			
				<div class="storeTitle">
					<#if !store.errored>
						<@render store.icon />
					</#if>
					<h3>${store.title}</h3>
				</div>
					
				<#if !store.errored>
					<@render store.table />
					
					<div class="tableActions">
						<#if store.status??>
							<div class="shopStatus">
								<h3>${b.key('shop.viewcart.label.storestatus')} <strong>${store.status}</strong></h3>
							</div>
						</#if>
						
						<#if !store.readOnly>	
							<@render section=store.removeAll class="removeAll" />
						</#if>
					</div>
					
					<#if m.stores?size gt 1> 
						<div class="shopTotal">
							<div>${store.total}</div>
						</div>
						<#if store.tax??>
							<#assign tax=store.tax /> 
							<div class="shopTotal">
								<div class="shopTax">${tax.modifier} ${tax.amount} ${tax.code}</div>
							</div>
						</#if>
					</#if>
					
					<#if store.gateways?size gt 0>
						<div class="shopGateways">
							<#list store.gateways as gateway>
								<div>
									<#if gateway.checkoutImage??>
										<@render gateway.checkoutLink>
											<@render gateway.checkoutImage />
										</@render>
									<#else>
										<@button section=gateway.checkoutLink showAs="download"/>									
									</#if>
								</div> 
							</#list>
						</div>
					</#if>
				<#else>
					<@message.error>
						${b.key('shop.viewcart.error.connection')}
					</@message.error>
				</#if>				
			</div>
		</#list>
	
		<#if m.totalLabel??>
			<div class="cartTotal bottomTotal">
				<div>${m.totalLabel}</div>
			</div> 
			
			<#list m.totalTax as tax>
				<div class="cartTotal bottomTotal">
					<div class="totalTax">${tax.modifier} ${tax.amount} ${tax.code}</div>
				</div>
			</#list>
		</#if>
		
		<#if m.submitting || m.approving || m.rejecting>
			<div class="orderhistory-add">
				<label for="${s.comment}">
					<h3>${b.key('shop.viewcart.label.addcomments')}</h3>
				</label>
				<div class="input textarea">
					<@textarea section=s.comment />
				</div>
				
				<div class="orderhistory-action">
					<#if m.submitting>				
						<@button section=s.submitButton class="submit" showAs="save" />
					</#if>
					<#if m.approving>				
						<@button section=s.approveButton class="submit" showAs="accept" />
					</#if>
					
					<#-- Note: reject appears to the left of submit -->
					<#if m.rejecting>
						<@button section=s.rejectButton class="reject" showAs="reject" />
					</#if>
					
					<#if m.redrafting>
						<@button section=s.redraftButton class="redraft" showAs="edit" />
						<@button section=s.deleteButton class="redraft" showAs="delete" />
					</#if>
				</div>
			</div>
		</#if>
	
		<#if m.history?size gt 0>
			<div class="orderhistories">
				<h3>${b.key('shop.viewcart.label.history')}</h3>
				<#list m.history as comment>
					<div class="orderhistory ${comment.reason}">
						<#assign stepdesc=b.key('shop.viewcart.label.history.type.' + comment.reason) />
						<div class="orderhistory-username" title="${stepdesc}">
							<@render comment.userLink /> 
							<span class="orderhistory-description">${stepdesc}</span>
							<span class="orderhistory-date">${comment.date}</span>
						</div>
						
						<div class="orderhistory-content">
							<#if comment.comment??>
								<p>${comment.comment?html?replace("\n", "<br>")}</p>
							</#if>
						</div>
					</div>
				</#list>
			</div>
		</#if>
	</#if>

	</div>
</div>

