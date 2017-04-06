<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<link rel="stylesheet" type="text/css" href="equellasoap.css"/>
<title>EQUELLA SOAP Contribution Example</title>
</head>
<body>
<form method="post" action="contribute.php" enctype="multipart/form-data">
<div>
<?php
	require 'common.php';
	require 'settings.php';
	
	$equella = new EQUELLA($endpoint, $username, $password, $proxyHost, $proxyPort, $proxyUsername, $proxyPassword);

	$collectionsXml = $equella->contributableCollections();
	
	$collectionUuid = getPost('collection', '');
	$itemname = getPost('itemname', '');
	$itemdescription = getPost('itemdescription', '');
	$attachmentdescription = getPost('attachmentdescription', '');
	
?>

<fieldset>
	<legend>Contibution Details</legend>
	<div class="formfield">
		<label for="collection">Select a Collection</label>
		<div class="help">Choose a collection to contribute to.</div>
		<select id="collection" name="collection">
			<?php
			foreach ($collectionsXml->nodeList('/xml/itemdef') as $collectionNode)
			{
				$thisCollectionUuid = $collectionsXml->nodeValue('uuid', $collectionNode);
				echo('<option value="'.$thisCollectionUuid.'"');
				if ($thisCollectionUuid == $collectionUuid)
				{
					echo(' selected="selected"');
				}
				echo('>'.$collectionsXml->nodeValue('name', $collectionNode).'</option>'."\n");
			}
			?>
		</select>
	</div>
	<div class="formfield">
		<label for="itemname">Item Name</label>
		<div class="help">The name of the new item.</div>
		<input id="itemname" name="itemname" type="text" />
	</div>
	<div class="formfield">
		<label for="itemdescription">Item Description</label>
		<div class="help">A description of the new item.</div>
		<input id="itemdescription" name="itemdescription" type="text" />
	</div>
	<div class="formfield">
		<label for="attachmentdescription">Attachment Description</label>
		<div class="help">A description for the uploaded attachment (if any).</div>
		<input id="attachmentdescription" name="attachmentdescription" type="text" />
	</div>
	<div class="formfield">
		<label for="attachmentfile">Attachment File</label>
		<div class="help">An attachment.</div>
		<input id="attachmentfile" name="attachmentfile" type="file" />
	</div>
</fieldset>

<div>
	<input type="submit" name="contribute" value="Contribute" />
</div>

<?php
	if (getPost('contribute', null) != null)
	{
		###################################################################################
		# EQUELLA SOAP Contribution Code
		###################################################################################
		
		#Create the item on the server.  The general process is: 
		# 1. newItem (creates a new item in the staging folder, doesn't get committed until saveItem is called)
		# 2. set metadata on the item XML (some are system fields e.g. /xml/item/attachments, others could be custom schema fields)
		# 3. saveItem (commits the changes to a previous newItem or editItem call)
	
		#Note: you may need to modify upload_max_filesize in your php.ini settings file to something more reasonable (default is only 2MB)
		#You will also need to modify post_max_size to suit.
	
		$item = $equella->newItem($collectionUuid);
		
		#set item name and description
		$itemUuid = $item->nodeValue('/xml/item/@id');
		$itemVersion = $item->nodeValue('/xml/item/@version');
		$stagingUuid = $item->nodeValue('/xml/item/staging');
		
		#name and description xpaths are collection dependent!  You need to find the correct xpath to use for the name and description nodes
		$collection = $equella->getCollection($collectionUuid);
		$schema = $equella->getSchema($collection->nodeValue('/itemdef/schemaUuid'));
		
		$itemNameXPath = '/xml'.$schema->nodeValue('/schema/itemNamePath');
		$itemDescriptionXPath = '/xml'.$schema->nodeValue('/schema/itemDescriptionPath');
		$item->setNodeValue( $itemNameXPath, $itemname );
		$item->setNodeValue( $itemDescriptionXPath, $itemdescription);
		
		if (isset($_FILES['attachmentfile']) && !empty($_FILES['attachmentfile']['name']))
		{
			$attachmentFilename = $_FILES['attachmentfile']['name']; 
			$equella->uploadFile($stagingUuid, $attachmentFilename, $_FILES['attachmentfile']['tmp_name']);
			
			#create the attachment object on the item
			$attachmentsNode = $item->node('/xml/item/attachments');
			if ($attachmentsNode == null)
			{
				$attachmentsNode = $item->createNode($item->node('/xml/item'), 'attachments');
			}
			$attachmentNode = $item->createNode($attachmentsNode, 'attachment');
			$item->createAttribute($attachmentNode, 'type')->nodeValue = 'local';
			$item->createNode($attachmentNode, 'file')->nodeValue = $attachmentFilename;
			$item->createNode($attachmentNode, 'description')->nodeValue = $attachmentdescription;
			$item->createNode($attachmentNode, 'size')->nodeValue = $_FILES['attachmentfile']['size'];
		}
		
		#save and submit
		#$equella->client->cancelItemEdit(array('in0' => $itemUuid, 'in1' => $itemVersion));
		$equella->saveItem($item, '1');
		
		echo('<h3>Item "'.$itemname.'" contributed</h3>');
	}
?>

</div>
</form>
</body>
</html>
