package com.tle.integration.blackboard.linkfixer;

/**
 * @author aholland
 */
public class Tester
{
	public static void main(String[] args)
	{
		StubFixer f = new StubFixer();
		f.setEquellaUrl("http://lebowski.com/dev+yeah/my/");
		f.addContent("<!--<item id=\"59be5bdf-0bf1-7b02-f12b-91ce644dbbd0\" itemdefid=\"48bd468b-e09e-4780-9e7d-11bc64e32709\" link=\"true\" live=\"false\" modified=\"false\" notify=\"true\" version=\"1\"><name>Pretty Photo</name><description/><requestUuid/><attachments selected=\"equella_cloud.PNG\" selectedDescription=\"\" selectedTitle=\"equella_cloud.PNG\"><attachment scorm=\"\"><file>crap.PNG</file><description>crap.PNG</description></attachment><attachment scorm=\"\"><file>equella_cloud.PNG</file><description>equella_cloud.PNG</description></attachment><attachment scorm=\"\"><file>IMAGE The Veronicas.jpg</file><description>IMAGE The Veronicas.jpg</description></attachment><attachment scorm=\"\"><file>avatar.jpeg</file><description>avatar.jpeg</description></attachment></attachments></item>--><table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"font-size:12pt\" width=\"450px\"><tr><td  colspan=\"2\"><img src=\"http://lebowski/dev/my/images/spacer.gif\" alt=\" \" style=\"border:none; width:0px; height:4px;\" /></td></tr><tr><td valign=\"top\"><table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"font-size:12pt\" width=\"100%\"><tr><td></td></tr><tr><td  colspan=\"2\"><img src=\"http://lebowski/dev/my/images/spacer.gif\" alt=\" \" style=\"border:none; width:0px; height:5px;\" /></td></tr><tr><td><table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"font-size:12pt\" ><tr><td ><img src=\"http://lebowski.com/dev+yeah/my/integ/bb/59be5bdf-0bf1-7b02-f12b-91ce644dbbd0/1/equella_cloud.PNG\" alt=\"equella_cloud.PNG\"/></td></tr></table></td></tr></table></td></tr></table>");
		// f.addContent("");
		// f.addContent("");

		try
		{
			f.submit(null);
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
}
