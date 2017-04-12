using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net;

namespace Equella.Util
{
    public class HttpException : Exception
    {
        public string Friendly
        {
            get
            {
                return Enum.GetName(typeof(HttpStatusCode), Code);
            }
        }

        public HttpStatusCode Code
        {
            get;
            set;
        }
    }
}
