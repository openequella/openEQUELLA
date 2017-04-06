<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as ajax/>

<div class="area">
	<h2>${m.pageTitle}</h2>

	<@settingContainer mandatory=m.editorRenderable?? wide=true>
		<#if !m.editExisting>
			<@setting label=b.key('editor.label.connectortype') section=s.connectorTypes />	
		</#if>
	
		<@ajax.div id="connectorEditor">
			<#if m.editorRenderable??>
				<#if !m.editExisting><hr></#if>
				<@render m.editorRenderable />
			</#if>
		</@ajax.div>
	</@settingContainer>
	
	<@ajax.div id="actions">
		<div class="button-strip">
			<#if m.editorRenderable??>
					<@button section=s.saveButton showAs="save" />
			</#if>
			<@render s.cancelButton />
		</div>
	</@ajax.div>
</div>
