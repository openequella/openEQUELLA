<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/textarea.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>
<#include "/com.tle.web.sections.standard@/filedrop.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<div class="area workflowcomments">
	<@css path="comments.css" hasRtl=true/>

	<h2>${m.pageTitle}</h2>

	<#if m.postAllowed>
		<@settingContainer wide=true>
			<@setting label=m.postCommentHeading mandatory=m.mandatory error=m.errorMessage labelFor=s.commentField>
				<@textarea section=s.commentField/>
			</@setting>
			<#if m.displayUploadButton>
				<@setting label="" rowStyle="dndUploadRow">
					<@a.div id="uploaded">
                        <#list m.stagingFiles as f>
                            <li id="sf_${f?index}"><@render m.stagingFiles[f?index] /> <@render m.deleteFiles[f?index] /></li>
                        </#list>
					</@a.div>
					<div id="current-uploads"></div>
					<@filedrop section=s.fileDrop id="workflowmessagednd"/>
				</@setting>
			</#if>
			<#if s.rejectSteps.isDisplayed(_info)>
				<@setting label=b.key('comments.steps') section=s.rejectSteps mandatory=true/>
			</#if>
			<div class="button-strip">
				<@button section=s.submitButton showAs="save" />
				<@button s.cancelButton/>
			</div>
		</@settingContainer>
	</#if>

	<@render s.viewCommentsSection/>

	<#if !m.postAllowed>
		<@button section=s.closeButton class="float-right" size="medium"/>
	</#if>
</div>