<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a>

<@css path="storefront/editstorefront.css" hasRtl=true />

<@setting label=b.key('storefront.edit.transactionsallowed')
	error=m.errors['minOnePaymentType']>
	
	<div class="ttfree">
		<@render s.allowFree />
	</div>

	<div class="ttpurchase">
		<@render s.allowPurchase />
	</div>
	
	<div class="ttsubscription">
		<@render s.allowSubscription />
	</div>
</@setting>

<@setting label=b.key('storefront.edit.taxtype')>
	<@render s.taxType />
</@setting>

<h2 class="subtitle">${b.key('storefront.edit.registrationdetails.title')}</h2>

<@setting label=b.key('storefront.edit.productname') 
	section=s.productName
	mandatory=true
	error=m.errors['productName'] />

<@setting label=b.key('storefront.edit.productversion') 
	section=s.productVersion
	mandatory=true
	error=m.errors['productVersion'] />

<@setting label=b.key('storefront.edit.country') 
	section=s.country
	mandatory=true
	error=m.errors['country'] />
	
<@setting label=b.key('storefront.edit.contactnumber') 
	section=s.contactNumber />

<h2 class="subtitle">${b.key('storefront.edit.oauthdetails.title')}</h2>

<@setting label=b.key('storefront.edit.clientid')
	mandatory=true  
	section=s.clientId
	error=m.errors['clientId'] />

<@setting label=b.key('storefront.edit.redirecturl')
	mandatory=true  
	section=s.redirectUrl
	error=m.errors['redirectUrl']
	help=b.key('storefront.edit.label.help.redirecturl') />

<@a.div id="userAjaxDiv">
<@setting label=b.key('storefront.edit.user')
	mandatory=true  
	error=m.errors['user'] >
		<#if m.user??>
			<@render m.user />
		</#if>
		<@render s.selectUserButton />
</@setting>
</@a.div>

<@setting label=b.key('storefront.edit.enabled') rowStyle="enabledRow" labelFor=s.enabled>
	<@render s.enabled />
</@setting>
