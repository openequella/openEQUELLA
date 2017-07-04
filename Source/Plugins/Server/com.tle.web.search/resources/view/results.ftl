<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a />
<#include "/com.tle.web.sections.equella@/renderer/linklist.ftl"/>

<div id="searchresults-outer-div">
	<@a.div id="searchresults-header-cont">
		<#if m.canonicalUrl??>
			<input type="hidden" id="searchUrl" value="${m.canonicalUrl}">
		</#if>

		<#if m.showResults>
			<div class="searchresults-header">
				<#if m.showResultSelection>
					<@linklist id="result-type-select" section=m.resultSelectionMenu/>
				<#else>
					<span>${m.resultsTitle}</span>
				</#if>
				<#if m.resultsAvailable>
					<div id="searchresults-stats">${m.resultsText}</div>
				</#if>
			</div>
		</#if>
	</@a.div>

	<#if m.showResults>
		<@render m.actions/>
	</#if>

	<@a.div id="searchresults-cont">
		<@render s.cssInclude/>

		<#if m.showResults>
			<@a.div id="searchresults" class="searchresults area">
				<#if m.resultsAvailable>
					<@render m.itemList/>
					<@render s.paging.pager/>
				<#else>
					<#if m.errored>
						<div class="searchError">
							<h3>${m.errorTitle}</h3>

							<#if m.errorMessageLabels??>
								<p>
									<#list m.errorMessageLabels as errorLabel>
										${errorLabel}<br>
									</#list>
								</p>
							</#if>
						</div>
					<#else>
						<h3>${m.noResultsTitle}</h3>
						<#if m.suggestions??>${m.suggestions}</#if>
					</#if>
				</#if>
				<#if m.footer??><@render m.footer/></#if>
			</@a.div>
		</#if>
	</@a.div>
</div>
