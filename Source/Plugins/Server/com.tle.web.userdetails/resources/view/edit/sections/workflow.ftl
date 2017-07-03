<#include "/com.tle.web.freemarker@/macro/sections.ftl" />

<@css path="userdetails.css" hasRtl=true/>

<div class="edit">
	<h3>${b.key('workflow.title')}</h3>
	<p>${b.key('workflow.description')}</p>

	<div class="workflow_container">
		<@render section=s.workflowItemDefs class="col_checklist"/>
	</div> 
</div>