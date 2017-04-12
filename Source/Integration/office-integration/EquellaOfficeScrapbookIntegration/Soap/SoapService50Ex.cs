using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Xml.Linq;

namespace Equella.Soap
{
    /// <summary>
    /// Note!  You need to add [EquellaSoapExtension] to the base keepAlive() method if you regenerate the service from the WSDL
    /// </summary>
    partial class SoapService50Ex : SoapService50.SoapService50Service
    {
        internal XElement headerInfo;

        public bool LoggedIn
        {
            get
            {
                string loggedInUser = LoggedInUser;
                return loggedInUser != null && "guest" != loggedInUser;
            }
        }

        public string LoggedInUser
        {
            get
            {
                keepAlive();
                return headerInfo == null ? null : (string)headerInfo.Attribute("id");
            }
        }
    }
}
