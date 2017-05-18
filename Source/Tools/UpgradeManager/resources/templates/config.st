<script type="text/javascript">
\$(function(){

\$('#saveButton').click(function()
{
	\$.post('/pages/config/', {
				username: \$('#username').val(),
				password: \$('#password').val(),
				proxhost: \$('#proxhost').val(),
				proxport: \$('#proxport').val(),
				proxusername: \$('#proxusername').val(),
				proxpassword: \$('#proxpassword').val(),
				'action-saveconfig': 'save'
			},
			function(data)
			{
				\$('#config-div').html(data);
			}
		);
		alert("Successfully Saved!");
}
);

});
</script>

<div id="config-div">

$if(error)$
	<span class="error">$error$</span>
$endif$

<form id="configForm" method="post" enctype="multipart/form-data" action="/pages/config/">
	<fieldset>
		<legend>Upgrade Server Authentication</legend>

		$("templates/heading-content")(heading="Username", content={
			<input type="text" id="username" name="username" value="$username$">
		})$
		$("templates/heading-content")(heading="Password", content={
			<input type="password" id="password" name="password" value="$password$">
		})$
	</fieldset>

	<fieldset>
		<legend>Connection Proxy Details</legend>

		$("templates/heading-content")(heading="Proxy Host", content={
			<input type="text" id="proxhost" name="proxhost" value="$proxhost$">
		})$
		$("templates/heading-content")(heading="Proxy Port", content={
			<input type="text" id="proxport" name="proxport" value="$proxport$">
		})$
		$("templates/heading-content")(heading="Proxy Username", content={
			<input type="text" id="proxusername" name="proxusername" value="$proxusername$">
		})$
		$("templates/heading-content")(heading="Proxy Password", content={
			<input type="password" id="proxpassword" name="proxpassword" value="$proxpassword$">
		})$

	</fieldset>

	<input id="saveButton" type="button" class="button" value="Save">
</form>

</div>