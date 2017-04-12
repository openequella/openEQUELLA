<#ftl />
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>

<div class="navbar navbar-fixed-top navbar-inverse">
	<div class="navbar-inner">
		<div class="navbar-content">
			<#if m.titleLink??>
				<@render section=m.titleLink class="brand" />
			</#if>
			
			<#if m.left??>
				<ul class="nav pull-left">
					<#list m.left as l>
						<li<#if l.cssClass??> class="${l.cssClass}"</#if>>
							<#if l.renderable??><@render l.renderable /></#if>
						</li>
					</#list>	
				</ul>
			</#if>
			
			<#if m.middle??>
			<div class="centered-pills">   
				<ul class="nav nav-pills">
					<#list m.middle as mid>
						<li<#if mid.cssClass??> class="${mid.cssClass}"</#if>>
							<#if mid.renderable??><@render mid.renderable /></#if>
						</li>
					</#list>	
				</ul>
			</div>	
			</#if>
			
			<#if m.right??>
				<ul class="nav pull-right">
					<#list m.right as e>
						<li<#if e.cssClass??> class="${e.cssClass}"</#if>>
							<#if e.renderable??><@render e.renderable /></#if>
						</li>
					</#list>	
				</ul>
			</#if>
		</div>
	</div>
</div>

