<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/textarea.ftl"/>
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<@css path="comments.css" hasRtl=true/>

<div id="comments-list">
	<h3>${m.commentHeading}</h3>

	<#list m.comments as comment>
		<div class="comment ${comment.extraClass}">
			<div class="comment-username">
				<@render comment.user/>
				<@render section=comment.dateRenderer class="comment-date"/>
			</div>
			<div class="comment-task">${b.key('comments.taskname')}: ${comment.taskName}</div>
			<#if comment.message??>
				<div class="comment-content">
					<p>${comment.message?html?replace("\n", "<br>")}</p>
				</div>
			</#if>
		</div>
	</#list>
</div>
