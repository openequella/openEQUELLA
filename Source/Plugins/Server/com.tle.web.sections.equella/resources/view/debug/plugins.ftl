<style>
	.activated
	{
		color:green;
	}
</style>
<div class="area">
	Loaded plugins: ${m.loadedPlugins}<br>
	<table class="zebra">
		<#list m.dependencyRows as row>
			<#if row.loaded>
				<tr class="activated">
			<#else>
				<tr>
			</#if>
				<td>${row.name}</td>
				<td>${row.count}</td>
			</tr>
		</#list>
	</table>
	
	<table class="zebra">
		<#list m.extensionRows as row>
			<tr>
				<td>${row.name}</td>
				<td>${row.count}</td>
			</tr>
		</#list>
	</table>
	
	
	<table class="zebra">
		<#list m.report.items as item>
			<#if item.severity != 'INFO'>
				<tr>
					<td>${item.severity}</td>
					<td>${item.message}</td>
				</tr>
			</#if>
		</#list>
	</table>
</div>
