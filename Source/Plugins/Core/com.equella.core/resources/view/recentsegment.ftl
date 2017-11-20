<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.equella@/styles/altlinks.ftl">
<@css "selectionhome.css"/>
<#if m.recent?size &gt; 0>
	<div class="alt-links">
		<#list m.recent as r>
			<@render section=r.link class="${altclass(r_index)}">${r.title?html}<br></@render>
		</#list>
	</div>
<#else>
	<div class="noresults">
		<p>${b.key("recent.norecent")}</p>
	</div>
</#if>