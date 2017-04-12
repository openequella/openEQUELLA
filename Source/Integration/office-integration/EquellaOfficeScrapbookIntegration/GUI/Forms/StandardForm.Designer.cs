namespace Equella.GUI
{
    partial class StandardForm
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
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(StandardForm));
            this.webBrowser1 = new System.Windows.Forms.WebBrowser();
            this.browseScrapbookPanel = new Equella.GUI.BrowseScrapbookPanel();
            this.SuspendLayout();
            // 
            // webBrowser1
            // 
            this.webBrowser1.Location = new System.Drawing.Point(-1, 0);
            this.webBrowser1.MinimumSize = new System.Drawing.Size(20, 20);
            this.webBrowser1.Name = "webBrowser1";
            this.webBrowser1.Size = new System.Drawing.Size(772, 335);
            this.webBrowser1.TabIndex = 2;
            this.webBrowser1.DocumentCompleted += new System.Windows.Forms.WebBrowserDocumentCompletedEventHandler(this.webBrowser1_DocumentCompleted);
            this.webBrowser1.Navigated += new System.Windows.Forms.WebBrowserNavigatedEventHandler(this.webBrowser1_Navigated);
            this.webBrowser1.Navigating += new System.Windows.Forms.WebBrowserNavigatingEventHandler(this.webBrowser1_Navigating);
            // 
            // browseScrapbookPanel
            // 
            this.browseScrapbookPanel.BackColor = System.Drawing.SystemColors.Control;
            this.browseScrapbookPanel.Location = new System.Drawing.Point(382, 203);
            this.browseScrapbookPanel.Margin = new System.Windows.Forms.Padding(0);
            this.browseScrapbookPanel.Name = "browseScrapbookPanel";
            this.browseScrapbookPanel.Size = new System.Drawing.Size(771, 294);
            this.browseScrapbookPanel.TabIndex = 3;
            // 
            // StandardForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.BackColor = System.Drawing.SystemColors.Control;
            this.ClientSize = new System.Drawing.Size(1213, 667);
            this.Controls.Add(this.browseScrapbookPanel);
            this.Controls.Add(this.webBrowser1);
            this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
            this.Name = "StandardForm";
            this.Text = "Browse EQUELLA";
            this.KeyDown += new System.Windows.Forms.KeyEventHandler(this.StandardForm_KeyDown);
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.WebBrowser webBrowser1;
        private Equella.GUI.BrowseScrapbookPanel browseScrapbookPanel;
    }
}