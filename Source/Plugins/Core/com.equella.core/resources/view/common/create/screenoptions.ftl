<#ftl strip_whitespace=true/>
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>

<@css "portalhelp.css"/>

<h3><@bundlekey "common.createnew.selectatype"/></h3>
<ul class="portallist">
	<#list m.portletTypes as type>
		<li><@render type.first /> &mdash; ${b.gkey(type.second)}</li>
	</#list>
</ul>
