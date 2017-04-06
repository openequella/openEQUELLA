<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<@css "merlotquery.css" />

<div id="searchform" class="area" role="search">
	<h2><@bundlekey value="search.query.searching" params=[m.title]/></h2>
	
	<div class="query-wrapper">
		<div id="querycontainer" class="input-append">
			<@textfield section=s.queryField autoSubmitButton="searchform-search" class="term" placeholder=b.key('query.hint') />
			
			<@button id="searchform-search" section=s.searchButton class="btn btn-primary add-on">
				<i class="icon-search icon-white"></i>
			</@button>
		</div>
	
		<div class="merlotwithin">
			<label>${b.key("filter.community.title")}</label>
			<@render s.communities />
		</div>
		
		<div class="merlotwithin">
			<label>${b.key("filter.materialtype.title")}</label>
			<@render s.materialTypes />
		</div>
	
		<div class="merlotwithin">
			<label>${b.key("filter.category.title")}</label>
			<@render s.categories />
		</div>
		
		<@a.div id="subcategories">
			<#if m.categorySelected>
				<div class="merlotwithin">
					<label>${b.key("filter.subcategory.title")}</label>
					<@render s.subcategories />
				</div>
			</#if>
		</@a.div>
	</div>
</div>
