<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<h2>${b.key("viewitem.saleshistory.title")}</h2>
<@button section=s.backButton showAs="prev" />
<@render s.historyTable />