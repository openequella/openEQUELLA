<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl" />
<#import "/com.tle.web.sections.standard@/list.ftl" as l />

<@css path="googlebook.css" hasRtl=true/>

<h3>${b.key("gbook.add.heading")}</h3>

<div id="googlebook-query" class="input text">
	<label for="${s.query}">${b.key("add.search.label")}</label>
	<@textfield section=s.query class="bookquery focus" autoSubmitButton=s.searchButton />
	<@button section=s.searchButton showAs="search" size="medium" />
</div>

<div id="googlebook-results">
	<@l.boollist section=s.results; opt, state>
		<div class="googlebook-result">
			<@render state />
			
			<#if opt.thumbnail??>
				<@render opt.thumbnail />
			</#if>
			<label for="${state.id}">
				<h4><@render opt.link /></h4>
				<#if opt.description??>
					<p>${opt.description}</p>
				</#if>
			</label>
			<p>${opt.author}</p>
		</div>
	</@l.boollist>
</div>

<@render s.pager />

