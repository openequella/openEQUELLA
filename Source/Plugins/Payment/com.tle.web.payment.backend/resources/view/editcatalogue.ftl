<#ftl strip_whitespace=true />
<#import "/com.tle.web.sections.standard@/ajax.ftl" as ajax/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<@css path="editcatalogue.css" hasRtl=true />

<@setting label=b.key('catalogue.edit.label.dynamiccollections') labelFor=s.dynamicCollections>
	<@render s.dynamicCollections />
</@setting>

<@render s.manageDiv>
	<@ajax.div id="manage">
		<@setting label=b.key('catalogue.edit.label.selectmanage') 
			help=b.key('catalogue.edit.help.selectmanage') 
			rowStyle="manageRow"
			mandatory=true error=m.errors['manager']>
			<#if m.manageExpressionPretty?? && m.manageExpressionPretty != "">
				${m.manageExpressionPretty}
				<br>
			</#if>
			<@button section=s.manageSelector.opener showAs="select_user">
				<@bundlekey "catalogue.edit.selectmanage.button.change" />
			</@button>
		</@setting>
	</@ajax.div>
</@render>

<@setting label=b.key('catalogue.edit.filterregions') labelFor=s.restrictToRegions>
	<#--<@checklist section=s.regionFiltered class="input radio"/>-->
	<@render s.restrictToRegions />
</@setting>

<@ajax.div id="regions">
	<#if m.displayRegions>
		<@setting label=b.key('catalogue.edit.regions') 
			help=b.key('catalogue.edit.help.regions') 
			rowStyle="regionRow" 
			labelFor = s.regionsList>
			<div class="regionsContainer input checkbox">
				<@render s.regionsList />
			</div>
		</@setting>
	</#if>
</@ajax.div>

<@setting label=s.enabledLabel labelFor=s.enabled >
	<@render s.enabled />
</@setting>