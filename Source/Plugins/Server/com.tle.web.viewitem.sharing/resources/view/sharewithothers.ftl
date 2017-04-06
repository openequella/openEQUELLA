<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/textfield.ftl">
<#include "/com.tle.web.sections.standard@/textarea.ftl">
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<h2>${b.key("summary.content.sharewithothers.pagetitle")}</h2>

<#if m.showNotifyWhenLive>
	<h3>${b.key("summary.content.sharewithothers.notify.title")}</h3>
	<p>${b.key("summary.content.sharewithothers.notify.description")}</p>
	<@a.div id="selectedusers">
		<p><@render s.othersTable /></p>
	</@a.div>
	<br>
</#if>

<#if m.sharePassOn>
	<h3>${b.key("summary.content.sharewithothers.share.title")}</h3>
	<p>${b.key("summary.content.sharewithothers.share.description")}</p>
	<@settingContainer>
		<@setting label=b.key("summary.content.sharewithothers.share.email") section=s.emailField mandatory=true error=m.errors["email"] />
		<@setting label=b.key("summary.content.sharewithothers.share.message") mandatory=true error=m.errors["message"] labelFor=s.messageField >
			<@textarea section=s.messageField rows=10 />
		</@setting>
		<@setting label=b.key("summary.content.sharewithothers.share.allow") section=s.daysList />
		<@setting label="" rowStyle="text-right">
			<@button section=s.sendEmailButton showAs="email" size="medium" />
		</@setting>
	</@settingContainer>
</#if>