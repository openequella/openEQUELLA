<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<div class="area kalturaservers">
	<h2>${m.pageTitle}</h2>
	<@a.div id="controls">
		<@settingContainer mandatory=true>
			<@a.div id="required">
				<#if m.editUuid??>
					<@setting label=b.key('editor.label.serveruuid') >
						<input type="text" readonly="readonly" value="${m.editUuid?html}" />
					</@setting>
				</#if>
				<@setting label=b.key('editor.label.name') section=s.title mandatory=true error=m.errors['name'] />
				<@setting label=b.key('editor.label.description') section=s.description mandatory=false  />
				<@setting label=""><hr></@setting>
				<@setting label=b.key('editor.label.endpoint') section=s.endPoint mandatory=true error=m.errors['endpoint'] help=b.key('editor.label.endpoint.help') />
				<@setting label=b.key('editor.label.partnerid') mandatory=true error=m.errors['partnerid'] >
					<@render section=s.partnerId />
					<#if m.editUuid??><@render section=s.unlockPidState style='display:none' /><@render section=s.unlockPartnerIdButton /></#if>
				</@setting>
				<@setting label=b.key('editor.label.subpartnerid') mandatory=false error=m.errors['subpartnerid'] >
					<@render section=s.subPartnerId />
					<#if m.editUuid??><@render section=s.unlockSpidState style='display:none' /><@render section=s.unlockSubPartnerIdButton /></#if>
				</@setting>
				<@setting label=b.key('editor.label.adminsecret') section=s.adminSecret mandatory=true error=m.errors['adminsecret'] />
				<@setting label=b.key('editor.label.usersecret') section=s.userSecret mandatory=true error=m.errors['usersecret'] />
			</@a.div>
		</@settingContainer>
		<hr>
		<div id="testconnection">
			<@setting label=b.key('editor.label.test.status') >
				<@a.div id="connectionstatus" showEffect="slide">
					<#if !m.successful && m.errors['connectiontest']?? >
						<div class="ctrlinvalid">
							<span>${m.errors['connectiontest']}</span>
						</div>
					<#elseif m.successful >
						<div class="success">
							<span>${b.key('editor.test.success')}</span>
						</div>
					<#else>
						<div class="waiting">
							${b.key('editor.label.test.prompt')}
						</div>
					</#if>
				</@a.div>
			</@setting>
			<@a.div id="testbutton" >
				<@setting label="" error=m.errors['nottested'] >
					<@button section=s.testButton showAs="verify" />
				</@setting>
				<#if m.successful >
					<@setting label=b.key('editor.label.player') section=s.selectConfId mandatory=true />
				</#if>
			</@a.div>
		</div>
	</@a.div>
	<br>
	<div class="button-strip">
		<@button section=s.saveButton showAs="save" /> <@render section=s.cancelButton />
	</div>
</div>