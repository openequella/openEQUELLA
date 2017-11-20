<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<@setting  section=s.url error=m.errors["url"] mandatory=true label=b.key('editor.iframe.label.url') />