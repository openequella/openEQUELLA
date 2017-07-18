<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl" />
<#include "/com.tle.web.wizard.controls.universal@/common-edit-handler.ftl" />
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>

<@css "file/fileedit.css" />
<@css path="filelist.css" plugin="com.tle.web.sections.equella" hasRtl=true/>
<@css path="file/zip.css" hasRtl=true />

<@detailArea>
	<@editArea>
        <#if m.showThumbnailOption>
            <div class="settingRow">
                <div class="settingLabel"><@bundlekey "handlers.file.details.nothumbs"/></div>
                <div class="settingField">
                    <@render s.suppressThumbnails />
                </div>
            </div>
        </#if>
    </@editArea>
	
	<div>
		<div class="settingContainer">
			<#-- file conversion -->
			<#if m.showFileConversion>
				<div class="settingRow">
					<div class="settingLabel"><@bundlekey "handlers.file.details.label.fileconversion"/></div>
					
					<div class="settingField">
						<@render s.allowFileConversion />
					</div>
				</div>
			</#if>
			<div class="editLinks">
				<div class="settingRow">
					<div class="settingLabel"><@bundlekey "handlers.file.details.inplace.label.editfileoptions"/></div>
				
					<div class="settingField">
						
						<div class="editLink"><@render section=s.editFileLink class="editFileLink" /></div> 
						<div class="editFileWithLink"><@render section=s.editFileWithLink class="editFileLink" /></div>
												
						<@div id="editFileAjaxDiv" class="inplaceAppletDiv">
							<@render s.editFileDiv />
						</@div>
					</div>
				</div>
			</div>
			<#if m.zipFile>
			    <@div id="zipArea">
                    <#if !m.unzipped>
                        <@setting label="" section=s.executeUnzip/>
                    <#elseif m.unzipping>
                        <@setting label=b.key('handlers.file.zipdetails.label.zipprogress')>
                            <@render s.zipProgressDiv>
                                <div id="zipProgress" class="progress-bar"></div>
                            </@render>
                        </@setting>
                    <#else>
                        <@setting label="" section=s.removeUnzip/>
                        <@setting label=b.key('handlers.file.zipdetails.label.attachzip') section=s.attachZip />

                        <h4><@bundlekey "handlers.file.zipdetails.label.selectfiles"/></h4>

                        <div class="file-scroller">
                            <@render section=s.fileListDiv class="file-list">
                                <#list m.files as file>
                                    <#assign fileOrZeroLevel=0 />
                                    <#if !file.folder && file.level !=1 >
                                        <#assign fileOrZeroLevel=2 />
                                    </#if>
                                    <div class="${file.fileClass} ${(file_index % 2 == 0)?string("odd","even")} level${fileOrZeroLevel}" alt="${file.displayPath?html}" title="${file.path?html}">
                                        <@render file.check />
                                        <#if file.folder>
                                            ${file.displayPath?html}
                                        <#else>
                                            ${file.name?html}
                                        </#if>
                                    </div>
                                </#list>
                            </@render>
                        </div>
                        <@render s.selectAll/> | <@render s.selectNone />
                    </#if>
                </@div>
			</#if>
		</div>
	</div>
</@detailArea>

<br clear="both">