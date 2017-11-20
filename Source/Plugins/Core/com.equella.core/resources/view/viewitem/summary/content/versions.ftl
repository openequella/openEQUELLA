<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<h2>${b.key("summary.content.versions.title")}</h2>
<@button section=s.backButton showAs="prev" />
<@render s.versionsTable />

