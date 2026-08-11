// v1.7.0  9/12/05
// -----------------


	/* Browser sniffer */
	/* --------------- */
	
	var isIE = false;
	var isOther = false;
	var isNS4 = false;
	var isNS6 = false;
	if(document.getElementById) {
		if(!document.all) {
			isNS6=true;
		}
		if(document.all) {
			isIE=true;
		}
	}
	else {
		if(document.layers) {
			isNS4=true;
		}
		else {
			isOther=true;
		}
	}
	

	/* Layer functions */
	/* --------------- */

	function aL(layerID) {
	var returnLayer;
		if(isIE) {
			returnLayer = eval("document.all." + layerID);
		}
		if(isNS6) {
			returnLayer = eval("document.getElementById('" + layerID + "')");
		}
		if(isNS4) {
			returnLayer = eval("document." + layerID);
		}
		if(isOther) {
			returnLayer = "null";
			alert(cgBrowserAlert);
		}
	return returnLayer;
	}


	function aLs(layerID) {
	var returnLayer;
		if(isIE) {
			returnLayer = aL(layerID).style;
		}
		if(isNS6) {
			returnLayer = aL(layerID).style;
		}
		if(isNS4) {
			returnLayer = aL(layerID);
		}
		if(isOther) {
			returnLayer = "null";
			alert(cgBrowserAlert);
		}
	return returnLayer;
	}


	/* Layer hide and show functions */
	/* ----------------------------- */
	
	function HideShow(ID) {
		if((aLs(ID).display == "block") || (aLs(ID).display == "")) {
			aLs(ID).display = "none";
		}
		else if(aLs(ID).display == "none") {
			aLs(ID).display = "block";
		}
	}

	function cgShow(ID) {
		aLs(ID).display = "block";
	}

	function cgHide(ID) {
		aLs(ID).display = "none";
	}



	/* Feedback functions for layers */
	/* ----------------------------- */

	function shfbkLyr_generic(n) {
	//Inputs:	N is the Question Number
	//Notes:	Displays Feedback for Question
		cgShow("Q" + n + "Feedback");
	}

	function shfbkLyr_mch1(n,c) {
	//Inputs:	N is the Question Number
	//		C is the Number of Choices
	//Notes:	Question Evaluates Choice from a Hidden Input in the HTML Form
	//		and Displays Correct/Incorrect Feedback
		var tmpChoice = eval("document.Q" + n + "Form.Q" + n + "Choice");
		var tmpMatch = eval("document.Q" + n + "Form.Q" + n + "Match");
		var correct=0;

		for (i = 0; i < c; i++) {
      			if (tmpChoice[i].checked == true) {
					if (tmpMatch.value == (i+1)) {
        				correct+= 1;
					}
				}
  		}

		if (correct == 0) {
			cgShow("Q" + n + "FBIncorrect");
			cgHide("Q" + n + "FBCorrect");
		}
		else {
			cgShow("Q" + n + "FBCorrect");
			cgHide("Q" + n + "FBIncorrect");
		}

	}

	function shfbkLyr_mch2(n,c) {
	//Inputs:	N is the Question Number
	//		C is the Number of Choices
	//Notes:	Question Evaluates Choice and Displays Corresponding FeedBack
		var tmpChoice = eval("document.Q" + n + "Form.Q" + n + "Choice");
		var tmpMatch = eval("document.Q" + n + "Form.Q" + n + "Match");
		var chosen = 0;
		for (i = 0; i < (c+1); i++) {
			cgHide("Q" + n + "Feedback" + i);
		}
		for (i = 0; i < c; i++) {
      			if (tmpChoice[i].checked == true) {
       				chosen = i+1;
				}
  		}

		cgShow("Q" + n + "Feedback" + chosen);
	}


	function shfbkLyr_chb2(n,c) {
	//Inputs:	N is the Question Number
	//		C is the Number of Choices
	//Notes:	Question Evaluates Choices and Displays Corresponding FeedBack
	//		Correct/Incorrect for Each Choice along with Score
	//		Gets Matches from Hidden Input in the Questions HTML Form
		var tmpChoice = eval("document.Q" + n + "Form.Q" + n + "Choice");
		var score = 0;

		for (i = 0; i < (c); i++) {
      		if (tmpChoice[i].checked == trim(eval("document.Q" + n + "Form.Q" + n + "Matches" + i + ".value"))) {
				cgShow("Q" + n + "FBCorrect" + i);
				cgHide("Q" + n + "FBIncorrect" + i);
				score += 1;
			}
			else {
				cgShow("Q" + n + "FBIncorrect" + i);
				cgHide("Q" + n + "FBCorrect" + i);
			}
  		}
	}


	function shfbkLyr_chb3(n,c) {
	//Inputs:	N is the Question Number
	//		C is the Number of Choices
	//Notes:	Question Evaluates Choices and Displays Correct/Incorrect FeedBack
	//		Gets Matches from Hidden Input in the Questions HTML Form
		var tmpChoice = eval("document.Q" + n + "Form.Q" + n + "Choice");
		var correct=0;

		for (i = 0; i < (c); i++) {
      		if (tmpChoice[i].checked != trim(eval("document.Q" + n + "Form.Q" + n + "Matches" + i + ".value"))) {
        		correct+= 1;
      		}
  		}

		if (correct == 0) {
			cgShow("Q" + n + "FBCorrect");
			cgHide("Q" + n + "FBIncorrect");
		}
		else {
			cgShow("Q" + n + "FBIncorrect");
			cgHide("Q" + n + "FBCorrect");
		}
	}


	function shfbkLyr_txt2(n){
	//Inputs:	N is the Question Number
	//Notes:	Displays user's response, correct answer and feedback for question
	
		var tmpTextEntry=eval("document.Q"+n+"Form.Q"+n+"TextEntry");
		var tmpYourAnswer=cgTextEntry2Response+tmpTextEntry.value+"<br/><br/>";
		var tmpHiddenAnswer=eval("document.Q"+n+"Form.Q"+n+"HiddenAnswer"+".value");
		var tmpFeedback=eval("document.Q"+n+"Form.Q"+n+"HiddenFeedback"+".value");
		var tmpObj=aL("Q"+n+"Feedback");
		
		tmpObj.innerHTML=tmpYourAnswer+tmpHiddenAnswer+"<br/><br/>"+tmpFeedback;
		cgShow("Q"+n+"Feedback");
	}


	function shfbkLyr_txt3(n,c) {
	//Inputs:	N is the Question Number
	//		C is the Number of Choices
	//Notes:	Question Evaluates Entered Text and Displays Correct/Incorrect FeedBack
	//		Gets Matches from Hidden Input in the Questions HTML Form
		var tmpTextEntry = eval("document.Q" + n + "Form.Q" + n + "TextEntry");
		var correct = 0;

		for (i = 0; i < (c); i++) {
      		if (trim(tmpTextEntry.value).toLowerCase() == trim(eval("document.Q" + n + "Form.Q" + n + "TextEntryMatches" + i + ".value")).toLowerCase()) {
        		correct = 1;
      		}
  		}

		if (correct == 0) {
			cgShow("Q" + n + "FBIncorrect");
			cgHide("Q" + n + "FBCorrect");
		}
		else {
			cgShow("Q" + n + "FBCorrect");
			cgHide("Q" + n + "FBIncorrect");
		}
	}


	function shfbkLyr_match2(n,c) {
	//Inputs:	N is the Question Number
	//		C is the Number of Choices
	//Notes:	Question Evaluates Entered Text and Displays Correct/Incorrect FeedBack
	//		Gets Matches from Hidden Input in the Questions HTML Form
		var correct=0;

		for (i = 1; i < (c+1); i++) {
      		if ((eval("document.Q" + n + "Form.Q" + n + "Combo" + i + ".selectedIndex")) != trim(eval("document.Q" + n + "Form.Q" + n + "Matches" + (i-1) + ".value"))) {
        		correct+= 1;
      		}
  		}

		if (correct == 0) {
			cgShow("Q" + n + "FBCorrect");
			cgHide("Q" + n + "FBIncorrect");
		}
		else {
			cgShow("Q" + n + "FBIncorrect");
			cgHide("Q" + n + "FBCorrect");
		}
	}


	function shfbkLyr_gap2(n,c) {
	//Inputs:	N is the Question Number
	//		C is the Number of Choices
	//Notes:	Question Evaluates Entered Text and Displays Correct/Incorrect FeedBack
	//		Gets Matches from Hidden Input in the Questions HTML Form
		var score=0;

		for (i = 0; i < (c); i++) {
      		if (trim(eval("document.Q" + n + "Form.Q" + n + "TextEntry" + (i+1) + ".value")).toLowerCase() == trim(eval("document.Q" + n + "Form.Q" + n + "TextEntryMatches" + i + ".value")).toLowerCase()) {
        		score += 1;
      		}
  		}

		var strTemp = cgGapfill2Response1 + score + cgGapfill2Response2 + c + cgGapfill2Response3;

		if (score == c) {
			var tmpObj1 = aL("Q" + n + "FBCorrectStore");
			var tmpObj2 = aL("Q" + n + "FBCorrect");
			tmpObj2.innerHTML = strTemp + "<br/><br/>" + tmpObj1.innerHTML;
			cgShow("Q" + n + "FBCorrect");
			cgHide("Q" + n + "FBIncorrect");
		}
		else {
			var tmpObj3 = aL("Q" + n + "FBIncorrectStore");
			var tmpObj4 = aL("Q" + n + "FBIncorrect");
			tmpObj4.innerHTML = strTemp + "<br/><br/>" + tmpObj3.innerHTML;
			cgShow("Q" + n + "FBIncorrect");
			cgHide("Q" + n + "FBCorrect");
		}
		
	}


	function shfbkLyr_gap4(n,c) {
	//Inputs:	N is the Question Number
	//		C is the Number of Choices
	//Notes:	Question Evaluates Entered Text and Displays Correct/Incorrect FeedBack
	//		Gets Matches from Hidden Input in the Questions HTML Form
		var score=0;

		for (i = 0; i < (c); i++) {
		var tmpCombo = eval("document.Q" + n + "Form.Q" + n + "Combo" + (i+1) + ".options[document.Q" + n + "Form.Q" + n + "Combo" + (i+1) + ".selectedIndex].text").toLowerCase();
      		if (tmpCombo == trim(eval("document.Q" + n + "Form.Q" + n + "TextEntryMatches" + i + ".value")).toLowerCase()) {
        		score += 1;
      		}
  		}

		var strTemp = cgGapfill4Response1 + score + cgGapfill4Response2 + c + cgGapfill4Response3;
		
		if (score == c) {
			var tmpObj1 = aL("Q" + n + "FBCorrectStore");
			var tmpObj2 = aL("Q" + n + "FBCorrect");
			tmpObj2.innerHTML = strTemp + "<br/><br/>" + tmpObj1.innerHTML;
			cgShow("Q" + n + "FBCorrect");
			cgHide("Q" + n + "FBIncorrect");
		}
		else {
			var tmpObj3 = aL("Q" + n + "FBIncorrectStore");
			var tmpObj4 = aL("Q" + n + "FBIncorrect");
			tmpObj4.innerHTML = strTemp + "<br/><br/>" + tmpObj3.innerHTML;
			cgShow("Q" + n + "FBIncorrect");
			cgHide("Q" + n + "FBCorrect");
		}
	
	}



	/* Window open functions */
	/* --------------------- */

	function winPop2(popUrl,myWidth,myHeight,myScroll) {
		popWin=window.open(""+popUrl,"popupWindow","width="+myWidth+",height="+myHeight+",scrollbars="+myScroll+",menubar=no");
		return true;
	}
	
	function winOpen(winUrl) { 
	 popWin=window.open(winUrl,"linkWindow","width=640,height=480,status=yes,toolbar=yes,menubar=yes,resizable=yes,location=yes,scrollbars=yes");
	}



	/* Utility functions */
	/* --------------------- */

	function trim(s) {
	    while (s.substring(0,1) == ' ') {
	    	s= s.substring(1,s.length);
	    }
	    while (s.substring(s.length-1,s.length) == ' ') {
	    	s = s.substring(0,s.length-1);
	    }
	    return s;
	}




	/* Scorm functions to store score */
	/* ------------------------------ */
	
	function savqn_mch1(id,n,c) {

		// Allow any error associated with saving a question to be displayed
		resetError();
		
		c = c.toString().substring(0,1);
		
		if ((c!="0") && (eval("document.Q" + n + "Form.Q" + n + "Choice[" + (c-1) + "].checked") == true)) {
			storeScore(id,1);
		}
		else {
			storeScore(id,0);
		}
		cgShow("Q" + n + "FBSaved");
	}

	function savqn_chb3(id,n,c,p) {

	// Allow any error associated with saving a question to be displayed
		resetError();

		var parray = p.split("|"), correct=0;

		for (i = 0; i < (c); i++) {
      		if (eval("document.Q" + n + "Form.Q" + n + "Choice[" + i + "].checked") != parray[i]) {
        		correct+= 1;
      		}
  		}

		if (correct == 0) {
			storeScore(id,1);
		}
		else {
			storeScore(id,0);
		}		
		cgShow("Q" + n + "FBSaved");
	}

	function savqn_txt3(id,n,c) {

		// Allow any error associated with saving a question to be displayed
		resetError();
		
		var tmpTextEntry = eval("document.Q" + n + "Form.Q" + n + "TextEntry");
		var correct = 0;

		for (i = 0; i < (c); i++) {
      		if (trim(tmpTextEntry.value).toLowerCase() == trim(eval("document.Q" + n + "Form.Q" + n + "TextEntryMatches" + i + ".value")).toLowerCase()) {
        		correct = 1;
      		}
  		}

		if (correct == 0) {
			storeScore(id,0);
		}
		else {
			storeScore(id,1);
		}
		cgShow("Q" + n + "FBSaved");
	}

	function savqn_match2(id,n,c,p) {

		// Allow any error associated with saving a question to be displayed
		resetError();

		var parray = p.split("|"), correct=0;

		for (i = 1; i < (c+1); i++) {
      		if ((eval("document.Q" + n + "Form.Q" + n + "Combo" + i + ".selectedIndex")) != parray[i-1]) {
        		correct+= 1;
      		}
  		}

		if (correct == 0) {
			storeScore(id,1);
		}
		else {
			storeScore(id,0);
		}		
		cgShow("Q" + n + "FBSaved");
	}

	function savqn_gap2(id,n,g,al) {

		// Allow any error associated with saving a question to be displayed
		resetError();

		var ans_array = al.split("|"), correct=0;

		for (i = 0; i < (g); i++) {
      		if (trim(eval("document.Q" + n + "Form.Q" + n + "TextEntry" + (i+1) + ".value")).toLowerCase() == trim(ans_array[i]).toLowerCase()) {
        		correct += 1;
      		}
  		}

		if (correct == g) {
			storeScore(id,1);
		}
		else {
			storeScore(id,0);
		}
		cgShow("Q" + n + "FBSaved");
	}

	function savqn_gap4(id,n,g,al) {
		
		// Allow any error associated with saving a question to be displayed
		resetError();
		
		var ans_array = al.split("|"), correct=0;

		for (i = 0; i < (g); i++) {
      		if (eval("document.Q" + n + "Form.Q" + n + "Combo" + (i+1) + ".options[document.Q" + n + "Form.Q" + n + "Combo" + (i+1) + ".selectedIndex].text") == ans_array[i]) {
        		correct += 1;
      		}
  		}

		if (correct == g) {
			storeScore(id,1);
		}
		else {
			storeScore(id,0);
		}
		cgShow("Q" + n + "FBSaved");
	}

	
	
	/* Rollover function */

    function imgSwap(imgName, imgSrc) {
         if (document.images) {
              document.images[imgName].src = imgSrc;
         }
    }



