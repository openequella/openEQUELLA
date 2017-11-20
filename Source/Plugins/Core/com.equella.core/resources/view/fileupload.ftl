<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.standard@/file.ftl"/>
<#include "/com.tle.web.sections.standard@/link.ftl"/>

<@css path="itemlist.css" plugin="com.tle.web.itemlist"/>
<@css "fileupload.css" />
<#assign TEMP_body>
<div class="modaldialog area">

	<#-- upload new file -->
	<h3><@bundlekey "upload.newfile" /></h3>
	
	<p>
		<@bundlekey "upload.filedescription" />
		<br>
		<@textfield section=s.fileName size=60 class="uploadfield" />
	</p>
	
	<h3><@bundlekey "upload.selectfile" /></h3>
	<#if m.noFile>
		<span class="mandatory"><@bundlekey "upload.error.mustselectfile" /></span><br>
	</#if>
	
	<@file section=s.fileUpload size=60 /><@render section=s.upload><@bundlekey "upload.upload"/></@render>
	
	
	
	<#-- display list of current attachments -->
	<br><br>
	
	<#if m.pages?size gt 0>
		<hr>
		<h2><@bundlekey "upload.currentattachments" /></h2>
		<br><br>
		<div class="attachmentcontainer">
			<#list m.pages as page>
				<div class="pagename"><span>${page.pageName}</span></div>				
			    <#list page.files as attachment>
			    	<div class="itemresult-wrapper">
						<div class="itemresult">
							<div class="itemresult-content">
								<h3><@render attachment.viewLink/></h3>
							</div>
						</div>
						<div class="itemresult-rating">
							&nbsp;
							<@link section=attachment.selectButton />
						</div>
					</div>
				</#list>
			</#list>
		</div>
	</#if>
</div>
</#assign>