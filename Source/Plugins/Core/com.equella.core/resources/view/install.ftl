<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/textarea.ftl">
<#include "/com.tle.web.sections.standard@/textfield.ftl">
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<@css "institutions.css" />

<div class="area">
	<h2>${b.key("install.heading")}</h2>
	<p>
		${b.key("install.description")}
	</p>
	<p>
		<@settingContainer wide=true>		
			<@setting label=b.key("institutions.server.emails.emails") mandatory=true error=m.errors["emails"] labelFor=s.emails help=b.key("institutions.server.emails.emails.help")>
				<@textfield section=s.emails class="detail" />
			</@setting>
			
			<@setting label=b.key("institutions.server.emails.smtp") 
				mandatory=true 
				error=m.errors["smtp"] 
				labelFor=s.smtpServer
				help=b.key("institutions.server.emails.smtp.help")>
				<@textfield  section=s.smtpServer class="detail" />
			</@setting>
			<@setting label=b.key("institutions.server.emails.noreplysender")
                mandatory=true
                error=m.errors["noreplysender"]
                labelFor=s.noReplySender
                help=b.key("institutions.server.emails.noreplysender.help")>
                <@textfield  section=s.noReplySender class="detail" />
            </@setting>
				<@setting label=b.key("institutions.server.emails.smtp.user") 
				error=m.errors["smtpuser"] 
				labelFor=s.smtpUser
				help=b.key("institutions.server.emails.smtp.user.help")>
				<@textfield section=s.smtpUser class="detail" />
			</@setting>				
			<@setting label=b.key("institutions.server.emails.smtp.password") 
				error=m.errors["smtppass"] 
				labelFor=s.smtpPassword
				help=b.key("institutions.server.emails.smtp.password.help")>
				<@textfield  section=s.smtpPassword class="detail" password=true />
			</@setting>				
			<@setting label=b.key("institutions.server.emails.smtp.password.confirm") labelFor=s.smtpPasswordConfirm >
				<@textfield section=s.smtpPasswordConfirm class="detail" password=true />
			</@setting>
			

			<@setting label=b.key("install.initialpassword") mandatory=true error=m.errors["password"] labelFor=s.password>
				<@textfield password=true section=s.password size=25/>
			</@setting>
			<@setting label=b.key("install.initialpasswordconfirm") mandatory=true labelFor=s.passwordConfirm>
				<@textfield password=true section=s.passwordConfirm size=25/>
			</@setting>			
		</@settingContainer>
		
		<div class="button-strip">
			<@render s.installButton/>
		</div>
	</p>
</div>
