<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<@css "qti.css" />
<@script "qti.js" />

<#assign q = m.quiz />
<#assign questions = q.questions />

<div class="qti">
	<div class="area">
		<h2><@render q.title /></h2><br>

		<div class="quizContainer"> 
				<div class="questionContainer" style="margin:auto;">
					<#list questions as question>
						<div id="question_${question_index+1}" class="question" onclick="showQuestion(this, ${question_index+1});">
							${b.key("quiz.question", question_index+1)}<@render question.title/>
						</div>
					</#list>
				</div>
				<div class="answerContainer">
					<#list questions as question>
						<div id="answer_${question_index+1}" class="answer" style="display: none">
							<h3>${b.key("quiz.question", question_index+1)}<@render question.title/></h3>
							<h4><@render question.question/></h4>
							
							<div class="input checkbox response"><@render question.responses/></div>
							
							<div class="clear">
								<h3>${b.key("quiz.answers")}</h3>
								<p><@render question.generalFeedback/></p>
								<@render question.answers/>
							</div>
						</div>
					</#list>
				</div>
			<div class="clear"></div>
		</div>
	</div>
</div>