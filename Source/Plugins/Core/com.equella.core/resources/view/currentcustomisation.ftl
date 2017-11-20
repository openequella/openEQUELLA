<#include "/com.tle.web.freemarker@/macro/sections.ftl" />

<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<div class="area">
	<h2>${b.key('customisation.title')}</h2>
	<ul class="standard">
		<li><@render s.getDownload() /></li>
		<li><@render s.getDelete() /></li>
	</ul>
</div>