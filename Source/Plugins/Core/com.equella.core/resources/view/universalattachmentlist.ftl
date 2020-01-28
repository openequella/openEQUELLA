<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>
<#include "/com.tle.web.sections.standard@/filedrop.ftl"/>
<#include "/com.tle.web.sections.standard@/link.ftl">
<@css "universalresource.css" />
<#if m.displayDuplicateWarning>
  <#assign visibility = "inline">
<#else >
  <#assign visibility = "none">
</#if>
<div id="${m.id}_attachment_duplicate_warning" style="display: ${visibility}">
  <p>
    <b style="color:red" role="alert">${m.duplicateWarningMessage}</b>
  </p>
  <@link section=s.duplicateWarningLink/>
</div>

<@render m.divTag />
