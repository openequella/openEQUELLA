<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<@css path="query.css" hasRtl=true />
<#-- FIXME: basically a copy of simplequery.ftl with a h2 on top.  could (should) be refactored -->

<div id="searchform" class="area" role="search">
	<h2><@bundlekey value="query.searching" params=[m.title]/></h2>
	
	<div class="query-wrapper" role="search">
		<div id="querycontainer" class="input-append">
			<@textfield section=s.queryField 
				autoSubmitButton="searchform-search" 
				placeholder=b.key('query.hint') />
					
			<@button id="searchform-search" section=s.searchButton class="btn btn-primary add-on">
				<i class="icon-search icon-white"></i>
			</@button>
		</div>
	</div>
</div>