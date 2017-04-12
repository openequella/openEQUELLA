<#include "page.ftl" />

<@page>
	<h1>Contribute</h1>
			
	<div class="fields">
	
		<form action="${urlContext}/edit" method="POST" enctype="multipart/form-data">
			<fieldset>
				<legend>Resource Details</legend>
				
				<div class="formfield">
					<label for="name">Name</label>
					<div class="help">Name of the resource</div>
					<input type="text" id="name" name="name" value="${name}">
				</div>
				
				<div class="formfield">
					<label for="name">Description</label>
					<div class="help">Description of the resource</div>
					<input type="text" id="description" name="description" value="${description}">
				</div>
				
				<div class="formfield">
					<label for="attachments">Attachments</label>
					<div class="help">Attachments on the resource</div>
					<input type="file" id="file0" name="file0">
					<input type="file" id="file1" name="file1">
					<input type="file" id="file2" name="file2">
					<input type="file" id="file3" name="file3">
					<input type="file" id="file3" name="file4">					
				</div>
				
			</fieldset>
			
			<div>
				<input type="submit" name="save" value="Save">
			</div>
		</form>
	</div>
</@page>