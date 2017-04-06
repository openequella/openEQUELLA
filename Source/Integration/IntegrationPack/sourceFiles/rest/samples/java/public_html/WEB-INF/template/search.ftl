<#include "page.ftl" />

<@page>
	<h1>Search</h1>

	<div class="fields">
		<form action="${urlContext}/search" method="POST" enctype="application/x-www-form-urlencoded">
			
			<fieldset>
				<legend>Search Details</legend>
				<div class="formfield">
					<label for="query">Freetext query</label>
					<div class="help">A simple text query.  E.g. <code>course*</code></div>
					<input type="text" id="query" name="query" value="${query}">
				</div>
				<div class="formfield">
					<label for="where">Where</label>
					<div class="help">Example:&nbsp;&nbsp;<code>where /xml/my/metadatanode like 'val%'</code></div>
					<input type="text" id="where" name="where" value="${where}">
				</div>
				<div class="formfield">
					<label for="showall">Include Non-Live Items?</label>
					<div class="help">Include non-LIVE items in the search results.  I.e. LIVE + DRAFT.</div>
					<input type="checkbox" id="showall" name="showall" <#if showall>checked="checked"</#if>>
				</div>
				<div class="formfield">
					<label for="sorttype">Sort Type</label>
					<div class="help">Order the results by</div>
					<select id="sorttype" name="sorttype">
						<option value="relevance" <#if sorttype == 'relevance'>selected="selected"</#if> >Search result relevance</option>
						<option value="modified" <#if sorttype == 'modified'>selected="selected"</#if> >Date modified</option>
						<option value="name" <#if sorttype == 'name'>selected="selected"</#if> >Item name</option>
						<option value="rating" <#if sorttype == 'rating'>selected="selected"</#if> >Item rating</option>
					</select>
				</div>
				<div class="formfield">
					<label for="reversesort">Reverse Sort?</label>
					<div class="help">Reverses the order of the Sort Type</div>
					<input type="checkbox" id="reversesort" name="reversesort" <#if reversesort>checked="checked"</#if>>
				</div>
				<div class="formfield">
					<label for="offset">Offset</label>
					<div class="help">The index of the first result to retrieve (zero based, i.e. zero is the first result).  E.g. if your search returns 200 results, you could retrieve results 50 to 100 using an Offset of 50 and a Maximum Results of 50.</div>
					<input type="text" id="offset" name="offset" value="${offset}">
				</div>
				<div class="formfield">
					<label for="maxresults">Maximum Results</label>
					<div class="help">The maximum number of results to return.</div>
					<input type="text" id="maxresults" name="maxresults" value="${maxresults}">
				</div>
			</fieldset>
			
			<div>
				<input type="submit" name="search" value="Search">
			</div>
		</form>
	</div>
	
	<#if results??>
		<div class="resultsContainer">
			<h2>Results ${start} to ${end} of ${available}</h2>
			
			<fieldset>
				<legend>Search Details</legend>
				
				<ul class="result">
				<#list results as result>
					<li><a href="${result.url}">${result.name}</a></li>
				</#list>
				</ul>
			</fieldset>
		</div>
	</#if>
</@page>