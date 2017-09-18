<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/textarea.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>
<#include "/com.tle.web.sections.standard@/filedrop.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<@css path="taskaction.css" />

<div class="comment-form">
	<h3>${s.postCommentHeading} <#if s.mandatoryMessage><span class="mandatory">*</span></#if></h3>

	<@textarea section=s.commentField class="comment-entry" rows=3 />

	<div class="errorMsg">
        <#if m.errorMessage??>
            ${m.errorMessage}
        </#if>
    </div>

	<#if s.rejectSteps??>
	    <div class="rejectTo">
            <h3 class="noTopMargin">${b.key('comments.steps')}</h3>
            <@render section=s.rejectSteps class="reject-steps" />
        </div>
	</#if>

	<h3 class="noTopMargin">${s.attachedFilesLabel}</h3>

	<@filedrop section=s.fileDrop />

	<div id="current-uploads"></div>

	<@a.div id="uploaded" class="uploaded-container">
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