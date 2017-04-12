using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using Equella;
using Equella.Properties;
using System.Collections.Specialized;
using System.Xml;
using System.IO;
using System.Reflection;
using System.Xml.Linq;
using Equella.Util;
using System.Net;
using Equella.Config;

namespace Equella.GUI
{
    public partial class ConfigForm : Form
    {
        private readonly NullProfile NULL_PROFILE = new NullProfile();
        private const string URL_DEFAULT = "http://";

        public ConfigForm()
        {
            InitializeComponent();
            Text = "Configure " + Constants.APP_NAME;
        }

        public void ShowProfiles(bool closeOnSave)
        {
            CloseOnSave = closeOnSave;
            LoadProfiles(null);
            ShowDialog();
        }

        private void LoadProfiles(Profile selectedProfile)
        {
            Profiles = new List<Profile>();
            Profiles.Add(NULL_PROFILE);
            Profiles.AddRange(Singletons.Config.Profiles);
            Profiles.Sort(new Comparison<Profile>(CompareProfiles));

            RefreshDropdown(selectedProfile ?? Singletons.Config.CurrentProfile ?? NULL_PROFILE);
            
            cmdSave.Enabled = false;
        }

        private void ddProfiles_SelectedIndexChanged(object sender, EventArgs e)
        {
            Profile profile = (Profile)ddProfiles.SelectedItem;
            if (profile != null)
            {
                LoadProfile(profile);
            }
        }

        private void LoadProfile(Profile profile)
        {
            if (profile is NullProfile)
            {
                profile = CreateTempProfile();
                txtProfileName.Text = string.Empty;
                txtURL.Text = URL_DEFAULT;
                cmdActivate.Enabled = false;
                cmdDelete.Enabled = false;
            }
            else
            {
                txtProfileName.Text = profile.Name;
                txtURL.Text = profile.URL;
                cmdActivate.Enabled = !profile.Active;
                cmdDelete.Enabled = true;
            }
            
            EditingProfile = profile;
        }

        private void RefreshDropdown(Profile selected)
        {
            //Surely there is a better way to refresh the items??
            ddProfiles.DataSource = null;

            ddProfiles.DisplayMember = "DisplayName";
            ddProfiles.ValueMember = "ProfileId";
            ddProfiles.DataSource = Profiles;

            ddProfiles.SelectedItem = selected;   
        }

        private int CompareProfiles(Profile p1, Profile p2)
        {
            if (p1 is NullProfile && !(p2 is NullProfile))
            {
                return -1;
            }
            else if (p2 is NullProfile && !(p1 is NullProfile))
            {
                return 1;
            }
            return p1.DisplayName.CompareTo(p2.DisplayName);
        }

        #region Button actions

        private void cmdSave_Click(object sender, EventArgs e)
        {
            Cursor = Cursors.WaitCursor;
            if (FinalValidation())
            {
                Profile profile = EditingProfile;
                profile.Name = txtProfileName.Text;
                profile.URL = txtURL.Text;

                Singletons.Config.SaveProfile(profile);

                cmdSave.Enabled = false;
                cmdActivate.Enabled = true;

                if (CloseOnSave)
                {
                    Hide();
                }
                else
                {
                    LoadProfiles(profile);
                }
            }
            Cursor = Cursors.Default;
        }

        private void cmdActivate_Click(object sender, EventArgs e)
        {
            Profile profile = (Profile)ddProfiles.SelectedItem;
            if (profile != NULL_PROFILE) // it can't be
            {
                Singletons.Config.ActivateProfile(profile);

                RefreshDropdown(profile);
            }
        }

        private void cmdDelete_Click(object sender, EventArgs e)
        {
            Profile profile = (Profile)ddProfiles.SelectedItem;
            if (profile != NULL_PROFILE) //it can't be
            {
                if (Utils.Confirm("Are you sure you want to delete this profile?"))
                {
                    Singletons.Config.DeleteProfile(profile);

                    EditingProfile = Singletons.Config.CurrentProfile ?? NULL_PROFILE;

                    LoadProfiles(null);
                }
            }
        }

        #endregion

        #region Validation

        private bool ValidateName(out string message)
        {
            if (string.IsNullOrWhiteSpace(txtProfileName.Text))
            {
                message = "You must enter a Profile Name";
                return false;
            }
            message = string.Empty;
            return true;
        }

        private bool ValidateURL(out string message)
        {
            string URL = txtURL.Text;
            if (string.IsNullOrWhiteSpace(URL) || URL == URL_DEFAULT || URL.Length < 8 || !URL.ToLower().StartsWith("http"))
            {
                message = "You must enter a valid Institution URL";
                return false;
            }
            message = string.Empty;
            return true;
        }

        private void txtProfileName_Validating(object sender, CancelEventArgs e)
        {
            string message;
            if (!ValidateName(out message))
            {
                e.Cancel = true;
                errorProvider.SetError(txtProfileName, message);
            }
        }

        private void txtProfileName_Validated(object sender, EventArgs e)
        {
            errorProvider.SetError(txtProfileName, string.Empty);
        }

        private void txtURL_Validating(object sender, CancelEventArgs e)
        {
            string message;
            if (!ValidateURL(out message))
            {
                e.Cancel = true;
                errorProvider.SetError(txtURL, message);
            }
        }

        private void txtURL_Validated(object sender, EventArgs e)
        {
            errorProvider.SetError(txtURL, string.Empty);
        }

        private void txtURL_TextChanged(object sender, EventArgs e)
        {
            UpdateSaveButton();
        }

        private void txtProfileName_TextChanged(object sender, EventArgs e)
        {
            UpdateSaveButton();
        }

        private void UpdateSaveButton()
        {
            string URL = txtURL.Text;
            string name = txtProfileName.Text;

            bool canSave = true;
            string message = null;
            if (!ValidateName(out message))
            {
                canSave = false;
            }
            else if (!ValidateURL(out message))
            {
                canSave = false;
            }

            cmdSave.Enabled = canSave;
        }

        private bool FinalValidation()
        {
            string message;
            if (!ValidateName(out message))
            {
                Utils.Alert(message);
                txtProfileName.Focus();
                return false;
            }

            if (!ValidateURL(out message))
            {
                Utils.Alert(message);
                txtURL.Focus();
                return false;
            }

            string URL = txtURL.Text;
            try
            {
                if (!URL.EndsWith("/"))
                {
                    URL = URL + '/';
                    txtURL.Text = URL;
                }
                Uri uri = new Uri(URL);
                // try to get an accessible URL e.g. SoapService50 without any redirects or errors
                using (Stream resp = Utils.LoadURL(new Uri(URL + "services/SoapService50?wsdl"), false))
                {
                }
            }
            catch (UriFormatException)
            {
                Utils.Alert("The Institution URL is invalid");
                return false;
            }
            catch (HttpException h)
            {
                string help;
                switch (h.Code)
                {
                    case HttpStatusCode.Found:
                    case HttpStatusCode.SeeOther:
                    case HttpStatusCode.Moved:
                        help = "A redirect occurred.  Perhaps part of the URL is incorrect?";
                        break;

                    case HttpStatusCode.NotFound:
                        help = "The URL cannot be reached at all";
                        break;

                    case HttpStatusCode.Forbidden:
                        help = "Access denied";
                        break;

                    default:
                        help = "HTTP status code " + (int)h.Code;
                        break;
                }
                Utils.Alert("There was problem contacting the Institution URL: " + help);
                return false;
            }
            catch (Exception e)
            {
                Utils.Alert("There was problem contacting the Institution URL: " + e.Message);
                return false;
            }
            return true;
        }


        #endregion

        private Profile CreateTempProfile()
        {
            XElement proX = new XElement("profile",
                        new XElement("profileId", System.Guid.NewGuid().ToString()));
            return new Profile { ProfileXml = proX, New = true };
        }

        private List<Profile> Profiles
        {
            get;
            set;
        }

        private Profile EditingProfile 
        { 
            get; 
            set; 
        }
        
        private bool CloseOnSave { get; set; }


        private class NullProfile : Profile
        {
            public override string Name
            {
                get
                {
                    return "<  New profile  >";
                }
            }

            public override string DisplayName
            {
                get
                {
                    return Name;
                }
            }

            public override string ProfileId
            {
                get
                {
                    return null;
                }
            }

            internal override bool New
            {
                get
                {
                    return true;
                }
            }
        }
    }
}
