<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/textfield.ftl" />
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>
<#include "/com.tle.web.sections.standard@/list.ftl"/>
<#include "/com.tle.web.sections.standard@/dialog.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<@css path="lmsexport.css" hasRtl=true />


<div id="lmsexport">
	<h2><@bundlekey "export.title"/></h2>

	<h3>${b.key('export.label.addingto')}</h3>
	<#if m.singleConnectorName??>
		<p>${m.singleConnectorName}</p>
	<#else>
		<div class="input select">
			<@render s.connectorsList />
		</div>
	</#if>
	
	<@div id="lms-tree-ajax">
	
		<#if m.error??>
			<p class="error">${m.error}</p>
		
		<#elseif m.authRequired>
		
			<h3>${b.key('export.label.authorisationrequired')}</h3>
			<p>${b.key('export.text.authorisationrequired')}</p>
			
			<@dialog section=s.authDialog />
			<@button section=s.authDialog.opener size="medium"><@bundlekey "export.button.auth" /></@button>
		
		<#elseif m.connectorSelected>
		
			<h3>${b.key('export.label.selectresources')}</h3>
			
			<div class="attachments">
			
				<div class="toplevelselections">
					<div class="input checkbox">
						<@render s.selectSummary />
					</div>
					
					<#if m.contentPackage>
						<div class="input checkbox">
							<@render s.selectContentPackage />
						</div>
					</#if>
				</div>
				
				<div class="attcontainer">
					<ul class="attachments-browse ${s.showStructuredView?string("structured", "thumbs")}">
						<#list m.attachmentRows as attachmentRow>
							<@render attachmentRow.row />
						</#list>
					</ul>
				</div>
			</div>
			<@render s.selectAllAttachments />
		
			<div class="filter-box">
				<@render section=s.filterBox class="filterBox" />
			</div>
			<!-- needs a margin on the bottom to stop the filter box floating over the locations div -->
			<h3 class="selectlocations">${b.key('export.label.selectlocations')}</h3>
			
			<div class="locations">
			
				<@div class="lms-tree-container" id="lms-tree-container">
					<@render section=s.folderTree class="lms-tree treeview-gray" />
					<div id="no-results" class="no-results">
						<#if m.copyrighted>
							${b.key('export.label.filter.noactivations')}
						<#else>
							${b.key('export.label.filter.nocourses')}
						</#if>
					</div>
				</@div>
				
				<div class="toplevelselections">
					<div class="input checkbox">
						<@render s.showArchived />
					</div>
				</div>
			</div>
			
			
			<div class="button-strip">
				<@render s.publishButton />
			</div>

		</#if>
	
	</@div>
</div>