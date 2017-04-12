<?php
	function getPost($name, $default) 
	{
		$value = $default;
		if (!empty($_POST)) 
		{
			if (!empty($_POST[$name])) 
			{
				$value = $_POST[$name];
			}
		}
		return $value;
	}
	
	function getBooleanPost($name, $default)
        {
               $value = $default;
               if (!empty($_POST)) 
               {
                       if (!empty($_POST[$name])) 
                       {
                               $value = '1';
                       }
                       else
                       {
                               $value = '0';
                       }
               }
               return $value;
        }

	function readFileAsBase64($filename)
	{
		$fd = fopen($filename, 'rb');
		$size = filesize($filename);
		$fileData = fread($fd, $size);
		fclose($fd);
		return base64_encode($fileData);
	}  
	
	/**
	Generates a token that is valid for 30 minutes.  This should be appended to URLs so that users are not forced to log in to view content.
	E.g. 
	$itemURL = "http://MYSERVER/myinst/items/619722b1-22f8-391a-2bcf-46cfaab36265/1/?token=" . generateToken("fred.smith", "IntegSecret", "squirrel");
        
	In the example above, if fred.smith is a valid username on the EQUELLA server he will be automatically logged into the system so that he can view 
	item 619722b1-22f8-391a-2bcf-46cfaab36265/1 (provided he has the permissions to do so).
        
	Note that to use this functionality, the Shared Secrets user management plugin must be enabled (see User Management in the EQUELLA Administration Console)
	and a shared secret must be configured.
	
	@param username :The username of the user to log in as
	@param sharedSecretId :The ID of the shared secret
	@param sharedSecretValue :The value of the shared secret
	@return : A token that can be directly appended to a URL (i.e. it is already URL encoded)   E.g.  $URL = $URL . "?token=" . generateToken(x,y,z);
	*/
	function generateToken($username, $sharedSecretId, $sharedSecretValue)
	{
		$time = mktime() . '000';
		return urlencode ($username) . ':' . urlencode($sharedSecretId) . ':' .  $time . ':' . urlencode(base64_encode (pack ('H*', md5 ($username . $sharedSecretId . $time . $sharedSecretValue))));
	}
	

	/**
	* This class is a thin wrapper around the PHP SoapClient object.  It is provided for convenience.
	*/
	class EQUELLA
	{
		public $endpoint;
		public $client;
		
		#endpoint is of the form:  http://myserver/mysint/services/SoapService51
		public function __construct($endpoint, $username, $password, $proxyHost = null, $proxyPort = 0, $proxyUsername = null, $proxyPassword = null, $soapoptions = array())
		{
			$this->endpoint = $endpoint;
			
			#you need to explicitly specify a location.  The wsdl returned contains a URL without the institution context so the location is rquired to override this
			$requiredSoapOptions = array('location' => $endpoint);
			if ( $proxyHost != null )
			{
				$requiredSoapOptions = array_merge( $requiredSoapOptions, array('proxy_host' => $proxyHost, 'proxy_port' => $proxyPort, 'proxy_login' => $proxyUsername, 'proxy_password' => $proxyPassword) );
			}
			$this->client = new SoapClient($endpoint.'?wsdl', array_merge($requiredSoapOptions, $soapoptions));
			
			#note parameters are named 'in0', 'in1', inX etc..
			#the result is always 'out'
			$this->client->login(array('in0' => $username, 'in1' => $password));
		}
		
		
		public function __destruct()
		{
			$this->client->logout();
		}
		
		
		#you may want to add more wrapper methods like the ones below and make the $client variable private
		
		/**
		* @return XMLWrapper
		*/
		public function searchItems($query, $collectionUuids, $where, $onlylive, $sorttype, $reversesort, $offset, $maxresults)
		{
			return new XMLWrapper( 
				$this->client->searchItems(
					array('in0' => $query, 'in1' => $collectionUuids, 'in2' => $where, 'in3' => $onlylive, 'in4' => $sorttype, 'in5' => $reversesort, 'in6' => $offset, 'in7' => $maxresults)
				)->out );
		}
		
		
		/**
		* @return XMLWrapper
		*/
		public function contributableCollections()
		{
			return new XMLWrapper( $this->client->getContributableCollections()->out );
		}
		
		
		/**
		* @return XMLWrapper
		*/
		public function newItem($collectionUuid)
		{
			return new XMLWrapper( $this->client->newItem(array('in0' => $collectionUuid))->out );
		}
		
		
		/**
		* @param XMLWrapper
		* @param int (boolean)
		*/
		public function saveItem($item, $submit)
		{
			$this->client->saveItem(array('in0' => $item, 'in1' => $submit));
		}
		
		
		public function uploadFile($stagingUuid, $serverFilename, $localFilename)
		{
			$base64Data = readFileAsBase64($localFilename);
			$this->client->uploadFile(
				array('in0' => $stagingUuid, 'in1' => $serverFilename, 'in2' => $base64Data, 'in3' => '1')
				);
		}
		
		/**
		* @return XMLWrapper
		*/
		public function getCollection($collectionUuid)
		{
			return new XMLWrapper( $this->client->getCollection(array('in0' => $collectionUuid))->out );
		}
		
		/**
		* @return XMLWrapper
		*/
		public function getSchema($schemaUuid)
		{
			return new XMLWrapper( $this->client->getSchema(array('in0' => $schemaUuid))->out );
		}
	}
	
	
	/**
	* A wrapper around the DOMDocument and DOMXPath classes.  It is provided for convenience.
	*/
	class XMLWrapper
	{
		private $domDoc;
		private $xpathDoc;
		
		public function __construct($xmlString)
		{
			$this->domDoc = new DOMDocument();
			$this->domDoc->loadXML($xmlString);
			$this->xpathDoc = new DOMXPath($this->domDoc);
		}
		
		
		public function __toString()
		{
			return $this->domDoc->saveXML();
		}
		
		
		public function node($xpath, $nodeContext=null)
		{
			if ($nodeContext == null)
			{
				$nodeList = $this->xpathDoc->query($xpath);
			}
			else
			{
				$nodeList = $this->xpathDoc->query($xpath, $nodeContext);
			}
			return $this->singleNodeFromList( $nodeList );
		}
		
		
		public function nodeValue($xpath, $nodeContext=null)
		{
			if ($nodeContext == null)
			{
				$nodeList = $this->xpathDoc->query($xpath);
			}
			else
			{
				$nodeList = $this->xpathDoc->query($xpath, $nodeContext);
			}
			return $this->singleNodeValueFromList( $nodeList );
		}
		
		
		public function nodeList($xpath)
		{
			return $this->xpathDoc->query($xpath);
		}
		
		public function addNodeValue($xpath, $value)
		{
			$xpathElements = explode('/', $xpath);
			$numElems = count($xpathElements);
			$parentNodeXpath = implode('/', array_slice($xpathElements,0,$numElems-1));
			$leaf = $xpathElements[$numElems-1];
						
			$parentNode = $this->createNodeFromXPath($parentNodeXpath);
			$node = $this->createNode($parentNode, $leaf);
			$node->nodeValue = $value;
			return $node;
		}
		
		public function setNodeValue($xpath, $value)
		{
			$node = $this->singleNodeFromList( $this->nodeList($xpath) );
			if ($node == null)
			{
				$node = $this->createNodeFromXPath($xpath);
			}
			$node->nodeValue = $value;
			return $node;
		}
		
		
		public function createNode($parent, $nodeName)
		{
			$node = $this->domDoc->createElement($nodeName);
			$parent->appendChild($node);
			return $node;
		}
		
		
		public function createNodeFromXPath($xpath)
		{
			$node = $this->node($xpath);
			if ($node == null)
			{
				$xpathElements = explode('/', $xpath);
				$path = '';
				$node = $this->domDoc->documentElement;
				foreach ($xpathElements as $element)
				{
					if (!empty($element))
					{
						$path = $path.'/'.$element;
						$nextNode = $this->node($path);
						if ($nextNode == null)
						{
							$node = $this->createNode($node, $element);
						}
						else
						{
							$node = $nextNode;
						}
					}
				}
			}
			return $node;
		}
		
		
		public function createAttribute($parent, $attrName)
		{
			$node = $this->domDoc->createAttribute($attrName);
			$parent->appendChild($node);
			return $node;
		}
		
		
		public function deleteNodeFromXPath($xpath)
		{
			$node = $this->node($xpath);
			if ($node != null)
			{
				$node->parentNode->removeChild($node);
			}
		}
		
		
		private function singleNodeValueFromList($nodeList)
		{
			$node = $this->singleNodeFromList($nodeList);
			if ($node == null)
			{
				return '!! node not found !!';
			}
			return $node->nodeValue;
		}
		
		
		private function singleNodeFromList($nodeList)
		{
			if ($nodeList->length > 0)
			{
				$node = $nodeList->item(0);
				return $node;
			}
			return null;
		}
	}
?>