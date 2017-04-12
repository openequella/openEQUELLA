<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/textfield.ftl">

<#import "/com.tle.web.sections.standard@/ajax.ftl" as ajax/>

<@css path="mypageseditor.css" hasRtl=true />


<@ajax.div id="page-edit">
<#if m.showEditor>
<fieldset class="editor">
	<legend><@bundlekey value="editpage.label" /></legend>
	<div>
		<div class="control ctrlbody">
			<label for="${s.pageNameField}">
				<h3 class="ctrltitle"><@bundlekey value="pagetitle.label" /></h3>
			</label>
			<@textfield section=s.pageNameField class="pagename focus"  />
		</div>
		
		<div class="control ctrlbody">
			<h3 class="ctrltitle"><@bundlekey value="contents.label" /></h3>
			<@render m.editorRenderable />
		</div>
		
		<#if m.extraRenderable??>
			<@render m.extraRenderable />
		</#if>
	</div>
</fieldset>
</#if>
</@ajax.div>
