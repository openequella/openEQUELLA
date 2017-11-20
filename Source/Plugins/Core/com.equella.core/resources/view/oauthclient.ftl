<#ftl strip_whitespace=true />
<#import "/com.tle.web.sections.standard@/ajax.ftl" as ajax/>
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/dialog.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.standard@/checklist.ftl"/>

<@css "oauthclient.css" />

<div class="area">
	<h2>${m.pageTitle}</h2>
	
	<@settingContainer mandatory=true wide=true>
		<@setting label=b.key('entity.uuid') >
			<input type="text" readonly="readonly" value="${m.entityUuid?html}" />
		</@setting>
	
		<@setting label=b.key('client.editor.label.name') 
			help=b.key('client.editor.help.name') 
			section=s.nameField 
			error=m.errors["name"] 
			mandatory=true />
		
		<@setting label=b.key('client.editor.label.id') 
			section=s.clientIdField 
			error=m.errors["clientid"] 
			mandatory=true />
		
		<@ajax.div id="clientSecretDiv">
			<@setting 
				label=b.key('client.editor.label.secret') 
				help=b.key('client.editor.help.secret') 
				error=m.errors["clientsecret"]
				rowStyle="passRow"
				labelFor=s.clientSecretField>
				
				<span id="oace_cs" class="client_secret">${m.clientSecret}</span>
				<@render section=s.resetSecretButton/>
			</@setting>
		</@ajax.div>
		
		<@ajax.div id="flowAjaxDiv">
		
			<@setting label=b.key('client.editor.flow.label') 
				help=b.key(m.descriptionKey, m.defaultRedirectUrl) 
				error=m.errors["flowType"] 
				section=s.selectFlow
				mandatory=true />
	
			<#if m.flow??>
				
				<#if m.defaultOption && m.setUrl>
					<@setting label=b.key('client.editor.label.chooseurl') labelFor=s.chooseUrl>
						<@checklist section=s.chooseUrl list=true class="input radio"/>
					</@setting>
					<#if m.showSetUrl>
						<@render s.redirectUrlDiv>
							<@setting label=b.key('client.editor.help.redirect') 
								section=s.redirectUrlField 
								error=m.errors["redirecturl"] 
								mandatory=true
								help=b.key('client.editor.redirect.nondefault') />
						</@render>
					<#else>
						<@setting label=b.key('client.editor.help.redirect')
							help=b.key('client.editor.defaulturl.help') >
							${m.defaultRedirectUrl}
						</@setting>
					</#if>
				<#elseif m.setUrl>
					<@render s.redirectUrlDiv>
						<@setting label=b.key('client.editor.help.redirect') 
							section=s.redirectUrlField 
							error=m.errors["redirecturl"] 
							mandatory=true
							help=b.key('client.editor.redirect.nondefault') />
					</@render>
				</#if>
					
				
				<#if m.selectUser>
					<@ajax.div id="userAjaxDiv">
						<@setting label=b.key('client.editor.label.fixeduser') 
							help=b.key('client.editor.help.fixeduser')
							error=m.errors["selectUser"] 
							mandatory=true >
							
							<#if m.fixedUser??>
								<@render m.fixedUser />
							</#if>
							<@button section=s.selectUserButton showAs="select_user" />
							<#if m.fixedUser??>
							 	<@button section=s.clearUserButton showAs="delete" />
							</#if>
						</@setting>
					</@ajax.div>
				</#if>
			</#if>
		</@ajax.div>
			
	</@settingContainer>

	<div class="button-strip">
		<@button section=s.saveButton showAs="save"/>
		<@button section=s.cancelButton />
	</div>
</div>