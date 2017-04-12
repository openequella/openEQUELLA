using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;

namespace Equella
{
    public partial class ErrorDialog : Form
    {
        public ErrorDialog()
        {
            InitializeComponent();
            Text = "An error occurred in " + Constants.APP_NAME;
        }

        public void ShowError(Exception e)
        {
            txtError.Text = e.Message + "\r\n\r\n" + e.StackTrace;
            ShowDialog();
        }

        private void ErrorDialog_Layout(object sender, LayoutEventArgs e)
        {
            txtError.Size = new Size(ClientSize.Width, ClientSize.Height);
        }
    }
}
