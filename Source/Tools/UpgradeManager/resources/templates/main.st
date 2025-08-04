main(timeout, tabIndex, managerVersion, version, buttonAction) ::= <<
$page(
script={
  function updateStatus() {
    \$.getJSON('/pages/ajaxstatus', function(data) {
      \$('#ajaxstatus').empty().append(data[0]);
      \$("#mainform").attr("action", data[1]);
      setTimeout(updateStatus, $timeout$);
    \});
  \}

	function deploy() {
    var v = \$('.version-item:checked');
    if( v.size() == 0 ) {
      alert('You must select a version above');
      return false;
    \}

    var results = confirm('Are you sure you want to deploy version ' + v.val() + '? This will stop the EQUELLA App Server service.');
    if( results != true ) {
      return false;
    \}

    //find out which version was selected?
    var checked;
    \$('.version-item').each(function() {
      if( this.checked )
        checked = this;
    \});
		document.location = '/deploy/deploy/' + checked.value;
	\}

	\$(function() {
		\$('#deploy').tabs({
			fx: {
				height: 'toggle',
				opacity: 'toggle'
			\},
			selected: $tabIndex$
		\});
		\$('#deploy').tabs('option', 'selected', $tabIndex$);

		updateStatus();
	\});
},
content={
  <form id="mainform" action="$buttonAction$" method="get">
   <dl id="statusversion">
     <dt>Deployed Version:</dt>
     <dd>$version.displayName$ <span style="color: #AAAAAA;"> ($version.semanticVersion$) </span></dd>
     <dt>Server Status:</dt>
     <dd id="ajaxstatus">...</dd>
   </dl>
  </form>
  <div id="deploy">
   <ul class="ui-tabs-nav">
     <li class="ui-tabs-selected"><a class="" href="/pages/versions"><span>Downloaded Versions</span></a></li>
     <li class=""><a href="/pages/other"><span>Get Other Versions</span></a></li>
   </ul>
  </div>
},
managerVersion=managerVersion)$
>>
