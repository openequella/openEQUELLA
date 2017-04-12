<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl" />
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>

<@css path="customlinks.css" hasRtl=true />

<@div id="linkListDiv" class="area">
	<h2>${m.heading}</h2>
	<#if !m.editing>
		<p>${b.key('links.ordering')}</p>
		<@render section=s.linkDiv>
			<ul id="cls_us">
				<#list m.links as link>
					<li class="ui-state-default" <#if link.iconUrl??>style="background-image: url('${link.iconUrl}')"</#if>>
						<div class="link-name">${link.name}</div>
						<div class="link-buttons">
							<@button section=link.edit showAs="edit" />
							<#if link.delete??><@button section=link.delete showAs="delete" /></#if>
						</div>
						<input type="hidden" value="${link.uuid}">
					</li>
				</#list>
			</ul>
		</@render>
		<div class="text-right">
			<@button section=s.addButton showAs="add" size="medium" />
		</div>
	</#if>	
</@div>
