<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.standard@/dialog.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as ajax/>

<@ajax.div id="brightspacesetup">

	<#include "/com.tle.web.connectors@/field/serverurl.ftl" />

	<#if m.testedUrl??>
		
		<@setting label=b.key('editor.label.appsetup') help=b.key('editor.help.appsetup', m.testedUrl, m.trustedUrl) />
		
		<@setting label=b.key('editor.label.appid') labelFor=s.appId mandatory=true help=b.key('editor.help.appid') error=m.errors["appid"] >
			<@textfield section=s.appId />
		</@setting>
		
		<@setting label=b.key('editor.label.appkey') labelFor=s.appKey mandatory=true help=b.key('editor.help.appkey') error=m.errors["appkey"] >
			<@textfield section=s.appKey />
		</@setting>
		
		<@setting label=b.key('editor.label.testapp') labelFor=s.testAppButton mandatory=true error=m.errors["testapp"]>
			<@button section=s.testAppButton showAs="verify" />
		
			<#if m.testAppStatus??>
				<span class="status ${m.testAppStatusClass}"> ${m.testAppStatus} </span>
			</#if>
		</@setting>
		
		<#if m.appOk>
			<@setting label=b.key('editor.label.adminsignin') help=b.key('editor.help.adminsignin')>
				<@dialog section=s.authDialog />
				<@button section=s.authDialog.opener size="medium"><@bundlekey "editor.button.adminsignin" /></@button>
				
				<#if m.adminStatus??>
					<span class="status ${m.adminStatusClass}"> ${m.adminStatus} </span>
				</#if>
			</@setting>
			
			<#if m.adminOk>
				<@setting label=b.key('editor.label.nextsteps') help=b.key('editor.help.nextsteps', m.ltiConsumersUrl) />
			</#if>
		</#if>
	</#if>
</@ajax.div>

<@setting label=''>
	<hr>
</@setting>