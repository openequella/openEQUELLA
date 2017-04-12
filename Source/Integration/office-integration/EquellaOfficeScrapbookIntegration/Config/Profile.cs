using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Xml.Linq;

namespace Equella.Config
{
    public class Profile
    {
        internal XElement ProfileXml { get; set; }

        public bool Active
        {
            get;
            set;
        }

        public string URL
        {
            get
            {
                return (string)ProfileXml.Element("url");
            }
            set
            {
                ProfileXml.SetElementValue("url", value);
            }
        }

        public virtual string Name
        {
            get
            {
                return (string)ProfileXml.Element("name");
            }
            set
            {
                ProfileXml.SetElementValue("name", value);
            }
        }

        public virtual string DisplayName
        {
            get
            {
                return (Active ? "(Active) " : string.Empty) + Name;
            }
        }

        public virtual string ProfileId
        {
            get
            {
                return (string)ProfileXml.Element("profileId");
            }
            set
            {
                ProfileXml.SetElementValue("profileId", value);
            }
        }

        internal virtual bool New
        {
            get;
            set;
        }
    }
}
