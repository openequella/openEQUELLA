<#ftl strip_whitespace=true />

<#import "/com.tle.web.sections.standard@/ajax.ftl" as ajax/>

<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl" />
<#include "/com.tle.web.sections.standard@/textfield.ftl" />
<#include "/com.tle.web.sections.standard@/list.ftl"/>

<@css path="popupbrowser.css" hasRtl=true/>

<@render s.selectTermFunc />

<div id="container" >
	<div id="termChooser" class="ui-tabs">
		<ul>
			<#if s.browseEnabled >
				<li><a href="#browsetab" class="focus"><span>${b.key('wizard.popupbrowser.browsemode')}</span></a></li>
			</#if>
			<#if s.searchEnabled >
				<li><a href="#searchtab" <#if !s.browseEnabled>class="focus"</#if>><span>${b.key('wizard.popupbrowser.searchmode')}</span></a></li>
			</#if>
		</ul>
		<#if s.browseEnabled >
			<div id="browsetab" class="popupbrowser-tab ui-tabs-hide">
				<@render s.treeView />
			</div>
		</#if>
		<#if s.searchEnabled >
			<div id="searchtab" class="popupbrowser-tab ui-tabs-hide">
				<@textfield section=s.searchQuery class="searchquery" autoSubmitButton=s.searchButton />
				<@button section=s.searchButton showAs="search" size="medium" />
				<@ajax.div id="searchResults">
					<#if m.searchExecuted>
						<br>
						<div class="resultcount">
							<#if m.searchTotalResults lt 0>
								${b.key('wizard.popupbrowser.searchcountunknowntotal', m.searchResults?size)}
							<#else>
								${b.key('wizard.popupbrowser.searchcountwithtotal', m.searchResults?size, m.searchTotalResults)}
							</#if>
						</div>
						<ul>
							<#list m.searchResults as sr>
								<li><@render section=sr /></li> 
							</#list>
						</ul>
						<#if m.showAllResults?? >
							<div class="showallresults">
								<@render m.showAllResults />
							</div>
						</#if>
					</#if>
				</@ajax.div>
			</div>
		</#if>
	</div>
	
	<@ajax.div id="selectedTerms">
		<h3><@bundlekey "currentlyselectedstuff.current" /></h3>
		<div class="selectedItems">
			<@render section=s.selectedTermsTable />
			<script>
				var rawUl = $('#selectedTerms table').get(0);
				rawUl.scrollTop = rawUl.scrollHeight;
			</script>
		</div>
	</@ajax.div>
</div>