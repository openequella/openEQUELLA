<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a>

<@css "showtiers.css" />

<div class="area">

	<h2>${b.key('tier.showlist.page.title')}</h2>
	
	<@a.div id="topt">
		<@settingContainer mandatory=false wide=true>
		
			<@setting label=b.key('tier.showlist.label.selectcurrency') rowStyle="currency" error=m.errors['currency'] labelFor=s.currency>
				<@render section=s.currency />
			</@setting>
			
			<@setting label=b.key('tier.showlist.label.selectpricingmodels')
				help=b.key('tier.showlist.pricingmodels.option.free.help')>
				<div class="input checkbox">
					<@render s.free />
				</div>
			</@setting>
			
			<@setting label=''
				help=b.key('tier.showlist.pricingmodels.option.purchase.help')>
				<div class="input checkbox">
					<@render s.purchase />
				</div>
			</@setting>
				
			<@setting label='' 
				help=b.key('tier.showlist.pricingmodels.option.subscription.help')>
				<div class="input checkbox">
					<@render s.subscription />
				</div>
			</@setting>
			
		</@settingContainer>
	</@a.div>
	
	<@render m.renderable />

</div>