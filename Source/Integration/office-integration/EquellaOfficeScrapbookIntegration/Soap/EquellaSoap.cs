using System;
using System.Data;
using System.Configuration;
using System.Linq;
using System.Web;
using System.Xml.Linq;
using System.Xml;
using System.Net;
using System.Security.Cryptography;
using System.Text;
using System.ServiceModel.Channels;
using System.ServiceModel;
using System.ServiceModel.Description;
using Equella;
using System.IO;

namespace Equella.Soap
{
    /**
    * This class is a thin wrapper around the automatically generated SoapService50 object.  It is provided for convenience.
    */
    public class EQUELLASOAP
    {
        private SoapService50Ex client;
        private ScrapbookSoapService.ScrapbookSoapService scrapbookClient;
        private string institutionURL;
        private CookieContainer cookieJar;


        /// <summary>
        /// Create an EQUELLASOAP and login to the EQUELLA server
        /// </summary>
        /// <param name="endpoint">Endpoint is of the form:  http://myserver/mysint/services/SoapService41</param>
        /// <param name="username">User's login name</param>
        /// <param name="password">User's password</param>
        public EQUELLASOAP(string endpoint, string username, string password)
            : this(endpoint, username, password, null, null, null, null)
        {
        }

        /// <summary>
        /// Create an EQUELLASOAP and login to the EQUELLA server
        /// </summary>
        /// <param name="institutionURL">URL is of the form:  http://myserver/mysint/</param>
        /// <param name="username">User's login name</param>
        /// <param name="password">User's password</param>
        /// <param name="proxyURL">The full URL to a proxy server (if required)</param>
        /// <param name="proxyUsername">The username for the proxy (if required)</param>
        /// <param name="proxyPassword">The password for the proxy (if required)</param>
        public EQUELLASOAP(string institutionURL, string username, string password, string proxyURL, string proxyUsername, string proxyPassword, CookieContainer cookieJar)
        {
            this.institutionURL = institutionURL;

            client = new SoapService50Ex();
            scrapbookClient = new ScrapbookSoapService.ScrapbookSoapService();

            ////You need to explicitly specify a URL, even if you setup a reference to a WSDL on your own server.  
            ////The wsdl returned contains a URL without the institution context, so the setting the URL is required to override this
            client.Url = institutionURL + "services/SoapService50";
            scrapbookClient.Url = institutionURL + "services/ScrapbookSoapService";
            
            ////This is important!  it is required to maintain user state between SOAP method invocations
            if (cookieJar == null)
            {
                this.cookieJar = cookieJar;
            }
            else
            {
                this.cookieJar = new CookieContainer();
            }
            client.CookieContainer = cookieJar;
            scrapbookClient.CookieContainer = cookieJar;

            if (proxyURL != null)
            {
                WebProxy proxy = null;
                if (proxyUsername != null)
                {
                    proxy = new WebProxy(new Uri(proxyURL), false, new string[0], new NetworkCredential(proxyUsername, proxyPassword));
                }
                else
                {
                    proxy = new WebProxy(new Uri(proxyURL));
                }
                client.Proxy = proxy;
                scrapbookClient.Proxy = proxy;
            }

            //The user must be logged in before any SOAP methods are invoked, otherwise the methods will assume you are an anonymous user
            //and your privileges will be limited.
            if (username != null)
            {
                client.login(username, password);
            }
        }


        /// <summary>
        /// You should call this when you are done with the EQUELLASOAP object
        /// </summary>
        public void Logout()
        {
            try
            {
                client.logout();
            }
            catch (Exception e)
            {
                System.Diagnostics.Debug.Print(string.Format("Failed to logout: {0}", e));
            }
        }

        #region SOAP invocations

        /********************************************************************************************************
         * You may want to add more wrapper methods like the ones below and make the client variable private
         ********************************************************************************************************/


        /// <summary>
        /// Search for items on the EQUELLA server.  Consult the SOAP API documentation for more information on the values of
        /// the parameters and return result.
        /// </summary>
        /// <param name="query"></param>
        /// <param name="where"></param>
        /// <param name="onlylive"></param>
        /// <param name="sorttype"></param>
        /// <param name="reversesort"></param>
        /// <param name="offset"></param>
        /// <param name="maxresults"></param>
        /// <returns></returns>
        public XElement SearchItems(string query, string[] collectionUuids, string where, bool onlylive, int sorttype, bool reversesort, int offset, int maxresults)
        {
            return NewXElement(
                client.searchItems(
                    query, collectionUuids, where, onlylive, sorttype, reversesort, offset, maxresults)
                );
        }

        /// <summary>
        /// Gets a list of collections that the user you logged in as can contribute to.
        /// </summary>
        /// <returns></returns>
        public XElement ContributableCollections()
        {
            return NewXElement(client.getContributableCollections());
        }

        /// <summary>
        /// Creates an item in the collection with the id of collectionId 
        /// for you to begin editing with.  Note that this will not create an item on the server until you call the saveItem method.
        /// The item XML will be initialised with a new UUID and a new staging ID  where attachments can be uploaded to.
        /// </summary>
        /// <param name="collectionUuid"></param>
        /// <returns></returns>
        public XElement NewItem(string collectionUuid)
        {
            return NewXElement(client.newItem(collectionUuid));
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="itemUuid"></param>
        /// <param name="itemVersion"></param>
        /// <returns></returns>
        public XElement EditItem(string itemUuid, int itemVersion)
        {
            return NewXElement(client.editItem(itemUuid, itemVersion, true));
        }

        /// <summary>
        /// Save changes made to an item which also unlocks the item. 
        /// Before calling this, you must either use {@link #editItem(String, int, boolean) editItem} or {@link #newItem(String) newItem} and use the XML
        /// returned by these methods to pass in as the item parameter.
        /// </summary>
        /// <param name="item"></param>
        /// <param name="submit"></param>
        public XElement SaveItem(XElement item, bool submit)
        {
            return NewXElement(client.saveItem(item.ToString(), submit));
        }


        /// <summary>
        /// Removes wizard for editing this item
        /// </summary>
        /// <param name="item"></param>
        public void CancelEdit(string itemUuid, int itemVersion)
        {
            client.cancelItemEdit(itemUuid, itemVersion);
        }


        /// <summary>
        /// Upload a file into the staging area.  Note that this does not attach the file to your item!  
        /// To link the file to the item you need to add an attachment node to the item XML.  Consult the SOAP API documentation for the format
        /// of the attachment nodes.
        /// </summary>
        /// <param name="stagingUuid"></param>
        /// <param name="serverFilename"></param>
        /// <param name="data"></param>
        public void UploadFile(string stagingUuid, string serverFilename, byte[] data)
        {
            string base64Data = Convert.ToBase64String(data);
            client.uploadFile(
                stagingUuid, serverFilename, base64Data, true);
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="stagingUuid"></param>
        /// <param name="serverFilename"></param>
        public void DeleteFile(string stagingUuid, string serverFilename)
        {
            client.deleteFile(stagingUuid, serverFilename);
        }


        /// <summary>
        /// Retrieves information about a collection.
        /// </summary>
        /// <param name="collectionUuid"></param>
        /// <returns></returns>
        public XElement GetCollection(string collectionUuid)
        {
            return NewXElement(client.getCollection(collectionUuid));
        }


        /// <summary>
        /// Retrieves information about a metadata schema.
        /// </summary>
        /// <param name="schemaUuid"></param>
        /// <returns></returns>
        public XElement GetSchema(string schemaUuid)
        {
            return NewXElement(client.getSchema(schemaUuid));
        }


        /// <summary>
        /// Check to see if an item exists
        /// </summary>
        /// <param name="itemUuid"></param>
        /// <param name="itemVersion"></param>
        /// <returns></returns>
        public bool ItemExists(string itemUuid, int itemVersion)
        {
            return client.itemExists(itemUuid, itemVersion);
        }


        /// <summary>
        /// 
        /// </summary>
        /// <param name="query"></param>
        /// <param name="mimeTypes"></param>
        /// <param name="offset"></param>
        /// <param name="length"></param>
        /// <returns></returns>
        public XElement SearchScrapbook(string query, string[] mimeTypes,  int sortOrder, int offset, int length)
        {
            return NewXElement(scrapbookClient.search(query, new string[]{"myresource"}, mimeTypes, sortOrder,  offset, length));
        }


        /// <summary>
        /// 
        /// </summary>
        /// <param name="title"></param>
        /// <param name="keywords"></param>
        /// <param name="filename"></param>
        /// <param name="data"></param>
        /// <returns></returns>
        public XElement NewScrapbookItem(string title, string keywords, string filename, byte[] data)
        {
            string base64Data = Convert.ToBase64String(data);
            return NewXElement(scrapbookClient.create(title, keywords, "myresource", filename, base64Data));
        }


        /// <summary>
        /// 
        /// </summary>
        /// <param name="itemUuid"></param>
        /// <param name="title"></param>
        /// <param name="keywords"></param>
        /// <param name="filename"></param>
        /// <param name="data"></param>
        /// <returns></returns>
        public XElement UpdateScrapbookItem(string itemUuid, string title, string keywords, string filename, byte[] data)
        {
            string base64Data = Convert.ToBase64String(data);
            return NewXElement(scrapbookClient.update(itemUuid, title, keywords, filename, base64Data));
        }


        /// <summary>
        /// 
        /// </summary>
        /// <param name="itemUuid"></param>
        /// <returns></returns>
        public bool ScrapbookItemExists(string itemUuid)
        {
            try
            {
                return scrapbookClient.exists(itemUuid);
            }
            catch (Exception)
            {
                return false;
            }
        }

        #endregion

        #region Private methods

        private XElement NewXElement(string xml)
        {
            return XElement.Load(new StringReader(xml));
        }

        #endregion

        #region Public methods

        /// <summary>
        /// 
        /// </summary>
        /// <exception cref="WebException">Throws this when the server cannot be contacted</exception>
        public bool LoggedIn
        {
            get
            {
                return client.LoggedIn;
            }
        }

        public string LoggedInUser
        {
            get
            {
                return client.LoggedInUser;
            }
        }

        public CookieContainer CookieJar
        {
            get
            {
                return cookieJar;
            }
        }

        #endregion

        #region Token generation code

        static readonly DateTime Epoch = new DateTime(1970, 1, 1, 0, 0, 0, 0, new System.Globalization.GregorianCalendar(), System.DateTimeKind.Utc);

        /// <summary>
        /// Generates a token that is valid for 30 minutes.  This should be appended to URLs so that users are not forced to log in to view content.
        /// E.g. 
        /// <code>
        /// string itemURL = "http://MYSERVER/myinst/items/619722b1-22f8-391a-2bcf-46cfaab36265/1/?token=" + EquellaSoap.GenerateToken("fred.smith", "IntegSecret", "squirrel");
        /// </code>
        /// 
        /// In the example above, if fred.smith is a valid username on the EQUELLA server he will be automatically logged into the system so that he can view 
        /// item 619722b1-22f8-391a-2bcf-46cfaab36265/1 (provided he has the permissions to do so).
        /// 
        /// Note that to use this functionality, the Shared Secrets user management plugin must be enabled (see User Management in the EQUELLA Administration Console)
        /// and a shared secret must be configured.
        /// </summary>
        /// <param name="username">The username of the user to log in as</param>
        /// <param name="sharedSecretId">The ID of the shared secret</param>
        /// <param name="sharedSecretValue">The value of the shared secret</param>
        /// <returns>A token that can be directly appended to a URL (i.e. it is already URL encoded) 
        /// E.g.  URL = URL + "?token=" + GenerateToken(x,y,z)</returns>
        //public static string GenerateToken(string username, string sharedSecretId, string sharedSecretValue)
        //{
        //    string time = CurrentTimeMillis().ToString();
        //    string plain = username + sharedSecretId + time + sharedSecretValue;

        //    MD5 md5 = System.Security.Cryptography.MD5.Create();
        //    byte[] inputBytes = System.Text.Encoding.UTF8.GetBytes(plain);
        //    string b64 = Convert.ToBase64String(md5.ComputeHash(inputBytes));

        //    return HttpUtility.URLEncode(username, Encoding.UTF8) + ":"
        //                            + HttpUtility.URLEncode(sharedSecretId, Encoding.UTF8)
        //                            + ":"
        //                            + time
        //                            + ":"
        //                            + HttpUtility.URLEncode(b64, Encoding.UTF8);
        //}

        private static long CurrentTimeMillis()
        {
            return (DateTime.UtcNow.Ticks - Epoch.Ticks) / TimeSpan.TicksPerMillisecond;
        }
        #endregion

        private class NotLoggedInException : Exception
        {
            public NotLoggedInException(string message)
                : base(message)
            {
            }
        }
    }
}