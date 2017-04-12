using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using Equella.Util;

namespace Equella
{
    public partial class ItemDetailsForm : Form
    {
        public ItemDetailsForm()
        {
            InitializeComponent();
        }

        public DialogResult ShowDetails(string itemName, string keywords, string attachmentFilename)
        {
            txtItemName.Text = itemName;
            txtKeywords.Text = keywords;
            txtAttachmentFilename.Text = attachmentFilename;
            return ShowDialog();
        }

        private bool ValidateItemName(out string message)
        {
            if (string.IsNullOrWhiteSpace(txtItemName.Text))
            {
                message = "You must enter a name for the resource";
                return false;
            }
            message = "";
            return true;
        }

        private bool ValidateAttachmentFilename(out string message)
        {
            if (string.IsNullOrWhiteSpace(txtAttachmentFilename.Text))
            {
                message = "You must enter a filename for the attachment";
                return false;
            }
            message = "";
            return true;
        }

        private void txtItemName_Validating(object sender, CancelEventArgs e)
        {
            string message;
            if (!ValidateItemName(out message))
            {
                e.Cancel = true;
                errorProvider.SetError(txtItemName, message);
            }
        }

        private void txtItemName_Validated(object sender, EventArgs e)
        {
            errorProvider.SetError(txtItemName, string.Empty);
        }

        private void txtAttachmentFilename_Validating(object sender, CancelEventArgs e)
        {
            string message;
            if (!ValidateAttachmentFilename(out message))
            {
                e.Cancel = true;
                errorProvider.SetError(txtAttachmentFilename, message);
            }
        }

        private void txtAttachmentFilename_Validated(object sender, EventArgs e)
        {
            errorProvider.SetError(txtAttachmentFilename, string.Empty);
        }
        
        private void cmdOk_Click(object sender, EventArgs e)
        {
            string message;
            if (!ValidateItemName(out message))
            {
                Utils.Alert(message);
                return;
            }

            if (!ValidateAttachmentFilename(out message))
            {
                Utils.Alert(message);
                return;
            }

            Hide();
        }

        private void cmdCancel_Click(object sender, EventArgs e)
        {
            Hide();
        }

        public string ItemName
        {
            get
            {
                return txtItemName.Text;
            }
        }

        public string Keywords
        {
            get
            {
                return txtKeywords.Text;
            }
        }

        public string AttachmentFilename
        {
            get
            {
                return txtAttachmentFilename.Text;
            }
        }
    }
}
