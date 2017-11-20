<#ftl strip_whitespace=true />
<#import "/com.tle.web.sections.standard@/ajax.ftl" as ajax/>
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<@css "portletedit.css" />

<div class="area">
<h2>${m.pageTitle}</h2>
<@settingContainer wide=true>
	<@setting label=b.key('editor.label.title') section=s.title error=m.errors["title"] mandatory=true />

	<#if m.admin>
		<@setting label=b.key('editor.label.disabled') section=s.disabled/>
		<@setting label=b.key('editor.label.institutional') section=s.institutional help=b.key('editor.label.institutional.help')/>
		<@ajax.div id="institutionWideSettings" param_showEffect="blind" param_hideEffect="blind">
			<#if m.institutionWideChecked>
				<@setting label=b.key('editor.label.closeable') section=s.closeable/>
				<@setting label=b.key('editor.label.minimisable') section=s.minimisable/>
				<@setting label=b.key('editor.label.visibleto') help=b.key('editor.label.visibleto.help')>
					${m.expressionPretty}
					<@button section=s.selector.opener showAs="select_user"><@bundlekey "portlet.editor.expressionselector.button.change"/></@button>
				</@setting>
			</#if>
		</@ajax.div>
	</#if>

	<#if m.customEditor??><@render m.customEditor /></#if>
	
</@settingContainer>
	<div class="button-strip">
		<@button section=s.saveButton showAs="save" />
		<@button section=s.cancelButton showAs="cancel" />
	</div>
</div>