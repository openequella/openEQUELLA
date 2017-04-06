<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>

<@css path="mailsettings.css" hasRtl=true />

<div class="area">
	<h2>${b.key('settings.page.title')}</h2>
	<@settingContainer mandatory=false>
		<@div id="required">
			<@setting label=b.key('settings.label.server') section=s.serverUrl error=m.errors['serverUrl'] />
			<@setting label=b.key('settings.label.sender.email') section=s.fromEmailAddr error=m.errors['fromEmailAddr'] />
		</@div>
		<@setting label=b.key('settings.label.username') section=s.username />
		<@setting label=b.key('settings.label.password') help=b.key('settings.label.help.account') labelFor=s.password>
			<@textfield id="password" section=s.password password=true  />
		</@setting>
		<@setting label=b.key('settings.label.sender.displayname') section=s.displayName />
		<hr>
		<@div id="testemail">
			<@setting label=b.key('settings.label.testemail') section=s.testEmailAddr mandatory=true error=m.errors["testEmailAddr"] help=b.key('settings.label.help.emailaddress')/>
		</@div>
		<@setting label="">
			<@button section=s.testButton showAs="verify" />
		</@setting>
		<@setting label=b.key('settings.label.emailstatus')>
			<@div id="emailstatus" showEffect="slide">
				<#if !m.successful && m.errors['emailError']?? >
					<div class="error">
						${m.errors['emailError']}
					</div>
				<#elseif m.successful && m.errors['emailSuccess']?? >
					<div class="success">
						${m.errors['emailSuccess']}
					</div>
				<#else>
					<div>
						${b.key('settings.test.email.prompt')}
					</div>
				</#if>
			</@div>
		</@setting>
	</@settingContainer>
	<div class="button-strip">
		<@button section=s.saveButton showAs="save" />
	</div>
</div>