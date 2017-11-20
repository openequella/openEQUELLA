<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<@css "mimetypes.css" />

<div class="area">
	<@settingContainer>
		<div class="mimeEditPage">
			<#assign title><#if m.editId==0>add<#else>edit</#if>.pagetitle</#assign>
			<h2>${b.key(title)}</h2>
			<#if m.errorLabel??>
				<@setting label="" error=m.errorLabel />
			</#if>
			<#list s.tabs.tabModel.getVisibleTabs(_info) as tab>
				<@render tab.renderer/>
			</#list>
		</div>
	</@settingContainer>
	<div class="button-strip">
		<@button section=s.saveButton showAs="save" />
		<@button section=s.cancelButton />
	</div>
</div>