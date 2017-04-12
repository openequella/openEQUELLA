function disableReject($comment, $reject)
{
	if ($reject.length > 0)
	{
		if ($comment.val() == '')
		{
			$reject.attr('disabled','disabled');
		}
		else
		{
			$reject.removeAttr('disabled');
		}
	}
}