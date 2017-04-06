<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.wizard.controls.universal@/common-edit-handler.ftl" />



<@css path="filelist.css" plugin="com.tle.web.sections.equella" hasRtl=true/>
<@css path="file/zip.css" hasRtl=true />
<@css path="file/file.css" hasRtl=true />

<@detailArea>
	<@editArea>
		<@setting label=b.key('handlers.file.zipdetails.label.attachzip') section=s.attachZip />
		<#if m.showZipPreview >
			<@setting label=b.key('handlers.abstract.preview') section=s.previewCheckBox />
		</#if>
		<#if m.warning >
			<p class="ctrlinvalidmessage">
				<label>${m.warningMsg}</label>
			</p>
		</#if>
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

	</@editArea>
</@detailArea >
