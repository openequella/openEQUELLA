<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.standard@/file.ftl"/>
<#include "/com.tle.web.sections.standard@/filedrop.ftl"/>

<@css path="file/file.css" hasRtl=true />

<div class="fileadd">
	<h3><@bundlekey "handlers.file.title"/></h3>
	
	<p><@bundlekey "handlers.file.prompt"/></p>
	<br>
	<div id="contribute-dnd-progress" class="filedrop-progress-container">
		<div class="clear"></div>
	</div>	
	<@div id="uploads">
	    <div class="uploadsprogress">
            <#list m.uploads as upload>
                <div class="file-upload">
                    <span class="file-name"><strong>${upload.filename?html}</strong></span>

                    <span class="file-upload-progress">
                        <@render section=upload.progressDiv class="progress-bar" />

                        <@render upload.remove />
                    </span>
                </div>
                <#if upload.problemLabel??>
                    <p class="ctrlinvalidmessage">${upload.problemLabel}</p>
                </#if>
            </#list>
        </div>

        <#-- need to find a way to focus this without it screwing up the styling -->
        <@file section=s.fileUpload renderBar=false class="focus" />

        <@filedrop section=s.fileDrop> </@filedrop>
	</@div>

	<#if m.canScrapbook>
		<div class="addlink">
			<@render section=s.filesFromScrapbookLink class="add" />
		</div>
	</#if>
	
	<#if m.warningLabel??>
		<p class="ctrlinvalidmessage">${m.warningLabel}</p>
	</#if>
	
	<#-- the problemLabel should include the filename -->
	<#if m.problemLabel??>
		<p class="ctrlinvalidmessage">${m.problemLabel}</p>
	</#if>
</div>