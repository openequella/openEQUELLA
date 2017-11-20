<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/dropdown.ftl" />
<#include "/com.tle.web.sections.standard@/textfield.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl" />

<@css "ga.css"/>

<div class="area">
	<h2>${b.key("analytics.pagetitle")}</h2>
	<p>${b.key("analytics.description")}</p>
	
	<p>${b.key("account.dont.have")} <a href="http://www.google.com/analytics/sign_up.html">${b.key("account.signup")}</a></p>
	
	<@setting label=b.key("account.enter") section=s.accountId help=b.key("account.example") />

	<@setting label=b.key("analytics.status")>
		<span class="bold">
			<#if m.setup>
				${b.key("analytics.receivingData")}
			<#else>
				${b.key("analytics.notInstalled")}
			</#if>
		</span>
	</@setting>

	<p><a target="_blank" href="http://www.google.com/support/analytics/bin/answer.py?answer=81977&amp;topic=10983&amp;hl=en_US">${b.key("analytics.help.trackingCode")}</a></p>
	<p><a target="_blank" href="http://www.google.com/analytics/">${b.key("analytics.help.visitorStats")}</a></p>
		
	<div class="button-strip">
		<@button section=s.save showAs="save" />
	</div>
</div>

