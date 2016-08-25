/*
 Certissim pre-payment scoring - copilot webservice -
 Sample .NET C# call implementation.
 
 This file has been written for the sole purpose of demonstrating how to
 call the copilot.cgi web-service and handle errors, as described in
 the Technical Integration Guide.
 
 Copyright (c) FIA-NET 2014
 
 Permission to use, copy, modify, and/or distribute this software for
 any purpose with or without fee is hereby granted, provided that the
 above copyright notice and this permission notice appear in all
 copies.
 
 THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE
 AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA
 OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER
 TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 PERFORMANCE OF THIS SOFTWARE.
*/

using System;
using System.Security.Cryptography;
using System.Text;
using System.Net;
using System.IO;
using System.Xml;


namespace Fianet.Certissim.Copilot.Example
{
	/// <summary>
	/// Copilot request: This object is used to call copilot and parse its results.
	/// </summary>
	public class CopilotRequest
	{
		/// <summary>
		/// The configuration object.
		/// </summary>
		private CopilotConfig cfg;

		public CopilotRequest (CopilotConfig cfg)
		{
			this.cfg = cfg;
		}

		private byte[] HexToBin (string hex)
		{
			int len = hex.Length;
			char[] hexArray = hex.ToCharArray();
			byte[] binkey = new byte[len / 2];

			if (len % 2 != 0) {
				throw new ArgumentException ("Input string length is not even.");
			}

			for (int i = 0, j = 0; i < len; ++i, ++j) 
			{
				if (hexArray [i] >= 'a' && hexArray [i] <= 'f') {
					binkey [j] = (byte)((hexArray [i] - 'a' + 10) << 4);
				} else if (hexArray [i] >= 'A' && hexArray [i] <= 'F') {
					binkey [j] = (byte)((hexArray [i] - 'A' + 10) << 4);
				} else if (hexArray [i] >= '0' && hexArray [i] <= '9') {
					binkey [j] = (byte)((hexArray [i] - '0') << 4);
				} else {
					throw new ArgumentException ("Not a hex digit:" + hexArray [i]);
				}

				++i;

				if (hexArray [i] >= 'a' && hexArray [i] <= 'f') {
					binkey [j] += (byte)(hexArray [i] - 'a' + 10);
				} else if (hexArray [i] >= 'A' && hexArray [i] <= 'F') {
					binkey [j] += (byte)(hexArray [i] - 'A' + 10);
				} else if (hexArray [i] >= '0' && hexArray [i] <= '9') {
					binkey [j] += (byte)(hexArray [i] - '0');
				} else {
					throw new ArgumentException ("Not a hex digit:" + hexArray [i]);
				}
			}

			return binkey;
		}

		private string BinToHex (byte[] data)
		{
			const string alphabet = "0123456789abcdef";
			char[] res = new char[data.Length * 2];

			for (int i = 0, j = 0; i < data.Length; ++i) 
			{
				res[j++]= alphabet [data [i] >> 4];
				res[j++]= alphabet [data [i] & 0xF];
			}

			return new string(res);
		}

		static byte[] GetBytes(string str)
		{
			byte[] bytes = new byte[str.Length * sizeof(char)];
			System.Buffer.BlockCopy(str.ToCharArray(), 0, bytes, 0, bytes.Length);
			return bytes;
		}


		private string ComputeAuthParameter (int mySiteID, string myAuthKeyHex, string data, Encoding enc) 
		{
			HMACSHA1 hmac = new HMACSHA1 (HexToBin (myAuthKeyHex));
			return BinToHex(hmac.ComputeHash (enc.GetBytes(data)));
		}

		/// <summary>
		/// Same as  Execute (int mySiteID, string myAuthKeyHex, string payload, Encoding encoding)
		/// but can be called only when the CopilotConfiguration instance has been fully initialized
		/// (e.g. the siteid and auth key must not be empty).
		/// </summary>
		/// <param name="payload">The XML document to send, as a string.</param>
		/// <param name="encoding">The Encoding used in the XML document. Must be the same as in the XML declaration "encoding" attribute.</param>
		/// <returns>An instance of <see cref="Fianet.Certissim.Copilot.Example.CopilotRequest"/> class, with the technical ID and authentication level.</returns>
		public CopilotResponse Execute (string payload, Encoding encoding)
		{
			if (cfg.SiteId == 0 || cfg.AuthKeyHex.Length == 0) {
				throw new ArgumentException ("CopilotConfig instance is missing site id or auth key values");
			}
			return this.Execute (cfg.SiteId, cfg.AuthKeyHex, payload, encoding);
		}

		/// <summary>
		/// Call the copilot web-service using the specified mySiteID, myAuthKeyHex, payload and encoding values.
		/// </summary>
		/// <param name="mySiteID">The site identifier.</param>
		/// <param name="myAuthKeyHex">The secret key in its hexadecimal form, for the HMAC computation.</param>
		/// <param name="payload">The XML document to send, as a string.</param>
		/// <param name="encoding">The Encoding used in the XML document. Must be the same as in the XML declaration "encoding" attribute.</param>
		/// <returns>An instance of <see cref="Fianet.Certissim.Copilot.Example.CopilotRequest"/> class, with the technical ID (when available) and
		/// authentication level.</returns>
		public CopilotResponse Execute (int mySiteID, string myAuthKeyHex, string payload, Encoding encoding)
		{
			if (!encoding.EncodingName.Equals(Encoding.UTF8.EncodingName) && !encoding.EncodingName.Equals(Encoding.GetEncoding("ISO-8859-1").EncodingName)) {
				throw new NotSupportedException ("Unsupported character set " + encoding.EncodingName);
			}

			CopilotResponse myResponse = new CopilotResponse (cfg.DefaultAuthLevel);
	
			try {
				StringBuilder postData = new StringBuilder();
			
				postData.Append ("siteid=").Append (mySiteID);
				postData.Append ("&auth=").Append (ComputeAuthParameter (mySiteID, myAuthKeyHex, payload, encoding));
				postData.Append ("&payload=").Append (System.Uri.EscapeDataString(payload));
				byte[] data = encoding.GetBytes(postData.ToString());
		
				HttpWebRequest request = (HttpWebRequest) WebRequest.Create (cfg.Uri);
				request.Method = "POST";
				request.ContentType = "application/x-www-form-urlencoded; charset="+encoding.EncodingName;
				request.ContentLength = data.Length;
				request.ReadWriteTimeout = cfg.TimeoutMs;

				Stream ds = request.GetRequestStream ();
				ds.WriteTimeout = cfg.TimeoutMs;
				ds.Write (data, 0, data.Length);
				ds.Close ();

				HttpWebResponse webresp = (HttpWebResponse) request.GetResponse ();
				StreamReader sr = new StreamReader (webresp.GetResponseStream());

				myResponse.StatusCode = (int) webresp.StatusCode;
				myResponse.StatusText = webresp.StatusDescription;

				if (webresp.ContentType.StartsWith ("text/xml") || webresp.ContentType.StartsWith ("application/xml")) {
					XmlDocument doc = new XmlDocument ();
					doc.Load (sr);
					XmlNode root = doc.SelectSingleNode ("/paymentAuthResponse");

					string version = root.Attributes.GetNamedItem ("version").Value;
					if (!version.Equals ("1.0")) {
						Console.Error.WriteLine ("WARNING: Unexpected copilot response version " + version);
					}

					myResponse.TechnicalId = root.Attributes.GetNamedItem ("id").Value;
					myResponse.AuthLevel = root.SelectSingleNode ("authLevel").InnerText;

				} else {
					myResponse.DocumentBody = sr.ReadToEnd();
					Console.Error.WriteLine ("ERROR: Did not receive XML data!");
					Console.Error.WriteLine (myResponse.DocumentBody);
				}
			
			} catch (WebException e) {
				Console.Error.WriteLine ("ERROR: WebRequest aborted.");
				myResponse.DocumentBody = e.Message;
			} catch (IOException e) {
				Console.Error.WriteLine ("ERROR: I/O error.");
				myResponse.DocumentBody = e.Message;
			} catch (XmlException e) {
				Console.Error.WriteLine ("ERROR: XML error.");
				myResponse.DocumentBody = e.Message;
			} catch (Exception e) {
				Console.Error.WriteLine ("ERROR: unexpected Exception caught.");
				myResponse.DocumentBody = e.Message;
			}

			return myResponse;
		}

	}
}
