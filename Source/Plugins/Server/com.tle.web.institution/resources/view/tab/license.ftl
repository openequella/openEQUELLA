<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/textarea.ftl">
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<h2>${b.gkey("institutions.license.link.name")}</h2>
<#assign error>
	<#if m.error?? && m.reason??>
		${m.error} - ${m.reason}
	</#if>
</#assign>

<#if m.message??>
	<div class="licenseMessage">${m.message}</div>
</#if>
<p>${b.gkey("institutions.license.contact", ['<p>Your preferred support organization.</p>'])}</p>
<h3>${b.gkey("institutions.license.details")}</h3>

<@settingContainer false>
	<#if m.license??>
		<@setting label=b.gkey("institutions.license.host") labelFor="hostnames"><span class="infoField" id="hostnames">${m.license.joinedHostnames?html}</span></@setting>
		<@setting label=b.gkey("institutions.license.institutions") labelFor="institutions"><span class="infoField" id="institutions">${m.license.institutions}</span></@setting>
		<@setting label=b.gkey("institutions.license.maxusers") labelFor="users"><span class="infoField" id="users">${m.license.users}</span></@setting>
		<@setting label=b.gkey("institutions.license.equellaversion") labelFor="version"><span class="infoField" id="version">${m.license.version?html}</span></@setting>
		<@setting label=b.gkey("institutions.license.warningdate") labelFor="warnDate"><span class="infoField" id="warnDate">${m.license.warning()?date?string}</span></@setting>
		<@setting label=b.gkey("institutions.license.expirydate") labelFor="expiryDate"><span class="infoField" id="expiryDate">${m.license.expiry()?date?string}</span></@setting>
	</#if>
	<@setting label=b.gkey("institutions.license.upgrade") help=b.gkey("institutions.license.enterlicense") error=error labelFor=s.licenseField>
		<@textarea rows=6 section=s.licenseField spellcheck=false/>
	</@setting>
</@settingContainer>
<div class="button-strip">
	<@button section=s.changeButton showAs="save" />
</div>
