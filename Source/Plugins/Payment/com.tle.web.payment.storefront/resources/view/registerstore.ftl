<#ftl strip_whitespace=true />
<#import "/com.tle.web.sections.standard@/ajax.ftl" as ajax/>
<#include "/com.tle.web.sections.standard@/button.ftl"/>
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl" />

<@css "editstore.css" />

<div class="area">
	<h2>${m.connected?string(b.key('store.register.title.edit'), b.key('store.register.title.new'))}</h2>

	<@settingContainer mandatory=true wide=true>
		<@setting label=b.key('entity.uuid') >
			<input type="text" readonly="readonly" value="${m.entityUuid?html}" />
		</@setting>
		
		<@setting label=b.key('store.register.label.storeurl') 
			section=s.storeUrl 
			help=b.key('store.register.help.storeurl')
			error=m.errors["storeUrl"] 
			mandatory=true />
		
		<@setting label=b.key('store.register.label.clientid') 
			section=s.clientId 
			help=b.key('store.register.help.clientid')
			error=m.errors["clientId"] 
			mandatory=true />

		<@setting label=b.key('store.register.label.connect') 
			help=m.connected?string(b.key('store.register.help.reconnect'), b.key('store.register.help.connect'))>
			
			<@button section=s.connectButton showAs="verify" />
			<#if m.testStatus??>
			  		<span class="status ok">${b.key('store.register.connected.receipt')}</span>
			</#if> 	
		</@setting> 
		
		<#if m.connected>  
			<@setting label=b.key('store.register.label.enable') section=s.enabled />
		</#if>
	</@settingContainer>

	<@ajax.div id="actions">
		<div class="button-strip">
			<#if m.connected>  
				<@button section=s.saveButton showAs="save"/>
			</#if>
			<@button section=s.cancelButton showAs="cancel"/>
		</div>
	</@ajax.div>

</div>
