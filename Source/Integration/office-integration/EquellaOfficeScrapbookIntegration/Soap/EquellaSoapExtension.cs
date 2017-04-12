using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Web.Services.Protocols;
using System.IO;
using System.Xml.Linq;
using System.Xml;

namespace Equella.Soap
{
    public class EquellaSoapExtension : SoapExtension
    {
        private Stream oldStream;
        private Stream newStream;

        public override Stream ChainStream(Stream stream)
        {
            oldStream = stream;
            newStream = new MemoryStream();
            return newStream;
        }

        public override object GetInitializer(Type serviceType)
        {
            return null;
        }

        public override object GetInitializer(LogicalMethodInfo methodInfo, SoapExtensionAttribute attribute)
        {
            return null;
        }

        public override void Initialize(object initializer)
        {
        }

        public override void ProcessMessage(SoapMessage message)
        {
            switch (message.Stage)
            {
                case SoapMessageStage.BeforeSerialize:
                    break;

                case SoapMessageStage.AfterSerialize:
                    newStream.Position = 0;
			        Copy(newStream, oldStream);
                    break;

                case SoapMessageStage.BeforeDeserialize:
                    newStream.Position = 0;
			        Copy(oldStream, newStream);

                    newStream.Position = 0;
                    //TextReader reader = new StreamReader(newStream);
                    XElement xml =XElement.Load(newStream);
                    

                    if (message is SoapClientMessage)
                    {
                        SoapClientMessage clientMessage = (SoapClientMessage)message;
                        if (clientMessage.Client is SoapService50Ex)
                        {
                            SoapService50Ex client = (SoapService50Ex)clientMessage.Client;
                            XNamespace soap = "http://schemas.xmlsoap.org/soap/envelope/";
                            client.headerInfo = xml.Element(soap + "Header").Element("equella");
                        }
                    }

                    newStream.Position = 0;
                    break;

                case SoapMessageStage.AfterDeserialize:
                    break;
            }
        }

        private void Copy(Stream from, Stream to)
        {
            TextReader reader = new StreamReader(from);
            TextWriter writer = new StreamWriter(to);
            writer.Write(reader.ReadToEnd());
            writer.Flush();
        }
    }

    public class EquellaSoapExtensionAttribute : SoapExtensionAttribute
    {
        public override Type ExtensionType
        {
            get
            {
                return typeof(EquellaSoapExtension);
            }
        }

        public override int Priority
        {
            get;
            set;
        }
    }
}
