<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl" />
<#include "/com.tle.web.wizard.controls.universal@/common-edit-handler.ftl" />
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>

<@css "file/fileedit.css" />

<@detailArea>
	<@editArea />
	
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
			<#if m.showThumbnailOption>
				<div class="settingRow">
					<div class="settingLabel"><@bundlekey "handlers.file.details.nothumbs"/></div>
					<div class="settingField">
						<@render s.suppressThumbnails />
					</div>
				</div>
			</#if>
		</div>
	</div>
</@detailArea>

<br clear="both">