<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl" >

<div class="area">
	<h2>${b.key("settings.title")}</h2>		
	
	<@setting label=b.key("settings.apikey.label") help=b.key("settings.apikey.help") >
		<@textfield section=s.apiKey maxlength=512 />
	</@setting>
	
	<div class="button-strip">
		<@button section=s.saveButton showAs="save" />
	</div>
</div>