<%@ Page Language="C#" AutoEventWireup="true" CodeFile="search.aspx.cs" Inherits="EQUELLA.search" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<title>EQUELLA SOAP Searching Example</title>
<link rel="stylesheet" type="text/css" href="equellasoap.css"/>
</head>
<body>
<form ID="form" runat="server">
<div>

<fieldset>
	<legend>Search Details</legend>
	<div class="formfield">
		<asp:Label AssociatedControlID="txtQuery" runat="server">Query</asp:Label>
		<div class="help">A simple text query.  E.g. <code>course*</code></div>
		<asp:TextBox ID="txtQuery" runat="server" />
	</div>
	<div class="formfield">
		<asp:Label AssociatedControlID="txtWhere" runat="server">Where</asp:Label>
		<div class="help">Example:&nbsp;&nbsp;<code>where /xml/my/metadatanode like 'val%'</code>&nbsp;&nbsp;See the SOAP API documentation (SoapService41.searchItems) for the complete documentation on the where clause.</div>
		<asp:TextBox ID="txtWhere" runat="server" />
	</div>
	<div class="formfield">
		<asp:Label AssociatedControlID="chkOnlylive" runat="server">Only Live Items?</asp:Label>
		<div class="help">Include on LIVE items in the search results.  I.e. not DRAFT.</div>
		<asp:CheckBox ID="chkOnlylive" runat="server" />
	</div>
	<div class="formfield">
		<asp:Label AssociatedControlID="lstSortType" runat="server">Sort Type</asp:Label>
		<div class="help">Order the results by</div>
		<asp:DropDownList ID="lstSortType" runat="server">
		    <asp:ListItem Value="0" Text="Search result relevance" />
		    <asp:ListItem Value="1" Text="Date modified" />
		    <asp:ListItem Value="2" Text="Item name" />
		</asp:DropDownList>
	</div>
	<div class="formfield">
		<asp:Label AssociatedControlID="chkReverseSort" runat="server">Reverse Sort?</asp:Label>
		<div class="help">Reverses the order of the Sort Type</div>
		<asp:CheckBox ID="chkReverseSort" runat="server" />
	</div>
	<div class="formfield">
		<asp:Label AssociatedControlID="txtOffset" runat="server">Offset</asp:Label>
		<div class="help">The index of the first result to retrieve (zero based, i.e. zero is the first result).  E.g. if your search returns 200 results, you could retrieve results 50 to 100 using an Offset of 50 and a Maximum Results of 50.</div>
		<asp:TextBox ID="txtOffset" runat="server" />
	</div>
	<div class="formfield">
		<asp:Label AssociatedControlID="txtMaxResults" runat="server">Maximum Results</asp:Label>
		<div class="help">The maximum number of results to return.</div>
		<asp:TextBox ID="txtMaxResults" runat="server" />
	</div>
</fieldset>

<div>
	<asp:Button ID="cmdSearch" runat="server" Text="Search" OnClick="cmdSearch_Click" />
</div>

<asp:Literal ID="feedback" runat="server" />

</div>
</form>
</body>
</html>
