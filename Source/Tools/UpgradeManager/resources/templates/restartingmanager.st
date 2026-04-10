restartingmanager() ::= <<
$page(
  script={
    window.setTimeout('document.location = "/pages/"', 10000);
  }, content={
    <p>Restarting the openEQUELLA Manager. Please wait a few seconds...</p>
  }
)$
>>
