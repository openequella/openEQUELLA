<#include "/com.tle.web.freemarker@/macro/sections.ftl" />

<@css path="userdetails.css" hasRtl=true/>

<div class="edit">
	<h3>${b.key('workflow.title')}</h3>
	<p>${b.key('workflow.description')}</p>

	<div class="workflow_container">
		<@render section=s.workflowItemDefs class="col_checklist"/>
	</div> 
</div>

<div class="edit">
	<h3>${b.key('mylive.title')}</h3>

    <div class="input checkbox">
		<@render id="myLiveItems" section=s.myLiveItems />
		<label for="myLiveItems">${b.key('mylive.check')}</label>
	</div>
</div>