<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.equella@/macro/receipt.ftl">

<@css path="layouts/onecolumn.css" hasNew=true />

<div id="col">
	<@receipt m.receipt />
	<@render m.template['body']/>	
</div>
	