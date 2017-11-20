<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/textfield.ftl">
<#include "/com.tle.web.sections.equella@/component/button.ftl">
<#include "/com.tle.web.sections.standard@/file.ftl">
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>
<#include "/com.tle.web.sections.standard@/filedrop.ftl"/>
<#include "/com.tle.web.sections.standard@/link.ftl"/>
<#include "/com.tle.web.sections.standard@/list.ftl"/>
<#include "/com.tle.web.sections.standard@/dropdown.ftl"/>
<#include "/com.tle.web.sections.standard@/richdropdown.ftl"/>

<@css "myresourcecontribute.css" />
<@css "bulkupload.css"/>

<#if !m.editing>
	<p id="dndHelpTxt"><@bundlekey 'dnd.helptext'/></p>
	<div id="dndTagForm">
		<div class="control ctrlbody">
			<label for="${s.dndTagsField}">
				<h3><@bundlekey 'dnd.providetags'/></h3>
			</label>
			<@textfield section=s.dndTagsField />
		</div>
	</div>
	<div id="scrapbook-upload-progress" class="filedrop-progress-container">
		<div class="clear"></div>
	</div>
	<div id="dndfiles">
	</div>
	<div id="dndOptionsList">
		<div class="control ctrlbody">
			<label for="${s.archiveOptionsDropDown}">
				<h3><@bundlekey 'dnd.archiveoptionlabel'/></h3>
			</label>
			<@render s.archiveOptionsDropDown />
		</div>
	</div>
	<@filedrop section=s.fileDrop/>
</#if>

<h3>${m.singleTitle}</h3>

<#if m.editing>
<div class="editing">
	<#-- floated right -->
	<@div id="editFileAjaxDiv" class="inplaceAppletDiv">
		<#if m.loadApplet>
			<@render s.editFileDiv />
		</#if>
	</@div>

	<div class="icon">
		<@render m.thumbnail />
	</div>
	<div class="fileinfo">
		<div class="filename"><p>${m.filenameLabel}</p></div>
		<div class="editLinks">
			<div class="editLink"><@render section=s.editFileLink class="editFileLink" /></div>
			<div class="editFileWithLink"><@render section=s.editFileWithLink class="editFileLink" /></div>
		</div>
	</div>

	<div class="clear"></div>
</div>
</#if>

<div>
	<div class="control ctrlbody">
		<#if m.errorKey??><p class="ctrlinvalidmessage"><@bundlekey m.errorKey /></p></#if>

		<h3>${m.selectFileLabel}</h3>
		<@file section=s.fileUploader />
	</div>
</div>

<div>
	<div class="control ctrlbody">
		<label for="${s.descriptionField}">
			<h3><@bundlekey 'enterdescription'/></h3>
		</label>
		<@textfield section=s.descriptionField />
	</div>
</div>

<div>
	<div class="control ctrlbody">
		<label for="${s.tagsField}">
			<h3><@bundlekey 'providetags'/></h3>
		</label>
		<@textfield section=s.tagsField />
	</div>
</div>

<hr class="twentyspaced">

<div class="button-strip">
	<@button section=s.cancelButton showAs="prev" />
	<@button section=s.saveButton showAs="save"/>
</div>	