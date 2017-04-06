<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<@a.div id="searchform" class="area" >
	<div class="query-wrapper" role="search">
		
		<div id="querycontainer" class="input-append">
			<@textfield section=s.queryField autoSubmitButton="searchform-search" />
			
			<@button id="searchform-search" section=s.searchButton class="btn btn-primary add-on">
				<i class="icon-search icon-white"></i>
			</@button>
		</div>
		
		<div id="withincontainer">
			<@render id="searchform-in" section=s.connectorList />
		</div>	
	</div>
	
	<@render s.resetFiltersSection/>
	<#if m.queryActions??>
		<div class="queryactions">
			<@renderList m.queryActions />
		</div>
	</#if>
</@a.div>