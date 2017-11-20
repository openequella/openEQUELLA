<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/textfield.ftl">
<#include "/com.tle.web.sections.equella@/component/button.ftl">

<@css path="oauthlogon.css" hasRtl=true/>

<@render m.containerDiv>
	<div id="oauth_topbar">
		<div id="oauth_topbarcontent">
			<div id="oauth_title">${b.key('logon.titlebar')}</div>
		</div>
	</div>
	<div id="oauth_wrap">
		<div id="oauth_main">
			<div id="oauth_content">
				<h2>${b.key('logon.clientrequest', [m.clientName])}</h2>
				
				<#if m.authError??>
					<p class="oauth_error">${m.authError}</p>
				</#if>
				
				<#if !m.alreadyLoggedIn> 
					<#if m.fixedUsername??>
						<div class="usernamepwd"><@render s.username.labelRenderer/><div class="fixedusername">${m.fixedUsername}</div></div>
					<#else>
						<div class="usernamepwd"><@render s.username.labelRenderer/><@textfield section=s.username autoSubmitButton=s.authButton nolabel=true/></div>
					</#if>
					<div class="usernamepwd"><@render s.password.labelRenderer/><@textfield password=true section=s.password autoSubmitButton=s.authButton nolabel=true/></div>
				<#else>
					<#if m.cannotUse>
						<div class="cannotuseclient">${b.key('logon.client.cannotuse', [m.username, m.clientName])}</div>
					<#else>
						<div class="alreadyloggedon">${b.key('logon.already', [m.username])}</div>
					</#if>
				</#if>
				
				<div class="buttons">
					<#if m.cannotUse>
						<@button class="signin" section=s.logoutButton />
					<#elseif m.alreadyLoggedIn>
						<@button class="signin" section=s.allowButton showAs="accept"/>
					<#else>
						<@button class="signin" section=s.authButton showAs="accept"/>
					</#if>
					<@button class="cancel" section=s.denyButton showAs="reject" />
				</div>
			</div>
		</div>
	</div>
</@render>