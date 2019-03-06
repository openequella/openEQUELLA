function resizeWindow()
{
	newWidth=0; newHeight=0;
	window.moveTo(0,0);
	if(document.getElementsByTagName('object')) {
	  for(i=0; i<document.getElementsByTagName('object').length; i++) {
	    if(parseInt(document.getElementsByTagName('object')[i].getAttribute('width'))+60 > newWidth) newWidth = parseInt(document.getElementsByTagName('object')[i].getAttribute('width'))+60; 
	    newHeight += parseInt(document.getElementsByTagName('object')[i].getAttribute('height'));
	  } 
	  newHeight += 350;
	}
	if(newWidth<420) newWidth=420;
	if(newHeight>screen.height) newHeight=screen.height-30;
	if(newHeight<460) newHeight=460;
	window.resizeTo(newWidth, newHeight);
}

window.onload = resizeWindow;

function closeWindow()
	{
	self.close();
	opener.focus();
	}