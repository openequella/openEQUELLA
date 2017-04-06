<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl" />

<@setting label=b.key('editor.username') section=s.apiUsername error=m.errors["username"] mandatory=true />
<@setting label=b.key('editor.sandbox') section=s.sandboxMode />