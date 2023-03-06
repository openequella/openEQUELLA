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

<h3>${s.editTitle}</h3>

<div class="editing">
	<div class="icon">
		<@render m.thumbnail />
	</div>
	<div class="fileinfo">
		<div class="filename"><p>${m.filenameLabel}</p></div>
	</div>

	<div class="clear"></div>
</div>

<div>
	<div class="control ctrlbody">
		<#if m.errorKey??><p class="ctrlinvalidmessage"><@bundlekey m.errorKey /></p></#if>

		<h3>${s.editFileLabel}</h3>
        <@render s.fileUploader/>
        <div id="uploadProgress" class="uploadsprogress"></div>
        <br>
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
