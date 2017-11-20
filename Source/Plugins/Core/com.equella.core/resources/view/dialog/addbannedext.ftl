<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl" />
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<@css "dialog/addbannedext.css" />

<div class="addbannedext">
	<@settingContainer>
		<@setting label=b.key('addbannedext.dialog.label') mandatory=true labelFor=s.bannedExtText >
			<@textfield section=s.bannedExtText maxlength=64 class="focus" />
		</@setting>
	</@settingContainer>
</div>