<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<div class="area">
	<h2>${m.pageTitle}</h2>
	<@settingContainer mandatory=true>
		<@a.div id="controls">
			<#if m.editUuid??>
				<@setting label=b.key('editor.label.serveruuid') >
					<input type="text" readonly="readonly" value="${m.editUuid?html}" />
				</@setting>
			</#if>
			<@setting label=b.key('editor.label.name') section=s.title mandatory=true error=m.errors['name'] />
			<@setting label=b.key('editor.label.description') section=s.description mandatory=false  />
			
			<@setting label=""><hr></@setting>
			<@setting label=b.key('editor.label.applicationurl') section=s.applicationUrl mandatory=true error=m.errors['applicationurl']  />
			<@setting label=b.key('editor.label.contenturl') section=s.contentUrl mandatory=true error=m.errors['contenturl']  />
			<@setting label=b.key('editor.label.consumerkey') section=s.consumerKey mandatory=true error=m.errors['consumerkey']  />
			<@setting label=b.key('editor.label.consumersecret') section=s.consumerSecret mandatory=true error=m.errors['consumersecret']  />
			<@setting label=b.key('editor.label.systemid') section=s.echoSystemID mandatory=true error=m.errors['systemid'] help=b.key('editor.label.systemid.help')  />
		</@a.div>
	</@settingContainer>
	
	<hr>
	
	<@a.div id="connectionstatus">
		<@setting label=b.key('editor.label.testurls') error=m.errors['testurls'] >
		<@button section=s.testUrlButton showAs="verify" />
		<#if m.testStatus??>
			<span class="status ${m.testStatus}">
				${b.key('echo.editor.label.teststatus.' + m.testStatus)}
			</span>
		</#if>
		</@setting>
	</@a.div>
	
	<div class="button-strip">
		<@button section=s.saveButton showAs="save" /> <@render section=s.cancelButton />
	</div>
</div>