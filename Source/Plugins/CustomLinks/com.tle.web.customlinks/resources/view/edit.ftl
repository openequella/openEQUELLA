<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl" />
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>
<#include "/com.tle.web.sections.standard@/file.ftl">
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<@css path="customlinks.css" hasRtl=true />

<div class="area">
	<h2>${m.heading}</h2>
	<#if m.editing>
		<@settingContainer>
			<@setting label=b.key('entity.uuid') >
				<input type="text" readonly="readonly" value="${m.entityUuid?html}" />
			</@setting>
			<@setting label=b.key('edit.display') section=s.displayNameField error=m.errors["displayNameField"] mandatory=true/>
			<@div id="downloadIcon">
				<@setting label=b.key('edit.url') section=s.urlField error=m.errors["urlField"] help=b.key('url.help') mandatory=true />
			</@div>
			<@setting label=b.key('edit.newwindow') section=s.newWindow />
			<@setting label=b.key('edit.users') help=b.key('view.help')>
				<#if m.expressionPretty?? && m.expressionPretty != "">
					${m.expressionPretty}
					<br>
				</#if>
				<@button section=s.selector.opener showAs="select_user">${b.key('edit.select')}</@button>
			</@setting>
			<hr>
			<@setting label=b.key("edit.upload.label") error=m.errors["upload"] labelFor=s.file>
				<@file section=s.file/>
				<@button section=s.uploadButton icon="upload" />
			</@setting>
			<@setting label=b.key("edit.download.label") help=b.key("edit.download.help")>
				<@button section=s.downloadButton icon="download" />
			</@setting>
			<@div id="currentIcon">
			<#if m.fileName??>
				<@setting label=b.key("edit.upload.filename")>
					<@render s.image /> <@button section=s.deleteIconButton showAs="delete" /> 
				</@setting>
			</#if>
			</@div>
			</@settingContainer>
			<div class="button-strip">
				<@button section=s.saveButton showAs="save" />
				<@button section=s.cancelButton />
			</div>
	</#if>	
</div>
