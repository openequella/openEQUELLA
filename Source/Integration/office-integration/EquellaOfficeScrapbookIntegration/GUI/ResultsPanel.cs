using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Drawing;
using System.Data;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Reflection;
using System.Collections;

namespace Equella.GUI
{
    public partial class ResultsPanel : UserControl
    {
        private const int ROW_HEIGHT = 103;

        public event OnSelectionHandler OnSelection;

        public ResultsPanel()
        {
            InitializeComponent();
            Layout += new LayoutEventHandler(OnLayout);
        }

        public void ClearResults()
        {
            tableLayoutPanel.Controls.Clear();
            tableLayoutPanel.RowCount = 0;
            tableLayoutPanel.Hide();
            lblNoResults.Show();
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="uuid"></param>
        /// <param name="URL"></param>
        /// <param name="title"></param>
        /// <param name="keywords"></param>
        /// <param name="filename"></param>
        /// <param name="attachmentUuid"></param>
        /// <param name="image"></param>
        /// <param name="thumbURL">Only supply if image == ImageType.Other</param>
        public void AddResult(string uuid, string URL, string title, string keywords, string filename, string attachmentUuid, DateTime lastModified, string thumbURL)
        {
            tableLayoutPanel.Show();
            lblNoResults.Hide();

            ResultPanel result = new ResultPanel();
            result.BackColor = Color.White;
            result.ItemUuid = uuid;
            result.ItemURL = URL;
            result.ItemName = title;
            result.Keywords = keywords;
            result.AttachmentFilename = filename;
            result.AttachmentUuid = attachmentUuid;
            result.LastModifiedDate = lastModified;
            result.SetThumbnail(filename, thumbURL);
            tableLayoutPanel.Controls.Add(result);

            result.MouseClick += new MouseEventHandler(Result_MouseClick);
            result.MouseDoubleClick += new MouseEventHandler(Result_MouseDoubleClick);
            result.MouseEnter += new EventHandler(Result_MouseEnter);
            result.MouseLeave += new EventHandler(Result_MouseLeave);
            foreach (Control control in result.Controls)
            {
                control.MouseClick += new MouseEventHandler(Result_MouseClick);
                control.MouseDoubleClick += new MouseEventHandler(Result_MouseDoubleClick);
                control.MouseEnter += new EventHandler(Result_MouseEnter);
                control.MouseLeave += new EventHandler(Result_MouseLeave);
            }

            //tableLayoutPanel.SetBounds(0, 0, ClientSize.Width, ROW_HEIGHT * (tableLayoutPanel.Controls.Count / 2));
        }



        private void HighlightResult(ResultPanel result)
        {
            foreach (Control control in tableLayoutPanel.Controls)
            {
                if (control == result)
                {
                    control.BackColor = Color.LemonChiffon;
                }
                else
                {
                    control.BackColor = Color.White;
                }
            }
        }

        private void Result_MouseLeave(object sender, EventArgs e)
        {
            HighlightResult(null);
        }

        private void Result_MouseEnter(object sender, EventArgs e)
        {
            HighlightResult(FindResultPanelSender(sender));
        }

        private void Result_MouseDoubleClick(object sender, MouseEventArgs e)
        {
            OnSelection((ItemSelection)FindResultPanelSender(sender));
        }

        private void Result_MouseClick(object sender, MouseEventArgs e)
        {
            HighlightResult(FindResultPanelSender(sender));
        }

        private ResultPanel FindResultPanelSender(object sender)
        {
            Control ctrl = (Control)sender;
            while (ctrl != null && !(ctrl is ResultPanel))
            {
                ctrl = ctrl.Parent;
            }
            return (ResultPanel)ctrl;
        }

        private void OnLayout(object sender, LayoutEventArgs e)
        {
            int w = ClientSize.Width;
            //int h = ClientSize.Height;
            int controlCount = tableLayoutPanel.Controls.Count;
            tableLayoutPanel.SetBounds(0, 0, w, ROW_HEIGHT * (controlCount / 2 + controlCount % 2));

            foreach (RowStyle style in tableLayoutPanel.RowStyles)
            {
                style.SizeType = SizeType.Absolute;
                style.Height = ROW_HEIGHT;
            }
           
            int controlWidth =  tableLayoutPanel.ClientSize.Width / 2;
            foreach (Control control in tableLayoutPanel.Controls)
            {
                control.Width = controlWidth;
            }

            lblNoResults.Location = new Point(ClientSize.Width / 2 - lblNoResults.Width / 2, ClientSize.Height / 2 - lblNoResults.Height / 2);
        }

        public EquellaOfficeScrapbookIntegration Module
        {
            get;
            set;
        }
    }
}
