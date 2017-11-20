<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl" />
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<@css "shortcuturls.css"/>

<div class="addshortcut">
	<@settingContainer mandatory=true>
		<@setting label=b.key('dialog.label.shortcut') error=m.errors["shortcut"] mandatory=true labelFor=s.shortcutText>
			<@textfield section=s.shortcutText maxlength=64 class="focus" />
		</@setting>
		<@setting label=b.key('dialog.label.url') error=m.errors["url"] mandatory=true labelFor=s.urlText >
			<@textfield section=s.urlText maxlength=1024 autoSubmitButton=s.ok />
		</@setting>
	</@settingContainer>
</div>
