namespace Equella.GUI
{
    partial class ConfigForm
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.components = new System.ComponentModel.Container();
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(ConfigForm));
            this.grpProfile = new System.Windows.Forms.GroupBox();
            this.label1 = new System.Windows.Forms.Label();
            this.lblInstitutionURLHelp = new System.Windows.Forms.Label();
            this.lblProfileName = new System.Windows.Forms.Label();
            this.lblURL = new System.Windows.Forms.Label();
            this.cmdSave = new System.Windows.Forms.Button();
            this.txtURL = new System.Windows.Forms.TextBox();
            this.txtProfileName = new System.Windows.Forms.TextBox();
            this.cmdDelete = new System.Windows.Forms.Button();
            this.ddProfiles = new System.Windows.Forms.ComboBox();
            this.lblProfiles = new System.Windows.Forms.Label();
            this.cmdActivate = new System.Windows.Forms.Button();
            this.errorProvider = new System.Windows.Forms.ErrorProvider(this.components);
            this.grpProfile.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.errorProvider)).BeginInit();
            this.SuspendLayout();
            // 
            // grpProfile
            // 
            this.grpProfile.Controls.Add(this.label1);
            this.grpProfile.Controls.Add(this.lblInstitutionURLHelp);
            this.grpProfile.Controls.Add(this.lblProfileName);
            this.grpProfile.Controls.Add(this.lblURL);
            this.grpProfile.Controls.Add(this.cmdSave);
            this.grpProfile.Controls.Add(this.txtURL);
            this.grpProfile.Controls.Add(this.txtProfileName);
            this.grpProfile.Location = new System.Drawing.Point(12, 51);
            this.grpProfile.Name = "grpProfile";
            this.grpProfile.Size = new System.Drawing.Size(719, 241);
            this.grpProfile.TabIndex = 8;
            this.grpProfile.TabStop = false;
            this.grpProfile.Text = "Profile";
            // 
            // label1
            // 
            this.label1.Location = new System.Drawing.Point(99, 55);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(602, 41);
            this.label1.TabIndex = 11;
            this.label1.Text = "A display name for this profile.  This name will appear in the drop-down box abov" +
                "e which provides a method to easily switch between multiple EQUELLA servers.";
            // 
            // lblInstitutionURLHelp
            // 
            this.lblInstitutionURLHelp.Location = new System.Drawing.Point(99, 141);
            this.lblInstitutionURLHelp.Name = "lblInstitutionURLHelp";
            this.lblInstitutionURLHelp.Size = new System.Drawing.Size(602, 56);
            this.lblInstitutionURLHelp.TabIndex = 9;
            this.lblInstitutionURLHelp.Text = resources.GetString("lblInstitutionURLHelp.Text");
            // 
            // lblProfileName
            // 
            this.lblProfileName.AutoSize = true;
            this.lblProfileName.Location = new System.Drawing.Point(13, 32);
            this.lblProfileName.Name = "lblProfileName";
            this.lblProfileName.Size = new System.Drawing.Size(67, 13);
            this.lblProfileName.TabIndex = 7;
            this.lblProfileName.Text = "Profile Name";
            // 
            // lblURL
            // 
            this.lblURL.AutoSize = true;
            this.lblURL.Location = new System.Drawing.Point(13, 114);
            this.lblURL.Name = "lblURL";
            this.lblURL.Size = new System.Drawing.Size(77, 13);
            this.lblURL.TabIndex = 6;
            this.lblURL.Text = "Institution URL";
            // 
            // cmdSave
            // 
            this.cmdSave.Location = new System.Drawing.Point(626, 200);
            this.cmdSave.Name = "cmdSave";
            this.cmdSave.Size = new System.Drawing.Size(75, 23);
            this.cmdSave.TabIndex = 4;
            this.cmdSave.Text = "Save";
            this.cmdSave.UseVisualStyleBackColor = true;
            this.cmdSave.Click += new System.EventHandler(this.cmdSave_Click);
            // 
            // txtURL
            // 
            this.txtURL.Location = new System.Drawing.Point(99, 114);
            this.txtURL.Name = "txtURL";
            this.txtURL.Size = new System.Drawing.Size(602, 20);
            this.txtURL.TabIndex = 2;
            this.txtURL.TextChanged += new System.EventHandler(this.txtURL_TextChanged);
            this.txtURL.Validating += new System.ComponentModel.CancelEventHandler(this.txtURL_Validating);
            this.txtURL.Validated += new System.EventHandler(this.txtURL_Validated);
            // 
            // txtProfileName
            // 
            this.txtProfileName.Location = new System.Drawing.Point(99, 32);
            this.txtProfileName.Name = "txtProfileName";
            this.txtProfileName.Size = new System.Drawing.Size(602, 20);
            this.txtProfileName.TabIndex = 1;
            this.txtProfileName.TextChanged += new System.EventHandler(this.txtProfileName_TextChanged);
            this.txtProfileName.Validating += new System.ComponentModel.CancelEventHandler(this.txtProfileName_Validating);
            this.txtProfileName.Validated += new System.EventHandler(this.txtProfileName_Validated);
            // 
            // cmdDelete
            // 
            this.cmdDelete.CausesValidation = false;
            this.cmdDelete.Location = new System.Drawing.Point(638, 16);
            this.cmdDelete.Name = "cmdDelete";
            this.cmdDelete.Size = new System.Drawing.Size(75, 23);
            this.cmdDelete.TabIndex = 5;
            this.cmdDelete.Text = "Delete";
            this.cmdDelete.UseVisualStyleBackColor = true;
            this.cmdDelete.Click += new System.EventHandler(this.cmdDelete_Click);
            // 
            // ddProfiles
            // 
            this.ddProfiles.CausesValidation = false;
            this.ddProfiles.DisplayMember = "DisplayName";
            this.ddProfiles.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.ddProfiles.FormattingEnabled = true;
            this.ddProfiles.Location = new System.Drawing.Point(59, 16);
            this.ddProfiles.Name = "ddProfiles";
            this.ddProfiles.Size = new System.Drawing.Size(489, 21);
            this.ddProfiles.TabIndex = 9;
            this.ddProfiles.ValueMember = "ProfileId";
            this.ddProfiles.SelectedIndexChanged += new System.EventHandler(this.ddProfiles_SelectedIndexChanged);
            // 
            // lblProfiles
            // 
            this.lblProfiles.AutoSize = true;
            this.lblProfiles.Location = new System.Drawing.Point(12, 16);
            this.lblProfiles.Name = "lblProfiles";
            this.lblProfiles.Size = new System.Drawing.Size(41, 13);
            this.lblProfiles.TabIndex = 10;
            this.lblProfiles.Text = "Profiles";
            // 
            // cmdActivate
            // 
            this.cmdActivate.CausesValidation = false;
            this.cmdActivate.Location = new System.Drawing.Point(557, 16);
            this.cmdActivate.Name = "cmdActivate";
            this.cmdActivate.Size = new System.Drawing.Size(75, 23);
            this.cmdActivate.TabIndex = 11;
            this.cmdActivate.Text = "Activate";
            this.cmdActivate.UseVisualStyleBackColor = true;
            this.cmdActivate.Click += new System.EventHandler(this.cmdActivate_Click);
            // 
            // errorProvider
            // 
            this.errorProvider.ContainerControl = this;
            // 
            // ConfigForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.AutoValidate = System.Windows.Forms.AutoValidate.EnableAllowFocusChange;
            this.ClientSize = new System.Drawing.Size(743, 308);
            this.Controls.Add(this.cmdActivate);
            this.Controls.Add(this.lblProfiles);
            this.Controls.Add(this.ddProfiles);
            this.Controls.Add(this.grpProfile);
            this.Controls.Add(this.cmdDelete);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedDialog;
            this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
            this.MaximizeBox = false;
            this.MinimizeBox = false;
            this.Name = "ConfigForm";
            this.Text = "ConfigForm";
            this.grpProfile.ResumeLayout(false);
            this.grpProfile.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.errorProvider)).EndInit();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.GroupBox grpProfile;
        private System.Windows.Forms.Label lblProfileName;
        private System.Windows.Forms.Label lblURL;
        private System.Windows.Forms.Button cmdDelete;
        private System.Windows.Forms.Button cmdSave;
        private System.Windows.Forms.TextBox txtURL;
        private System.Windows.Forms.TextBox txtProfileName;
        private System.Windows.Forms.ComboBox ddProfiles;
        private System.Windows.Forms.Label lblProfiles;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label lblInstitutionURLHelp;
        private System.Windows.Forms.Button cmdActivate;
        private System.Windows.Forms.ErrorProvider errorProvider;


    }
}