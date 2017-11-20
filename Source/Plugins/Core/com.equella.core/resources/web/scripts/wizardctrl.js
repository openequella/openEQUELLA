var WizardCtrl = {
	setMessage : function(ctrlid, message)
	{
		var $ctrl = $("#" + ctrlid);
		var $content = $ctrl.children("div:first-child");
		var $msg = $content.children("p.ctrlinvalidmessage");
		if (!message)
		{
			$content.removeClass("ctrlinvalid");
			$msg.empty();
		}
		else
		{
			$content.addClass("ctrlinvalid");
			$msg.html(message);
		}
	}
};