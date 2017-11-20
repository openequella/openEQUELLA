<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<@css "settings/filter.css" />

<div class="area">
	<h2>${m.title}</h2>
	<@settingContainer>
		<#assign nameErr><#if m.nameError??><@bundlekey m.nameError/></#if></#assign>
		<@setting label=b.key('settings.filter.label.name') 
			section=s.name 
			mandatory=true 
			error=nameErr />
		<#assign mimeErr><#if m.mimeError??><@bundlekey m.mimeError/></#if></#assign> 
		<@setting label=b.key('settings.filter.label.mimetypes')  
			mandatory=true
			error=mimeErr
			rowStyle="mimeRow"
			labelFor="s.mimeTypes">
			<div class="mimeTypesContainer input checkbox">
			<@render s.mimeTypes />
			</div>
		</@setting>
	</@settingContainer>
	<div class="button-strip">
		<@button section=s.saveButton showAs="save" />
		<@button section=s.cancelButton />
	</div>
</div>