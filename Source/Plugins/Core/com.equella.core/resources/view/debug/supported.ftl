<#macro paramlist list>
	<h2>${list?size} parameters</h2>
	<table border="1">
		<#list list as param>
				<tr>
				<td>${param.param?html}</td>
				<td>${param.type}</td>
				<td>
					<#if param.values?has_content>
						<ul>
							<#list param.values as val>
								<#if val??>
									<li>${val?html}</li>
								</#if>
							</#list>
						</ul>
					</#if>
				</td>
			</tr>
		</#list>
	</table>
</#macro>

<html>
	<body>
		<#if m.params??>
			<#assign params = m.params>
			<div id="supported">
				<h1>Supported</h1>
				<@paramlist params.supported/>
			</div>
			<div id="unsupported">
				<h1>Unsupported</h1>
				<@paramlist params.unsupported/>
			</div>
		<#else>
			<h1>No params</h1>
		</#if>
		<div id="trees">
			<h1>Section Trees</h1>
			<#list m.trees as tree><pre>${tree}</pre></#list>
		</div>		
	</body>
</html>
