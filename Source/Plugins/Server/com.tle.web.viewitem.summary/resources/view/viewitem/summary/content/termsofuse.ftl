<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<h2>${b.key("summary.content.termsofuse.title")}</h2>

<#include "/com.tle.web.viewitem.summary@/viewitem/drm/terms.ftl" />

<@button section=s.backButton showAs="prev" />
<#if m.showAgreements>
	<h3 id="acceptancecount">${m.agreementsLabel}</h3>
	<#if m.agreementsCount gt 0>
		<@render s.agreementsTable />
	</#if>
</#if>
