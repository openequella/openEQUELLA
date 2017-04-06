function select($treeUl, $nodeDiv, func, id)
{
	$treeUl.find('.cachingNode').removeClass('selected');
	$nodeDiv.addClass('selected');
	func(id);
}