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

<div class="button-strip">
	<@button section=s.cancelButton showAs="prev" />
</div>
