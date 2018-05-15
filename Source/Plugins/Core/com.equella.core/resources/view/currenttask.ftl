<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/link.ftl">
<#include "/com.tle.web.sections.equella@/macro/receipt.ftl">

<@css path="moderate.css" hasRtl=true/>

<div id="moderate" class="area content">

	<@receipt m.receipt />

	<div class="clearfix">
		<h2 class="task-name">${m.taskName}</h2>

		<div id="moderate-controls">
			<@link section=s.prevButton/>
			<@link section=s.listButton/>
			<@link section=s.nextButton/>
		</div>
	</div>

	<#if m.taskDescription??>
		<div class="task-description">${m.taskDescription}</div>
	</#if>

	<div class="details">
		<div class="detail">
			<span class="detail-label">${b.key('moderate.assignedto')}</span> <@render m.assignedTo /> (<@link section=s.assignButton/>)
		</div>

		<div class="detail">
			<span class="detail-label">${b.key('moderate.submittedby')}</span> <@render m.submittedBy/> ${b.key('moderate.formoderationby')} <@renderList list=m.moderators separator=", "/>
			<#if s.showAllModsButton.isDisplayed(_info)>(<@link s.showAllModsButton/>)</#if>
		</div>
	</div>

	<div class="comments-label">
		<span class="moderate-label">${b.key('moderate.showcomments', m.commentsSize)}</span> (<@link s.postButton/>)
	</div>

	<#if m.comments??>
		<div id="moderation-comments">
		    <@render m.comments/>
		</div>
	</#if>

	<#if m.dialogs??><@render m.dialogs/></#if>
</div>
