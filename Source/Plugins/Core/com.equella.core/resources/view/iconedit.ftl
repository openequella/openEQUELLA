<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/file.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<@css "iconedit.css" />

<#if m.displayIconUrl??>
	<@setting label=b.key('editicon.label.current')>
		<img src="${m.displayIconUrl}" alt="" >
		<#if m.hasCustomIcon>
			<@button section=s.removeIconButton showAs="delete"><@bundlekey "editicon.removeicon"/></@button>
		</#if>
	</@setting>
</#if>

<br>
<@setting label=b.key('editicon.uploadnew')>
	<@file s.iconUpload/>&nbsp;<@button section=s.uploadButton icon="upload" class="uploadButton">${b.key("editicon.uploadbutton")}</@button>
</@setting>

<#if m.errorKey??>
	<@setting label="" error=b.key("error." + m.errorKey) />
</#if>
