<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />

<@css "purchasedsummary.css" />

<div class="purchased">
	<h3>${b.key("purchased.summary.view.title")}</h3>
	<div>	
		<@render s.purchasedInfoTable />
	</div>
</div>