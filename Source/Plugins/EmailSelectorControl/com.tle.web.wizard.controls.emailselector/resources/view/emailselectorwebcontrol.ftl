<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#include "/com.tle.web.sections.standard@/dialog.ftl"/>
<#include "/com.tle.web.sections.standard@/dropdown.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>

<@css "emailselector.css" />

<@div id="${id}emailControl">

	<#if m.warning??>
		<div class="noemailwarning"><@bundlekey m.warning /></div>
	</#if>

	<div class="input">
		<@textfield section=s.email autoSubmitButton=s.addEmailButton class="emailfield" />
		<#if m.allowAdd>
			<@button section=s.addEmailButton showAs="email" size="medium" />
		</#if>
	</div>
	
	<@dialog section=s.selectUserDialog class="selectuserdialog" />
	<#if m.allowAdd>
		<div class="input">
			<@button section=s.selectUserDialog.opener showAs="select_user" size="medium"><@bundlekey "button.selectuser" /></@button>
		</div>
	</#if>

	<@render section=s.selectedTable />
</@div>