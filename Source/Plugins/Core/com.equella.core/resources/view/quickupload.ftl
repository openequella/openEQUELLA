<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/file.ftl">
<#include "/com.tle.web.sections.equella@/component/button.ftl">

<@css "quickupload.css"/>

<span class="quickuploadtitle">${b.key("quick.selectable.name", m.collectionName)}</span>

<div class="contribute-div">
	<@file section=s.fileUploader class="uploadfield" />
	<br>
	<div id="uploadProgress" class="uploadsprogress"></div>
</div>
