<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<div>
	<h2>${b.key("exporters.title")}</h2>
	<@button section=s.backButton showAs="prev" />
	<@renderList list=m.sections separator='<hr>' />
</div>