<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.equella@/component/button.ftl">
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a>

<@css path="selection.css" hasRtl=true />
<@css plugin="com.tle.web.sections.equella" path="selectionreview.css"/>

<@a.div id="checkout-div" class="area">
	<h2>${b.key('checkout.title')}</h2>
	<@render s.versionSelectionSection />

	<div class="float-right">
		<#if !m.cancelDisabled>
			<@button section=s.cancelButton size="medium" />&nbsp;
		</#if>
		<@button section=s.finishButton showAs="save" size="medium" />
	</div>
	<@button section=s.continueButton showAs="prev" size="medium" />	
</@a.div>