<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl" />
<#include "/com.tle.web.sections.standard@/radio.ftl" />
<#include "/com.tle.web.sections.standard@/checklist.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#import "/com.tle.web.sections.standard@/list.ftl" as l />

<@css path="kaltura.css" hasRtl=true />

<h3>${b.key("add.heading")}</h3>

<div id="kaltura-query">
	<div class="input text">
	    <label>${b.key("add.search.label")}</label>
		<@textfield section=s.query autoSubmitButton=s.search class="focus" /> <@button section=s.search showAs="search" size="medium" />
	</div>
</div>

<div class="modal-search-results">
	<#if m.searchPerformed && s.results.size(_info) == 0 >
		<p class="info">${b.key("add.search.noresults")}</p>
	</#if>
	<@l.boollist section=s.results; opt, state>
		<div class="modal-search-result kaltura">
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
			<p>
				<@render opt.date /> |
				<strong>${opt.views}</strong>
			</p>
		</div>
	</@l.boollist>
</div>

<@render s.pager />
