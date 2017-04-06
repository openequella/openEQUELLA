<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/textfield.ftl">
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<div class="area">
	<h2>${b.key("institutions.export.title")}</h2>

	<@settingContainer false>
		<@setting label="" error=m.error />
		<@setting label=b.key("institutions.details.name")>
			<input type="text" readonly="readonly" value="${m.name?html}" disabled="disabled" />
		</@setting>
		<@setting label=b.key("institutions.details.url")>
			<input type="text" readonly="readonly" value="${m.url?html}" disabled="disabled" />
		</@setting>
		<@setting label=b.key("institution.options") >
			<div class="input checkbox option"><@render section=s.itemsCheck /></div>
			<div style="padding-left: 20px; padding-top: 5px;"><div class="input checkbox option"><@render section=s.attachmentsCheck /></div></div>
		</@setting>
		<@setting label="">
			<div class="input checkbox option"><@render section=s.auditlogsCheck /></div>
		</@setting>

		<@setting label="" error=m.warning />
	</@settingContainer>
	<div class="button-strip">
		<@render s.actionButton>${b.key("institutions.export.action.name")}</@render>
		<@render s.cancelButton />
	</div>
</div>

