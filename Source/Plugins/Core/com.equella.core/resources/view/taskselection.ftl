<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/textarea.ftl"/>
<@css path="taskselection.css" />

<h3>${b.key("bulkop.movetask.title")}</h3> 

<div class ="content">
	<h4>${m.workflowName}</h4>
	<p>${b.key("bulkop.movetask.select")}</p>
	<div class="input select">
		<@render section=s.taskList />
	</div>
	<p>${b.key("bulkop.movetask.optional")}</p>
<@textarea section=s.commentField/>
</div>