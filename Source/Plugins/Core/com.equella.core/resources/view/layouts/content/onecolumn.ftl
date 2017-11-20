<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.equella@/macro/receipt.ftl">

<@css "layouts/onecolumn.css" />

<div id="col">
	<@receipt m.receipt />
	<@render m.template['body']/>	
</div>
	