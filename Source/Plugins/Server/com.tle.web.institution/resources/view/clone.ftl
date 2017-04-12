<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/textfield.ftl">
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<div class="area">
	<h2>
		${b.gkey("institutions.clone.action.name")}
	</h2>
	<@settingContainer false>
		<@setting label=b.key("institution.details.schema") help=b.key('institution.details.schema.help') error=m.errors['schema'] mandatory=true>
			<#if m.selectedDatabase??>${m.selectedDatabase}<#else>${b.key('institution.details.schema.noneselected')}</#if>
			<@render s.selectDatabase />
		</@setting>
		<@setting label=b.key("institutions.details.name") section=s.name mandatory=true error=m.errors['name'] />
		<@setting label=b.key("institutions.details.url") section=s.url help=b.key("institutions.details.url.help") mandatory=true error=m.errors['url'] />
		<@setting label=b.key("institutions.details.filestore") section=s.filestore help=b.key("institutions.details.filestore.help") error=m.errors['filestoreId'] />
		<@setting label=b.key("institutions.details.password") labelFor=s.adminPassword >
			<@textfield section=s.adminPassword password=true/>
		</@setting>
		<@setting label=b.key("institutions.edit.confirm") help=b.key("institutions.details.password.help") labelFor=s.adminConfirm>
			<@textfield section=s.adminConfirm password=true />
		</@setting>
		<@setting label=b.key("institution.options") >
			<div class="input checkbox option"><@render section=s.itemsCheck /></div>
			<div style="padding-left: 20px; padding-top: 5px;"><div class="input checkbox option"><@render section=s.attachmentsCheck /></div></div>
		</@setting>
		<@setting label="">
			<div class="input checkbox option"><@render section=s.auditlogsCheck /></div>
		</@setting>
	</@settingContainer>
	<br>
	<div class="button-strip">
		<@button section=s.actionButton showAs="save">${b.gkey("institutions.clone.action.name")}</@button>
		<@button section=s.cancelButton showAs="cancel" />
	</div>
</div>
