namespace Equella.GUI
{
    partial class BrowseScrapbookPanel
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

        #region Component Designer generated code

        /// <summary> 
        /// Required method for Designer support - do not modify 
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.cmdSearch = new System.Windows.Forms.Button();
            this.txtSearch = new System.Windows.Forms.TextBox();
            this.linkProfile = new System.Windows.Forms.LinkLabel();
            this.lblResultCount = new System.Windows.Forms.Label();
            this.lblSortBy = new System.Windows.Forms.Label();
            this.listSortBy = new System.Windows.Forms.ComboBox();
            this.resultsPanel = new Equella.GUI.ResultsPanel();
            this.SuspendLayout();
            // 
            // cmdSearch
            // 
            this.cmdSearch.BackColor = System.Drawing.SystemColors.Control;
            this.cmdSearch.Font = new System.Drawing.Font("Microsoft Sans Serif", 12F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.cmdSearch.Location = new System.Drawing.Point(681, 49);
            this.cmdSearch.Name = "cmdSearch";
            this.cmdSearch.Size = new System.Drawing.Size(75, 29);
            this.cmdSearch.TabIndex = 0;
            this.cmdSearch.Text = "Search";
            this.cmdSearch.UseVisualStyleBackColor = false;
            this.cmdSearch.Click += new System.EventHandler(this.cmdSearch_Click);
            // 
            // txtSearch
            // 
            this.txtSearch.BackColor = System.Drawing.SystemColors.Window;
            this.txtSearch.Font = new System.Drawing.Font("Microsoft Sans Serif", 12F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.txtSearch.Location = new System.Drawing.Point(16, 49);
            this.txtSearch.Name = "txtSearch";
            this.txtSearch.Size = new System.Drawing.Size(659, 26);
            this.txtSearch.TabIndex = 1;
            this.txtSearch.KeyDown += new System.Windows.Forms.KeyEventHandler(this.txtSearch_KeyDown);
            // 
            // linkProfile
            // 
            this.linkProfile.AutoSize = true;
            this.linkProfile.Font = new System.Drawing.Font("Microsoft Sans Serif", 12F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.linkProfile.Location = new System.Drawing.Point(12, 16);
            this.linkProfile.Name = "linkProfile";
            this.linkProfile.Size = new System.Drawing.Size(95, 20);
            this.linkProfile.TabIndex = 4;
            this.linkProfile.TabStop = true;
            this.linkProfile.Text = "some profile";
            this.linkProfile.LinkClicked += new System.Windows.Forms.LinkLabelLinkClickedEventHandler(this.linkProfile_LinkClicked);
            // 
            // lblResultCount
            // 
            this.lblResultCount.AutoSize = true;
            this.lblResultCount.Font = new System.Drawing.Font("Microsoft Sans Serif", 12F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblResultCount.Location = new System.Drawing.Point(13, 92);
            this.lblResultCount.Name = "lblResultCount";
            this.lblResultCount.Size = new System.Drawing.Size(71, 20);
            this.lblResultCount.TabIndex = 5;
            this.lblResultCount.Text = "X results";
            // 
            // lblSortBy
            // 
            this.lblSortBy.AutoSize = true;
            this.lblSortBy.Font = new System.Drawing.Font("Microsoft Sans Serif", 12F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblSortBy.Location = new System.Drawing.Point(391, 92);
            this.lblSortBy.Name = "lblSortBy";
            this.lblSortBy.Size = new System.Drawing.Size(114, 20);
            this.lblSortBy.TabIndex = 6;
            this.lblSortBy.Text = "Sort results by:";
            // 
            // listSortBy
            // 
            this.listSortBy.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.listSortBy.Font = new System.Drawing.Font("Microsoft Sans Serif", 12F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.listSortBy.FormattingEnabled = true;
            this.listSortBy.Items.AddRange(new object[] {
            "Last Modified",
            "Relevance",
            "Title"});
            this.listSortBy.Location = new System.Drawing.Point(511, 84);
            this.listSortBy.Name = "listSortBy";
            this.listSortBy.Size = new System.Drawing.Size(164, 28);
            this.listSortBy.TabIndex = 7;
            // 
            // resultsPanel
            // 
            this.resultsPanel.BackColor = System.Drawing.Color.White;
            this.resultsPanel.BorderStyle = System.Windows.Forms.BorderStyle.Fixed3D;
            this.resultsPanel.Location = new System.Drawing.Point(16, 120);
            this.resultsPanel.Margin = new System.Windows.Forms.Padding(0);
            this.resultsPanel.Module = null;
            this.resultsPanel.Name = "resultsPanel";
            this.resultsPanel.Size = new System.Drawing.Size(740, 205);
            this.resultsPanel.TabIndex = 2;
            // 
            // BrowseScrapbookPanel
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.BackColor = System.Drawing.SystemColors.Control;
            this.Controls.Add(this.listSortBy);
            this.Controls.Add(this.lblSortBy);
            this.Controls.Add(this.lblResultCount);
            this.Controls.Add(this.linkProfile);
            this.Controls.Add(this.resultsPanel);
            this.Controls.Add(this.txtSearch);
            this.Controls.Add(this.cmdSearch);
            this.Margin = new System.Windows.Forms.Padding(0);
            this.Name = "BrowseScrapbookPanel";
            this.Size = new System.Drawing.Size(771, 341);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Button cmdSearch;
        private System.Windows.Forms.TextBox txtSearch;
        private ResultsPanel resultsPanel;
        private System.Windows.Forms.LinkLabel linkProfile;
        private System.Windows.Forms.Label lblResultCount;
        private System.Windows.Forms.Label lblSortBy;
        private System.Windows.Forms.ComboBox listSortBy;
    }
}
