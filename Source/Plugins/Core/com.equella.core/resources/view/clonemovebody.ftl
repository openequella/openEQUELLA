<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/checklist.ftl" />
<#include "/com.tle.web.sections.standard@/dropdown.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl" />
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<@css path="cloneormove.css" hasRtl=true/>

<div class="selectcollection" id="mainDiv">
	<#if m.showCloneOptions>
		<div class="formsection">
			<label for="cloneOptions_fs">
				<h3><@bundlekey "selectcollection.label.selectclonetype" />
			</h3></label>
			<fieldset id="cloneOptions_fs" class="focus" tabIndex="-1">
				<@checklist section=s.cloneOptions class="cloneopts input checkbox" />
			</fieldset>
		</div>
	</#if>
	
	<#if m.allowCollectionChange>
		<div class="formsection">
			<label for="${s.collections}">
				<h3><@bundlekey "selectcollection.label.selectcollection" /></h3>
			</label>
			<@dropdown s.collections />
		</div>
	</#if>
	
	<@a.div id="collectionOptions">
		<#if m.submitLabel??>
			<div class="formsection">
				<@bundlekey m.submitLabel /><br>
				<@checklist section=s.submitOptions class="cloneopts input checkbox" />
			</div>
		</#if>
		
		<div class="formsection">
			<h3><@bundlekey "selectcollection.label.selectoptionalxslt" /></h3>
			<@dropdown s.schemaImports />
		</div>
	</@a.div>
	
	<#if m.schemaChanged && m.sourceSchema?? && m.destSchema??>	
		<div class="formsection">
			<div class="infobox">
				<@bundlekey value="selectcollection.info.collectiontransfer" params=[b.bundle(m.sourceSchema.name), b.bundle(m.destSchema.name)] />
			</div>
		</div>
	</#if>
	
	<br>

	<#if !s.forBulk>	
		<div class="button-strip">
			<@button section=s.proceedButton showAs="save" />
		</div>
	</#if>
</div>