ajaxstatus(statusClass, statusText, buttonText) ::= <<
<span class="$statusClass$">$statusText$</span>
$if(buttonText)$<input type="submit" value="$buttonText$" />$endif$
>>
