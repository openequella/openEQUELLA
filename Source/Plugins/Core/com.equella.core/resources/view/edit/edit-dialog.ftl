<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<@css "edit-dialog.css"/>

<@setting label=b.key('manage.edit.dialog.name') labelFor=s.nameField mandatory=true>
	<@render section=s.nameField class="focus" />
</@setting>

<#if m.editDescription>
<@setting label=b.key('manage.edit.dialog.description') section=s.descriptionField />
</#if>

<div class="status">
<#if m.error??>
	<p class="error">${m.error}</p>
</#if>
</div>