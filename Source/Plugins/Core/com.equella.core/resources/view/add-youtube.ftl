<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl" />
<#include "/com.tle.web.sections.standard@/radio.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl" />
<#import "/com.tle.web.sections.standard@/list.ftl" as l />

<@css path="youtube.css" hasRtl=true />

<h3>${b.key("add.heading")}</h3>

<div id="youtube-query">

<div class="input text">
    <label for=${s.query}>${b.key("add.search.channel.search")}</label>
	<@textfield section=s.query autoSubmitButton=s.searchButton class="focus" />
	<@button section=s.searchButton showAs="search" size="medium" />
</div>	
	
<#if s.channelList.listModel??>
<div class="input select">  
   <label>${b.key("add.search.channel")}</label>
   <@render section=s.channelList style="position:absolute;left:50px;" />
</div>
</#if>

</div>


<div class="modal-search-results">
	<#if m.error??>
		<p class="info error">${m.error}</p>
	<#elseif m.noResult?? && m.noResult>
		<p class="info results">${b.key("add.search.noresults")}</p>
	</#if>
	<@l.boollist section=s.results; opt, state>
		<div class="modal-search-result youtube">
			<@render state />
			
			<#if opt.thumbnail??>
				<@render opt.thumbnail />
			</#if>
			<label for=${state.id} >	
				<h4><@render opt.link /></h4>
				<#if opt.description??>
					<p>${opt.description}</p>
				</#if>
			</label>
			<p>
				${opt.author} |
				<@render opt.date /> | 
				<strong>${opt.views}</strong>
			</p>
		</div>
	</@l.boollist>
</div>
