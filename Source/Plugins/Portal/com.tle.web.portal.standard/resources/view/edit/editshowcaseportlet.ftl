<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/thickbox.ftl"/>

<@render s.resultsCallback/>

<div class="setting">
	<div class="label"><@bundlekey "editor.showcase.label.xx"/></div>
	<div class="field"><@thickbox section=s.searchButton title=b.key("searchtitle") width="85%" height="85%" class="ctrlbuttonNW">${b.key("searchbutton")}</@thickbox></div>
	<#if m.errors["xx"]??>
		<div class="error">${m.errors["xx"]?html}</div>
	</#if>
</div>
