<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/textfield.ftl">
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<div class="area">
	<h2>
		${b.gkey("institutions.import.action.name")}
	</h2>
	<@settingContainer false>
		<@setting label=b.key("institution.details.schema") help=b.key('institution.details.schema.help') error=m.errors['schema'] mandatory=true>
			<#if m.selectedDatabase??>${m.selectedDatabase}<#else>${b.key('institution.details.schema.noneselected')}</#if>
			<@render s.selectDatabase />
		</@setting>
		<@setting label=b.key("institutions.details.name") section=s.name error=m.errors['name'] mandatory=true />
		<@setting label=b.key("institutions.details.url") section=s.url error=m.errors['url'] help=b.key("institutions.details.url.help") mandatory=true />
		<@setting label=b.key("institutions.details.filestore") error=m.errors['filestoreId'] section=s.filestore help=b.key("institutions.details.filestore.help") />
		<@setting label=b.key("institutions.details.password") labelFor=s.adminPassword>
			<@textfield section=s.adminPassword password=true/>
		</@setting>
		<@setting label=b.key("institutions.edit.confirm") help=b.key("institutions.details.password.help") labelFor=s.adminConfirm>
			<@textfield section=s.adminConfirm password=true />
		</@setting>
	</@settingContainer>
	<br>
	<div class="button-strip">
		<@render s.actionButton>${b.gkey("institutions.import.action.name")}</@render>
		<@render s.cancelButton />
	</div>
</div>
