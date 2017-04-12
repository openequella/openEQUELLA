<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/textfield.ftl">
<#include "/com.tle.web.sections.standard@/textarea.ftl">
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<@css path="share/sharedialog.css" />

<h3>${b.key("share.cloud.sharewithothers.dialog.heading")}</h3>
<div id="sharecloudcontainer">
<@settingContainer>
	<@setting label=b.key("share.cloud.sharewithothers.dialog.share.email") section=s.emailField mandatory=true error=m.errors["email"] />
	<@setting label=b.key("share.cloud.sharewithothers.dialog.share.message") mandatory=true error=m.errors["message"] labelFor=s.messageField >
		<@textarea section=s.messageField rows=5 />
	</@setting>
</@settingContainer>
</div>