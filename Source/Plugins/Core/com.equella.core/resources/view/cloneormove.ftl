<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<h2>
	<#if m.isMove>
		<@bundlekey "moveonly.header" />
	<#else>
		<@bundlekey "cloneonly.header" />
	</#if>
</h2>
<@button section=s.backButton showAs="prev" />
<@renderList m.sections />
