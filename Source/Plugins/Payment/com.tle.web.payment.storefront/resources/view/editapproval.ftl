<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl" />

<div class="area">
	<h2>${m.pageTitle}</h2>
	<@settingContainer mandatory=true wide=true>
		<@setting label=m.fromLabel mandatory=true help=m.fromHelp error=m.errors['from']>
			<@div id="userDiv">
				<#if m.userPretty?? && m.userPretty != "">
						${m.userPretty}
						<br>
				</#if>
				<@button section=s.userSelector.opener showAs="select_user">${b.key('approvals.edit.purchase.select')}</@button>
			</@div>
		</@setting>
		<@setting label=m.toLabel mandatory=true help=m.toHelp error=m.errors['to']>
			<@div id="approverDiv">
				<#if m.approverPretty?? && m.approverPretty != "">
						${m.approverPretty}
						<br>
				</#if>
				<@button section=s.approverSelector.opener showAs="select_user">${b.key('approvals.edit.purchase.select')}</@button>
			</@div>
		</@setting>
		
	</@settingContainer>
	
	<@div id="actions">
		<div class="button-strip">
			<@button section=s.saveButton showAs="save"/>
			<@button section=s.cancelButton showAs="cancel"/>
		</div>
	</@div>
</div>