<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>
<#include "/com.tle.web.sections.standard@/filedrop.ftl"/>
<#include "/com.tle.web.sections.standard@/link.ftl">

<@css "universalresource.css" />
<#if m.displayDuplicateWarning>
  <@link section=s.duplicateWarningMessage style="color:red"/>
</#if>
<@render m.divTag />
