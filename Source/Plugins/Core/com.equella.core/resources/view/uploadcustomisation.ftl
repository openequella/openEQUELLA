<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/file.ftl">
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<@css "uploadcustomisation.css" />

<div class="area">
	<h2>${b.key('customisation.title')}</h2>
	<p>
		<@setting label=b.key("upload")>
			<@file section=s.getUpload() size=30/>
			<@button section=s.uploadButton icon="upload" class="marginbutton" />
		</@setting>
	</p>
</div>