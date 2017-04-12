<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<@css path="zquery.css" hasRtl=true />

<div id="searchform" class="area z3950" role="search">
	<h2><@bundlekey value="search.query.searching" params=[m.title]/></h2>
	
	<div class="query-wrapper">
		<div class="row">
			<span class="operator where"><@bundlekey "search.label.where" /></span>
			<@render section=s.use1 class="use" />
			<span class="is"><@bundlekey "search.label.is" /></span>
			<@textfield section=s.queryField autoSubmitButton="searchform-search" class="term" />
			
			<@button id="searchform-search" section=s.searchButton showAs="search" />
		</div>
	
		<div class="row">
			<span class="operator"><@render section=s.op2 /></span>
			<@render section=s.use2 class="use" />
			<span class="is"><@bundlekey "search.label.is" /></span>
			<@textfield section=s.term2 autoSubmitButton="searchform-search" class="term" />
		</div>
	
		<div class="row">
			<span class="operator"><@render section=s.op3 /></span>
			<@render section=s.use3 class="use" />
			<span class="is"><@bundlekey "search.label.is" /></span>
			<@textfield section=s.term3 autoSubmitButton="searchform-search" class="term" />
		</div>
	</div>
</div>

