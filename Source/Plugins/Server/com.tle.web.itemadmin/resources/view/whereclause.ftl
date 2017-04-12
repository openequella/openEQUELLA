<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl" />
	
<@css "whereclause.css" />
		
<div class="area">
	<h2>${b.key("whereclause.title")}</h2>
	<p>${b.key("whereclause.description")}</p>
	<@render section=s.div >
		<div class="criteria-wrapper">
			<div class="input text select criteria-1">
				<span class="criteria-label">${b.key("whereclause.addcriteria")}</span>
				<@render section=s.whereStart /> 
				<@textfield section=s.wherePath />	
				<@render section=s.browse />
			</div>
			<div class="input text select criteria-2">
				<@render section=s.whereOperator />	
				<@textfield section=s.whereValue  autoSubmitButton=s.add />	
			</div>
			<div class="addbutton">
				<@button section=s.add showAs="add" size="medium" />
			</div>
		</div>
		<@render section=s.whereClauses />
	</@render>
	<div class="button-strip">
		<@button section=s.search showAs="search" size="medium"/>
	</div>
</div>