<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<div>
<h1>Pages</h1>
<ul class="pages">
	<#list 0..(m.totalPages - 1) as p>
		<li>
			<#if p = m.currentPage>
				${p}
			<#else>
				<a href="harvest.do?${pfx}currentPage=${p?c}">${p}</a>
			</#if>
		</li>			
	</#list>
</ul>
</div>

<div>
<h1>Items</h1>
<ul>
	<#list m.results as result>
		<li><a href="${result.url?html}"/><@bundle value=result.bundleId default=result.itemId /></a></li>
	</#list>
</ul>
</div>

<style>
	BODY
	{
		background-color: #FFFFFF;
	}
	H1
	{
		font-size: 20px;
	}
	DIV
	{
		margin: 3em;
	}
	UL.pages LI
	{
		display: inline;
	}
</style>