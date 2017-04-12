using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Xml.Linq;
using System.IO;

namespace Equella.Config
{
    public class ProfileConfiguration
    {
        private readonly string PROFILES_XML = Path.Combine(Constants.AddInPreferenceFolder.FullName, "Profiles.xml");
        
        public ProfileConfiguration()
        {
            LoadProfiles();
        }

        private void LoadProfiles()
        {
            string defaultProfileId = DefaultProfileId;
            Profiles = new List<Profile>();
            Profiles.AddRange(
                from p in XmlConfig.Elements("profile")
                select new Profile { ProfileXml = p });
            foreach (Profile p in Profiles)
            {
                if (defaultProfileId != null && p.ProfileId == defaultProfileId)
                {
                    p.Active = true;
                    CurrentProfile = p;
                }
            }
        }

        public void SaveProfile(Profile profile)
        {
            if (profile.New)
            {
                profile.New = false;
                Profiles.Add(profile);
                XmlConfig.Add(profile.ProfileXml);
            }

            //if we have no default, make it this one
            if (string.IsNullOrEmpty(DefaultProfileId))
            {
                DefaultProfileId = profile.ProfileId;
                CurrentProfile = profile;
            }

            XmlConfig.Save(PROFILES_XML);
        }

        public void ActivateProfile(Profile profile)
        {
            DefaultProfileId = profile.ProfileId;
            CurrentProfile = profile;

            foreach (Profile otherProfile in Profiles)
            {
                otherProfile.Active = false;
            }
            profile.Active = true;

            XmlConfig.Save(PROFILES_XML);
        }

        public void DeleteProfile(Profile profile)
        {
            bool wasActive = profile.Active;
            Profiles.Remove(profile);

            (
                from p in XmlConfig.Elements("profile")
                where (string)p.Element("profileId") == profile.ProfileId
                select p
            ).FirstOrDefault().Remove();

           
            if (wasActive)
            {
                if (Profiles.Count > 0)
                {
                    Profile newCurrent = Profiles[0];
                    DefaultProfileId = newCurrent.ProfileId;
                    CurrentProfile = newCurrent;
                }
                else
                {
                    DefaultProfileId = null;
                    CurrentProfile = null;
                }
            }

            XmlConfig.Save(PROFILES_XML);
        }

        private Profile _CurrentProfile;
        public Profile CurrentProfile
        {
            get
            {
                return _CurrentProfile;
            }
            set
            {
                if (_CurrentProfile != value)
                {
                    if (Singletons.Module.ProfileChanged(_CurrentProfile, value))
                    {
                        _CurrentProfile = value;
                        foreach (Profile profile in Profiles)
                        {
                            profile.Active = false;
                        }
                        if (_CurrentProfile != null)
                        {
                            _CurrentProfile.Active = true;
                        }
                    }
                }
            }
        }

        private string DefaultProfileId
        {
            get
            {
                return (string)XmlConfig.Element("defaultProfileId");
            }
            set
            {
                XmlConfig.SetElementValue("defaultProfileId", value);
            }
        }

        public List<Profile> Profiles
        {
            get;
            set;
        }

        private XElement _XmlConfig;
        private XElement XmlConfig
        {
            get
            {
                if (_XmlConfig == null)
                {
                    if (File.Exists(PROFILES_XML))
                    {
                        _XmlConfig = XElement.Load(PROFILES_XML);
                    }
                    else
                    {
                        _XmlConfig = new XElement("profiles");
                    }
                }
                return _XmlConfig;
            }
        }
    }
}
