<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/textfield.ftl" />
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>
<#include "/com.tle.web.sections.standard@/list.ftl"/>


<@css path="lmsexport.css" hasRtl=true />

<@css "move-tree.css" />

<@div id="lms-tree-ajax">
	<div id="lmsexport">		
			<#if m.error??>
				<p class="error">${m.error}</p>
			<#else>
				<div class="filter-box">
					<@render section=s.filterBox class="filterBox" />
				</div>
				<!-- needs a margin on the bottom to stop the filter box floating over the locations div -->
				<h3 class="selectlocations">${b.key('export.label.selectlocations')}</h3>
				
				<div class="locations">
				
					<@div class="lms-tree-container" id="lms-tree-container">
						<@render section=s.folderTree class="lms-tree treeview-gray" />
						<div id="no-results" class="no-results">
							${b.key('export.label.filter.nocourses')}
						</div>
					</@div>
					
					<div class="toplevelselections">
						<div class="input checkbox">
							<@render s.showArchived />
						</div>
					</div>
				</div>
			</#if>
	</div>	
</@div>