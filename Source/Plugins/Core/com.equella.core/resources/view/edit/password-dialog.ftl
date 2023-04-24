<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl" />
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<@div id="main">
	<@settingContainer mandatory=false>
		<@setting label=b.key('internal.oldpassword') error=m.errorList["oldpass"] labelFor=s.oldPassword>
			<@textfield section=s.oldPassword maxlength=32 password=true class="focus"/>
		</@setting>
		<@setting label=b.key('internal.passwordchars') error=m.errorList["password"] labelFor=s.newPassword>
			<@textfield section=s.newPassword maxlength=32 password=true />
		</@setting>
		<@setting label=b.key('internal.passwordconfirm') error=m.errorList["confirmpass"] labelFor=s.confirmPassword>
			<@textfield section=s.confirmPassword maxlength=32  password=true autoSubmitButton=s.ok/>
		</@setting>
	</@settingContainer>
</@div>