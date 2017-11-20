<#ftl strip_whitespace=true />
<#import "/com.tle.web.sections.standard@/ajax.ftl" as ajax/>
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl" />

<@css "adduserandquota.css" />
<div class="area">
	<h2>${m.heading}</h2>
	
	<@settingContainer mandatory=false wide=true>
		<@setting label=b.key('addquota.dialog.label') mandatory=true labelFor=s.quotaSizeField error=m.errors['userquota']>
				<@render section=s.quotaSizeField class="focus" />
		</@setting> 
		
		<@setting label=b.key('addquota.dialog.users')  mandatory=true error=m.errors['expression']>
				<#if m.expressionPretty?? && m.expressionPretty != "">
					<span class="expression">${m.expressionPretty}</span>
					<br>
				</#if>
				<@button section=s.userSelector.opener showAs="select_user">${b.key('addquota.selectuser.button')}</@button>
		</@setting> 
	</@settingContainer>
	<div class="button-strip">
		<@button section=s.saveButton showAs="save" />
		<@button section=s.cancelButton />
	</div>
</div>