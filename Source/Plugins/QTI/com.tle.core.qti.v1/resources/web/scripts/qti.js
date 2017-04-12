function showQuestion(ele, question)
{	
	$(".answer.active").hide();
	$("#answer_" + question).addClass("active");
	$("#answer_" + question).show();
	
	$(".question").removeClass("active");
	$(ele).addClass("active");
}