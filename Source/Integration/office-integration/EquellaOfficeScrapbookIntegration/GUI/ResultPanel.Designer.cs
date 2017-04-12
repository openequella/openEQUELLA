namespace Equella.GUI
{
    partial class ResultPanel
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
            this.picThumb = new System.Windows.Forms.PictureBox();
            this.lblTitle = new System.Windows.Forms.Label();
            this.lblKeywords = new System.Windows.Forms.Label();
            this.lblFilename = new System.Windows.Forms.Label();
            this.lblKeywordsTitle = new System.Windows.Forms.Label();
            this.lblFilenameTitle = new System.Windows.Forms.Label();
            this.lblTitleTitle = new System.Windows.Forms.Label();
            this.lblLastModifiedTitle = new System.Windows.Forms.Label();
            this.lblLastModified = new System.Windows.Forms.Label();
            ((System.ComponentModel.ISupportInitialize)(this.picThumb)).BeginInit();
            this.SuspendLayout();
            // 
            // picThumb
            // 
            this.picThumb.InitialImage = null;
            this.picThumb.Location = new System.Drawing.Point(9, 12);
            this.picThumb.Name = "picThumb";
            this.picThumb.Size = new System.Drawing.Size(64, 80);
            this.picThumb.SizeMode = System.Windows.Forms.PictureBoxSizeMode.StretchImage;
            this.picThumb.TabIndex = 0;
            this.picThumb.TabStop = false;
            // 
            // lblTitle
            // 
            this.lblTitle.AutoEllipsis = true;
            this.lblTitle.AutoSize = true;
            this.lblTitle.Font = new System.Drawing.Font("Microsoft Sans Serif", 14.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblTitle.Location = new System.Drawing.Point(174, 6);
            this.lblTitle.Name = "lblTitle";
            this.lblTitle.Size = new System.Drawing.Size(350, 24);
            this.lblTitle.TabIndex = 1;
            this.lblTitle.Text = "This is the title of the scrapbook item";
            // 
            // lblKeywords
            // 
            this.lblKeywords.AutoSize = true;
            this.lblKeywords.Font = new System.Drawing.Font("Microsoft Sans Serif", 9.75F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblKeywords.Location = new System.Drawing.Point(174, 32);
            this.lblKeywords.Name = "lblKeywords";
            this.lblKeywords.Size = new System.Drawing.Size(300, 16);
            this.lblKeywords.TabIndex = 2;
            this.lblKeywords.Text = "Keyword1, Keyword2, Keyword3, Keyword4";
            // 
            // lblFilename
            // 
            this.lblFilename.AutoSize = true;
            this.lblFilename.Font = new System.Drawing.Font("Microsoft Sans Serif", 9.75F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblFilename.Location = new System.Drawing.Point(174, 54);
            this.lblFilename.Name = "lblFilename";
            this.lblFilename.Size = new System.Drawing.Size(161, 16);
            this.lblFilename.TabIndex = 3;
            this.lblFilename.Text = "this is a filename.docx";
            // 
            // lblKeywordsTitle
            // 
            this.lblKeywordsTitle.AutoSize = true;
            this.lblKeywordsTitle.Font = new System.Drawing.Font("Microsoft Sans Serif", 9.75F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblKeywordsTitle.ForeColor = System.Drawing.Color.DimGray;
            this.lblKeywordsTitle.Location = new System.Drawing.Point(79, 32);
            this.lblKeywordsTitle.Name = "lblKeywordsTitle";
            this.lblKeywordsTitle.Size = new System.Drawing.Size(79, 16);
            this.lblKeywordsTitle.TabIndex = 4;
            this.lblKeywordsTitle.Text = "Keywords:";
            // 
            // lblFilenameTitle
            // 
            this.lblFilenameTitle.AutoSize = true;
            this.lblFilenameTitle.Font = new System.Drawing.Font("Microsoft Sans Serif", 9.75F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblFilenameTitle.ForeColor = System.Drawing.Color.DimGray;
            this.lblFilenameTitle.Location = new System.Drawing.Point(79, 54);
            this.lblFilenameTitle.Name = "lblFilenameTitle";
            this.lblFilenameTitle.Size = new System.Drawing.Size(76, 16);
            this.lblFilenameTitle.TabIndex = 5;
            this.lblFilenameTitle.Text = "Filename:";
            // 
            // lblTitleTitle
            // 
            this.lblTitleTitle.AutoSize = true;
            this.lblTitleTitle.Font = new System.Drawing.Font("Microsoft Sans Serif", 9.75F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblTitleTitle.ForeColor = System.Drawing.Color.DimGray;
            this.lblTitleTitle.Location = new System.Drawing.Point(79, 12);
            this.lblTitleTitle.Name = "lblTitleTitle";
            this.lblTitleTitle.Size = new System.Drawing.Size(43, 16);
            this.lblTitleTitle.TabIndex = 6;
            this.lblTitleTitle.Text = "Title:";
            // 
            // lblLastModifiedTitle
            // 
            this.lblLastModifiedTitle.AutoSize = true;
            this.lblLastModifiedTitle.Font = new System.Drawing.Font("Microsoft Sans Serif", 9.75F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblLastModifiedTitle.ForeColor = System.Drawing.Color.DimGray;
            this.lblLastModifiedTitle.Location = new System.Drawing.Point(79, 76);
            this.lblLastModifiedTitle.Name = "lblLastModifiedTitle";
            this.lblLastModifiedTitle.Size = new System.Drawing.Size(72, 16);
            this.lblLastModifiedTitle.TabIndex = 8;
            this.lblLastModifiedTitle.Text = "Modified:";
            // 
            // lblLastModified
            // 
            this.lblLastModified.AutoSize = true;
            this.lblLastModified.Font = new System.Drawing.Font("Microsoft Sans Serif", 9.75F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblLastModified.Location = new System.Drawing.Point(174, 76);
            this.lblLastModified.Name = "lblLastModified";
            this.lblLastModified.Size = new System.Drawing.Size(169, 16);
            this.lblLastModified.TabIndex = 7;
            this.lblLastModified.Text = "31 August 1977, 3:45PM";
            // 
            // ResultPanel
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.BackColor = System.Drawing.Color.White;
            this.Controls.Add(this.lblLastModifiedTitle);
            this.Controls.Add(this.lblLastModified);
            this.Controls.Add(this.lblTitleTitle);
            this.Controls.Add(this.lblFilenameTitle);
            this.Controls.Add(this.lblKeywordsTitle);
            this.Controls.Add(this.lblFilename);
            this.Controls.Add(this.lblKeywords);
            this.Controls.Add(this.lblTitle);
            this.Controls.Add(this.picThumb);
            this.Margin = new System.Windows.Forms.Padding(0);
            this.Name = "ResultPanel";
            this.Size = new System.Drawing.Size(540, 105);
            ((System.ComponentModel.ISupportInitialize)(this.picThumb)).EndInit();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.PictureBox picThumb;
        private System.Windows.Forms.Label lblTitle;
        private System.Windows.Forms.Label lblKeywords;
        private System.Windows.Forms.Label lblFilename;
        private System.Windows.Forms.Label lblKeywordsTitle;
        private System.Windows.Forms.Label lblFilenameTitle;
        private System.Windows.Forms.Label lblTitleTitle;
        private System.Windows.Forms.Label lblLastModifiedTitle;
        private System.Windows.Forms.Label lblLastModified;
    }
}
