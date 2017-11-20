<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.standard@/autocomplete.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl" />
<#include "/com.tle.web.sections.standard@/link.ftl" />
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>
		
<@a.div id="searchform" class="area">
	<div class="query-wrapper" role="search">
		
		<div id="querycontainer" class="input-append">
			<@autocomplete section=s.queryField 
				autoSubmitButton="searchform-search" 
				placeholder=b.key('query.hint') />
					
			<@button id="searchform-search" section=s.searchButton showAs="search" trait="primary" iconOnly=true />
		</div>
		
		<div id="withincontainer">
			<@render id="searchform-in" section=s.collectionList class="form-control" />
		</div>
		
		<div id="dropdowncontainer"></div>
		
		<@a.div id="wherecontainer">
			<@textfield section=s.currentHidden hidden=true />
			<#if m.showWhere>
				<div id="searchform-where">
					<ul>
						<#if m.criteria?has_content>
							<#list m.criteria as criteria>
								<li>${criteria?html}</li>
							</#list>
						<#else>
							<li>${b.key('query.nocriteria')}</li>
						</#if>
					</ul>
						<@button id="searchform-editquery" section=s.editQueryButton showAs="add"/>
						<#if m.criteria?has_content> 
							<@button section=s.clearQueryButton showAs="delete"/>
						</#if>		
				</div>
			</#if>
		</@a.div>
		
	</div>
	
	<@a.div id="wizardcontainer">
		<#if m.editQuery >
			<hr>
			<div id="wizard-controls" class="wizard-controls wizard-parentcontrol">
				<#if m.advancedControls??>
					<#list m.advancedControls['wizard-controls'] as control>
						<#if control.result??><@render control.result/><#t/></#if>
					</#list>
				</#if>
			</div>
			<@button id="searchform-advanced-search" section=s.doAdvancedButton showAs="search" size="medium" />
			<br clear="both">
		</#if>
	</@a.div>
	
	<@render s.resetFiltersSection/>
	<#if m.queryActions??>
		<div class="queryactions">
			<@renderList m.queryActions />
		</div>
	</#if>
</@a.div>
