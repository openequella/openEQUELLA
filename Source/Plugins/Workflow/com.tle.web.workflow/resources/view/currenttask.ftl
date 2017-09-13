<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/link.ftl">
<#include "/com.tle.web.sections.equella@/macro/receipt.ftl">

<@css path="moderate.css" hasRtl=true/>
<@css path="comments.css" hasRtl=true/>

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
		<span class="moderate-label">${b.key('moderate.showcomments', m.comments?size)}</span> (<@link s.postButton/>)
	</div>

	<#if m.comments?size gt 0>
		<div id="moderation-comments">
			<div>
				<#list m.comments as comment>
					<div class="comment ${comment.extraClass}">
						<div class="comment-username">
							<@render comment.user/>
							<@render section=comment.dateRenderer class="comment-date"/>
						</div>
						<#if comment.taskname??>
							<div class="comment-task">${b.key('comments.taskname')}: ${comment.taskName}</div>
						</#if>
						<#if comment.message??>
							<div class="comment-content">
								<p>${comment.message?html?replace("\n", "<br>")}</p>
							</div>
						</#if>
						<#if comment.attachments??>
						<ul>
						<#list comment.attachments as filelink>
							<li><@render filelink/></li>
						</#list>
						</ul>
						</#if>
					</div>
				</#list>

				<#--
				<#list m.comments as comment>
					<div class="moderation-comment ${comment.extraClass}">
						<div class="moderate-label"><@render comment.user /> (<@render comment.dateRenderer />)</div>
						<div class="moderation-comment-message">${comment.message}</div>

						<#if comment.attachments?size gt 0>
						<ul class="moderation-comment-attachments">
							<#list comment.attachments as attachment>
								<li><@render attachment /></li>
							</#list>
						</ul>
						</#if>
					</div>
				</#list>
				-->
			</div>
		</div>
	</#if>

	<#if m.dialogs??><@render m.dialogs/></#if>
</div>
