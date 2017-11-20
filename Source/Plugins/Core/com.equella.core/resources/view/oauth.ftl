<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.freemarker@/macro/table.ftl" />
<#import "/com.tle.web.sections.standard@/ajax.ftl" as ajax/>

<@css "oauth.css" />

<div class="area">
	<h2>${m.pageTitle}</h2>
	
	<#if m.showClients>
		<@ajax.div id="clientsDiv">
			<h3>${b.key('oauth.clients.label.heading')}</h3>
			<@render s.clientTable />
			<#if m.inUseError??>
				<p class="oauth_error">${m.inUseError}</p>
			</#if>
		</@ajax.div>	
	</#if>
	
	<#if m.showTokens>
		<h3>${b.key('oauth.tokens.label.heading')}</h3>
		
		<@ajax.div id="tokensDiv">
			<@render s.tokenTable />
		</@ajax.div>
	</#if>
</div>