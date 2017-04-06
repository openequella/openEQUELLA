<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/link.ftl">

<@css "topbar.css"/>

<div id="topbar"> 
	<div id="topbar-wrap"> 
		<div id="topbar-inner">
			<div id="topmenu"> 
				<#list m.links as l>
					<@render l />
				</#list>
				<@link section=s.editUserLink />
				<@link section=s.logoutLink />
			</div> 
		</div> 
	</div> 
</div>
<div id="header" role="banner"> 
	<div id="header-wrap"> 
		<div id="header-inner"> 
			<div class="badge">
				<a href="home.do"  title="${b.gkey("com.tle.web.sections.equella.header.badge.title")}">
					<@bundlekey "header.product" />
				</a>
			</div> 
			<div class="banner"><@render section=m.pageTitle /></div> 
		</div> 
	</div> 
</div>