<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.standard@/autocomplete.ftl"/>
<#include "/com.tle.web.sections.standard@/link.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl" />
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<div id="itemadmin-page">

	<@a.div id="searchform" class="area" >
		<div class="query-wrapper" role="search">
		
		
			<div id="querycontainer" class="input-append">
				<@autocomplete section=s.queryField 
					autoSubmitButton="searchform-search" 
					placeholder=b.key('query.hint') />
						
				<@button id="searchform-search" section=s.searchButton class="btn btn-primary add-on">
					<i class="icon-search icon-white"></i>
				</@button>
			</div>
		
			<div id="withincontainer">
				<@render id="searchform-in" section=s.collectionList class="form-control" />
			</div>
			
			<#if m.whereShowing>
			<div id="wherecontainer">
				<div id="searchform-where">
					<ul>
						<#if m.criteria?has_content>
							<#list m.criteria as criteria>
								<li>${criteria?html}</li>
							</#list>
						</#if>
					</ul>
						<@button id="searchform-editquery" section=s.editQueryButton showAs="add"/>
						<#if m.criteria?has_content>
							<@button section=s.clearQueryButton showAs="delete" />
						</#if>						
				</div>
			</div>
			</#if>
			
			<@renderList m.sections />
		</div>
		
		<@render s.resetFiltersSection/>
		
		<#if m.queryActions??>
			<div class="queryactions">
				<@renderList m.queryActions />
			</div>
		</#if>
		
	</@a.div>
</div>
