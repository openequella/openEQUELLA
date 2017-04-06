<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/link.ftl">
<#include "/com.tle.web.sections.equella@/macro/receipt.ftl">

<div id="moderate">
<@css path="moderate.css" hasRtl=true/>
	
	<@receipt m.receipt />

	<div class="float-left content-left">
		<h3>${b.key('moderate.task')}: <span class="red">${m.taskName}</span></h3>
		<p>${m.taskDescription}</p>
		<#assign submittedBy><@render m.submittedBy/></#assign>
		<p>${b.key('moderate.submittedby', [submittedBy])} <@renderList list=m.moderators separator=", "/>
		<#if s.showAllModsButton.isDisplayed(_info)>(<@link s.showAllModsButton/>)</#if><br/>
		${b.key('moderate.assignedto')}: <@render m.assignedTo /> (<@link class="small" section=s.assignButton/>)
		</p>
	
		<p class="small"><@link section=s.showButton class="comments"/> | <@link s.postButton/></p>
		<#if m.dialogs??><@render m.dialogs/></#if>
	</div>
	
	<div id="mod_right_box" class="float-right">
		<@render id="moderate-reject" section=s.rejectButton class="float-left"/>
		<@render id="moderate-approve" section=s.approveButton class="float-right"/>
		
		<div id="moderate-controls">
			<@link class="float-left" section=s.prevButton/>
			<@link class="float-right" section=s.nextButton/>
			<@link class="task-link" section=s.listButton/>
		</div>
	</div>
</div>
