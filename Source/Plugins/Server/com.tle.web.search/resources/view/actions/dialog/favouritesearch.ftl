<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/textfield.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl" />

<@css path="dialog/favouritesearch.css" hasRtl=true />

<div class="favouritesearch input">
	<label for="${s.nameField}"><h3>${b.key("actions.favourite.dialog.desc")}</h3>
	<p>
		${b.key("actions.favourite.dialog.prompt")} 
		<span class="mandatorysymbol">${b.key("actions.favourite.dialog.mandatory.symbol")}</span>
	</p></label>
	<div class="control">
		<@textfield section=s.nameField autoSubmitButton=s.ok size=50 class="focus" />
	</div>
	<p class="mandatory">
		<span style="color: red;">${b.key("actions.favourite.dialog.mandatory.symbol")}</span>
		${b.key("actions.favourite.dialog.mandatory")}
	</p>
	
	<#if s.addButton??>
		<div class="addButton">
			<@button section=s.addButton showAs="save"/>
		</div>
	</#if>
</div>