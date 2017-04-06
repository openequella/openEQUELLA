<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/textfield.ftl">
<#include "/com.tle.web.sections.standard@/dropdown.ftl">
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<@css "institutions.css" />

<div class="area">
	<h2>
		${b.key("institutions.edit.title")}
	</h2>
	<@settingContainer false>
		<@setting label=b.key("institutions.edit.name") section=s.name error=m.errors['name'] />
		<@setting label=b.key("institutions.edit.url") error=m.errors['url'] labelFor=s.url>
			<@textfield class="unlockable field" section=s.url maxlength=100/>
			<@render section=s.unlockUrlButton />
		</@setting>
		<br>
		<@setting label=b.key("institutions.edit.filestore") error=m.errors['filestoreId'] labelFor=s.filestore>
			<@render class="unlockable field" section=s.filestore/> 
			<@render section=s.unlockFilestoreButton/>
		</@setting>
		<@setting label=b.key("institutions.edit.limit") error=m.errors['quota'] labelFor=s.limit help=b.key("institutions.edit.limit.help")>
			<@render class="quota" section=s.limit/>
		</@setting>
		<@setting label=b.key("institutions.edit.currentusage")>
			<@a.div id="usage-ajax" class="usage"><#if m.fileSystemUsage??>${m.fileSystemUsage} ${b.key('institutions.edit.filestorageinuse')}</#if></@a.div>
		</@setting>
		<br>
		<@setting label=b.key("institutions.edit.timezone") section=s.timeZones help=b.key("institutions.edit.date.help")/>
		<br>
		<@setting label=b.key("institutions.edit.admin") labelFor=s.adminPassword>
			<@textfield password=true section=s.adminPassword/>
		</@setting>
		<@setting label=b.key("institutions.edit.confirm") labelFor=s.adminConfirm>
			<@textfield password=true section=s.adminConfirm autoSubmitButton=s.actionButton/>
		</@setting>
	</@settingContainer>
	<div class="button-strip">
		<@button section=s.actionButton showAs="save" showAs="save">${b.key("institutions.edit.action.name")}</@button>
		<@button section=s.cancelButton showAs="cancel" />
	</div>
</div>
				
