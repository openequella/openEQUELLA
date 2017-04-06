<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<@css path="edittier.css" hasRtl=true />

<#-- row just for general price validation errors -->
<@setting label='' error=m.errors['price.subscription.general'] />

<#list m.periods as row>
	<@setting label=row.label rowStyle="editperiodprice" error=m.errors[row.errorKey] >
		<#if !m.purchase>
			<div class="enableprice input checkbox">
				<@render section=row.enabled />
			</div>
		</#if>
		<@render section=row.value/> 
	</@setting>
</#list>
<@setting label=b.key('tier.enabled') rowStyle="enabledRow" labelFor=s.enabled>
		<@render s.enabled />
</@setting>