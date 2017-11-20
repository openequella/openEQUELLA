<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<@css "promptdialog.css"/>

<div class="area prompt">
	<h3>${s.prompt}</h3>
	<@render s.text />
</div>