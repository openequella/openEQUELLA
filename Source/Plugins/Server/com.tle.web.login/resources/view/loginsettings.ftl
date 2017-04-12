<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.standard@/dialog.ftl"/>
<#include "/com.tle.web.sections.standard@/textarea.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<@css "loginsettings.css" />

<div class="area">
	<h2>${b.key('login.title')}</h2>

	<@settingContainer mandatory=true wide=true>
		<@setting label=b.key('login.enable') section=s.enableSSLCheck />
		<@setting label=b.key('login.enableloginip') section=s.enableViaIpCheck />
		
		<@a.div id="innercontrols" class="indent-margin-left">
			<#if m.showInnerControls>
			<hr>
				<@a.div id="selecteduser">
					<@setting label=b.key('login.asuser') mandatory=true error=m.errors['user'] >
						<#if m.userLink??><@render m.userLink /></#if>
						<@button section=s.selectUserDialog.opener showAs="select_user">${b.key("button." + (m.userLink??)?string("changeuser", "selectuser"))}</@button>
					</@setting>
				</@a.div>

				<@setting label=b.key('login.notautomatic') section=s.disableAutoLoginCheck help=b.key('login.notautomatic.help') />
				<@setting label=b.key('login.noeditdetails') section=s.disallowUserEditCheck />
				<@setting label=b.key('login.transientdrmacceptances') section=s.transientDRMCheck />
				

				<@a.div id="enteredIpAddress">
					<@setting label=b.key('login.ipaddresses') mandatory=true error=m.errors['iplist'] labelFor=s.ipAddressTable>
						<@render s.ipAddressTable />
					</@setting>
				</@a.div>
			</#if>
			<hr>
		</@a.div>
		<@setting label=b.key('login.enableanonacl') section=s.enableAnonACL />

		<@setting label=b.key('loginnotice.title') labelFor=s.loginNoticeField >
			<@textarea section=s.loginNoticeField class="notice" rows=14 />
		</@setting>

	</@settingContainer>

	<div class="button-strip">
		<@button section=s.saveButton showAs="save"/>
	</div>
</div>
