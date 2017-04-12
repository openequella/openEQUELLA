<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/checklist.ftl"/>

<@css path="actions/sort.css" plugin="com.tle.web.search" />

<div class="sortaction">
	<label for="${s.sortOptions.getElementId(_info)}" >
		<h3>${b.gkey("com.tle.web.search.sortsection.order.title")}</h3>
	</label>
	<div class="input select">
		<@render s.sortOptions />
	</div>
	<div class="input checkbox"> 
		<@render section=s.reverse />
	</div>
</div>