var selectedGroupingID = "grouping0";

function setupClick(id, $hidden, newGrouping)
{
	$('#' + id).click(function(){
		$('li.select').removeClass('selected');
		$(this).addClass('selected');
		var id = $(this).attr('id');
		$hidden.val(id);
		selectedGroupingID = id;
	});
	
	$(document).ready(function(){
		$('#' + selectedGroupingID).addClass('selected');
	});
	
	if(newGrouping)
	{
		$('li.select').removeClass('selected');
		$hidden.val(id);
		$('#' + id).addClass('selected');
	}
	
	$('#myTab a').click(function (e) {
		e.preventDefault();
		$(this).tab('show');
	});
}

function selectResult($link, select, resultId)
{	
	var position = $('#'+selectedGroupingID).siblings().last();
	if(position.length > 0)
	{
		$link.parents('li').effect('transfer',  {to: $('#'+selectedGroupingID).siblings().last()},500);	
	}
	else
	{
		$link.parents('li').effect('transfer',  {to: $('#'+selectedGroupingID)},500);	
	}
	select(resultId);
}

function selectResults(select)
{
	$('#search-result-list').effect('transfer',  {to: $('#'+selectedGroupingID)},500);	
	select();
}


function selectOther(select)
{
	var position = $('#'+selectedGroupingID).siblings().last();
	if(position.length > 0)
	{
		$('input[type="radio"]:checked:visible').not(":disabled").effect('transfer',  {to: $('#'+selectedGroupingID).siblings().last()},500);
	}
	else
	{
		$('input[type="radio"]:checked:visible').not(":disabled").effect('transfer',  {to: $('#'+selectedGroupingID)},500);	
	}
	select();
}