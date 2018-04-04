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
	},
	affixDiv: function() {
	    var ad = $("#affix-div");
	    var offset = (ad.offset().top) - 55;
	    ad.attr("data-offset-top",offset, "data-spy", "affix");
	}
};