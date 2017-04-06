<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.standard@/autocomplete.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl" />
<#include "/com.tle.web.sections.standard@/link.ftl" />
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<@a.div id="searchform" class="area">
	<div class="query-wrapper" role="search">
		<div class="input">
			<label for='q'>${b.key('query.search')}</label>
			<@autocomplete section=s.queryField autoSubmitButton="searchform-search" />
		</div>
		<@button showAs="search" id="searchform-search" section=s.searchButton />
	</div>	
	<@render s.resetFiltersSection/>
</@a.div>
