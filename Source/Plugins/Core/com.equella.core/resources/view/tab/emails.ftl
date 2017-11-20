<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/textfield.ftl">
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<h2>${b.key("institutions.server.emails.name")}</h2>
<@settingContainer false>
	<@setting label=b.key("institutions.server.emails.emails") 
		mandatory=true 
		error=m.errors["emails"] 
		labelFor=s.emails 
		help=b.key("institutions.server.emails.emails.help")>
		<@textfield section=s.emails class="detail" />
	</@setting>

    <@setting label=b.key("institutions.server.emails.noreplysender")
        mandatory=true
        error=m.errors["noreplysender"]
        labelFor=s.noReplySender
        help=b.key("institutions.server.emails.noreplysender.help")>
        <@textfield section=s.noReplySender class="detail" />
    </@setting>

	<@setting label=b.key("institutions.server.emails.smtp") 
		mandatory=true 
		error=m.errors["smtp"] 
		labelFor=s.smtpServer
		help=b.key("institutions.server.emails.smtp.help")>
		<@textfield section=s.smtpServer class="detail" />
	</@setting>

	<@setting label=b.key("institutions.server.emails.smtp.user") 
		error=m.errors["smtpuser"] 
		labelFor=s.smtpUser
		help=b.key("institutions.server.emails.smtp.user.help")>
		<@textfield section=s.smtpUser class="detail" />
	</@setting>

	<@setting label=b.key("institutions.server.emails.smtp.password") 
		error=m.errors["smtppassword"] 
		labelFor=s.smtpPassword
		help=b.key("institutions.server.emails.smtp.password.help")>
		<@textfield  section=s.smtpPassword class="detail" password=true />
	</@setting>
	<@setting label=b.gkey("institutions.password.column.confirm") labelFor=s.confirmPassword >
		<@textfield section=s.confirmPassword class="detail" password=true />
	</@setting>

</@settingContainer>
<div class="button-strip">
	<@button section=s.save showAs="save" />
</div>

<#assign PART_FUNCTION_DEFINITIONS>
function ensurePasswordMatch(userfield, p1, p2)
{
	if( userfield != undefined && userfield.value.length > 0 && p1 != p2 )
	{
		alert("${b.gkey("institutions.password.match")?js_string}");
		return false;
	}
	return true;
}
</#assign>
