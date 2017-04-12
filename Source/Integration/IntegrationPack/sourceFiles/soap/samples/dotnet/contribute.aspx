<%@ Page Language="C#" AutoEventWireup="true" CodeFile="contribute.aspx.cs" Inherits="EQUELLA.contribute" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<link rel="stylesheet" type="text/css" href="equellasoap.css"/>
<title>EQUELLA SOAP Contribution Example</title>
</head>
<body>
<form ID="form" runat="server">
<div>
<fieldset>
	<legend>Contibution Details</legend>
	<div class="formfield">
		<asp:Label runat="server" AssociatedControlID="lstCollections">Select a Collection</asp:Label>
		<div class="help">Choose a collection to contribute to.</div>
		<asp:DropDownList ID="lstCollections" runat="server">
		</asp:DropDownList>
	</div>
	<div class="formfield">
		<asp:Label runat="server" AssociatedControlID="txtItemName">Item Name</asp:Label>
		<div class="help">The name of the new item.</div>
		<asp:TextBox ID="txtItemName" runat="server" />
	</div>
	<div class="formfield">
		<asp:Label runat="server" AssociatedControlID="txtItemDescription">Item Description</asp:Label>
		<div class="help">A description of the new item.</div>
		<asp:TextBox ID="txtItemDescription" runat="server" />
	</div>
	<div class="formfield">
		<asp:Label runat="server" AssociatedControlID="txtAttachmentDescription">Attachment Description</asp:Label>
		<div class="help">A description for the uploaded attachment (if any).</div>
		<asp:TextBox ID="txtAttachmentDescription" runat="server" />
	</div>
	<div class="formfield">
		<asp:Label runat="server" AssociatedControlID="fileAttach">Attachment File</asp:Label>
		<div class="help">An attachment.</div>
		<asp:FileUpload ID="fileAttach" runat="server" />
	</div>
</fieldset>

<div>
    <asp:Button ID="cmdSubmit" runat="server" Text="Contribute" OnClick="cmdSubmit_Click" />
</div>

<asp:Literal ID="feedback" runat="server" />

</div>
</form>
</body>
</html>
