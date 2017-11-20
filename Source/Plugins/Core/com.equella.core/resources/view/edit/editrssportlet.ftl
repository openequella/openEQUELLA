<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<@setting label=b.key('editor.rss.label.url') section=s.url error=m.errors["url"] mandatory=true />
<@setting section=s.defaultResultsCount error=m.errors["results"] mandatory=true label=b.key('editor.rss.label.results') />
<@setting section=s.displayTypeList label=b.key('editor.rss.label.display') />