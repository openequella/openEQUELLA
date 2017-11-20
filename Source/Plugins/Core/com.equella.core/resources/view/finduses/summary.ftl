<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>
<#include "/com.tle.web.sections.standard@/link.ftl"/>
<#include "/com.tle.web.sections.standard@/dialog.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<@css "finduses.css" />
<@script "finduses.js" />

<div id="finduses">
	<h2><@bundlekey "finduses.title"/></h2>
	<@button section=s.backButton showAs="prev" />

	<h3>${b.key('finduses.label.searching')}</h3>
	<#if m.singleConnectorName??>	
		<p>${m.singleConnectorName}</p>
	<#else>
		<div class="input select">
			<@render s.connectorsList />
		</div>
	</#if>
	
	<@div id="lms-table-ajax">
		<#if m.error??>
				<p class="error">${m.error}</p>
				
		<#elseif m.authRequired>
		
			<h3>${b.key('finduses.label.authorisationrequired')}</h3>
			<p>${b.key('finduses.text.authorisationrequired')}</p>
			
			<@dialog section=s.authDialog />
			<@button section=s.authDialog.opener size="medium"><@bundlekey "finduses.button.auth" /></@button>
		
		<#elseif m.connector??>
			
			<h3>${b.key('finduses.label.locations')}</h3>
			
			<#if m.results> 
				<@render section=s.usageTable class="detailsTable" />
				
			<#else>
				<#-- no results -->
				<p>${b.key('finduses.noresults')}</p>
			</#if>
			
			<div class="finduseoptions">
				<div class="input checkbox">
					<@render s.showAllVersion />
				</div>
				<div class="input checkbox">
					<@render s.showArchived />
				</div>
			</div>	
		</#if>
	</@div>
</div>