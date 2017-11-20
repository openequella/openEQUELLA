<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<@css path="flickr.css" hasRtl=true />

<@a.div id="searchform" class="area">

	<div class="query-wrapper" role="search">
		<div id="querycontainer" class="input-append">
			<@textfield id="searchform-query" section=s.queryField 
				autoSubmitButton="searchform-search" 
				placeholder=b.key('query.hint') />
					
			<@button id="searchform-search" section=s.searchButton class="btn btn-primary add-on">
				<i class="icon-search icon-white"></i>
			</@button>
		</div>
		
		<#if s.textOrTagSelector.isDisplayed(_info)>
			<div id="withincontainer">
				<@render id="searchform-in" section=s.textOrTagSelector />
			</div>
		</#if>
	</div>

	<@render s.resetFiltersSection/>

</@a.div>
