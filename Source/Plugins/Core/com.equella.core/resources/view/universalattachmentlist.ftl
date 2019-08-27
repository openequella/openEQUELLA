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
  <@link section=s.duplicateWarningMessage style="color:red; display: ${visibility}" class="attachment-duplicate-message"/>

<@render m.divTag />
