<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/textarea.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>
<#include "/com.tle.web.sections.standard@/filedrop.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<@css path="taskaction.css" />

<div class="comment-form">
	<h3>${s.postCommentHeading}</h3>

	<@textarea section=s.commentField class="comment-entry" rows=3 />

	<#if s.rejectSteps??>
		<h3>${b.key('comments.steps')}</h3>
		<@render section=s.rejectSteps class="reject-steps" />
	</#if>

	<h3>${s.attachedFilesLabel}</h3>

	<@filedrop section=s.fileDrop />


	<div id="current-uploads"></div>

	<!--<h3>${b.key('comments.uploaded')}</h3>-->
	<@a.div id="uploaded" class="uploaded-container">
        <#if m.errorMessage??>
            <div class="errorMsg">${m.errorMessage}</div>
        </#if>
		<div class="uploaded">
			<#list m.stagingFiles as f>
				<div id="sf_${f?index}" class="file-upload">
					<span class="file-name"><strong><@render m.stagingFiles[f?index] /></strong></span>

					<span class="file-upload-cancel">

						<@render m.deleteFiles[f?index] />
					</span>
				</div>
			</#list>
		</div>
	</@a.div>
</div>