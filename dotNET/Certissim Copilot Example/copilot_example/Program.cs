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
using System.Text;

namespace Fianet.Certissim.Copilot.Example
{
	class MainClass
	{
		/*
		 * Definition of the default configuration properties.
		 *
		 * For this example to work, the variables hereunder must be set
		 * accordingly to the values provided by Fia-Net.
		 *
		 * Those values should not change across multiple calls, unless you have
		 * multiple siteids and authentication keys.
		 *
		 */

		/// <summary>
		/// The location of copilot.cgi, as provided by FIA-NET
		/// </summary>
		private static string COPILOT_URI = "http://www.server.name/path/to/copilot.cgi";

		/// <summary>
		/// My SiteID, as provided by FIA-NET
		/// </summary>
		private static int MY_SITEID = 0;

		/// <summary>
		/// My authentication key, in its hexadecimal form.
		/// </summary>
		private static string MY_AUTHKEY_HEX = "0123456789abcdef";


		/// <summary>
		/// This is the authentication level to be used whenever an error occurs.
		/// Fia-Net recommends using the highest authentication level available.
		/// </summary>
		private static String MY_DEFAULT_AUTHLEVEL = "high/required";

		/// <summary>
		/// This is the timeout (in milliseconds) above which we stop waiting for
		/// a response from copilot and use our default authentication level.
		/// </summary>
		private static int MY_TIMEOUT_MS = 2;

		/// <summary>
		///  The encoding defined here must be the same used in the XML document.
		/// Only UTF-8 and ISO-8859-1 are supported.
		/// </summary>
		private static Encoding MY_ENCODING = Encoding.UTF8;

		public static void Main (string[] args)
		{
			try {
				// The sample XML document...
				string xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
					"<paymentAuthRequest version=\"1.0\" id=\"YOUR_ID_HERE\">\n" +
					"<control>\n" +
					"  <adresse type=\"facturation\" format=\"1\">\n" +
					"    <rue1>15 rue spencer &amp; spencer</rue1>\n" +
					"    <cpostal>75008</cpostal>\n" +
					"    <ville>paris</ville>\n" +
					"    <pays>FRANCE</pays>\n" +
					"  </adresse>\n" +
					"  <utilisateur type=\"facturation\" qualite=\"1\">\n" +
					"   <nom>dupont</nom>\n" +
					"   <prenom>jean rene</prenom>\n" +
					"   <email>jean.dupont@test.com</email>\n" +
					"  </utilisateur>\n" +
					"  <infocommande>\n" +
					"   <siteid>678</siteid>\n" +
					"   <refid>000000001</refid>\n" +
					"   <montant devise=\"eur\">100.00</montant>\n" +
					"   <ip timestamp=\"2012-08-08 11:11:56\">127.0.0.1</ip>\n" +
					"   <transport>\n" +
					"    <type>4</type>\n" +
					"    <nom>La Poste</nom>\n" +
					"    <rapidite>2</rapidite>\n" +
					"   </transport>\n" +
					"   <list nbproduit=\"1\">\n" +
					"    <produit>the first product</produit>\n" +
					"   </list>\n" +
					"  </infocommande>\n" +
					"  <paiement>\n" +
					"  	<type>carte</type>\n" +
					"  </paiement>\n" +
					"</control>\n" +
					"</paymentAuthRequest>";

				Console.WriteLine ("*******************************************");
				Console.WriteLine ("** Calling copilot.cgi...                **");
				Console.WriteLine ("*******************************************\n");


				// Our configuration object
				CopilotConfig cfg = new CopilotConfig (
					COPILOT_URI,
					MY_SITEID,
					MY_AUTHKEY_HEX,
					MY_TIMEOUT_MS,
					MY_DEFAULT_AUTHLEVEL
				);

				// Call the web-service and get the reponse :
				CopilotResponse resp = new CopilotRequest(cfg).Execute (xml, MY_ENCODING);

				Console.WriteLine ("*******************************************");
				Console.WriteLine (" HTTP Status: "+resp.StatusText);

				if (resp.Success) {
					Console.WriteLine (" Call successful.");
				} else {
					Console.WriteLine (" An error occured. We will be using the default authentication level.");
					Console.WriteLine (" Response body: "+resp.DocumentBody);
				}

				Console.WriteLine ("*******************************************");
				Console.WriteLine (" Authentication level: "+resp.AuthLevel);

				Console.Write (" Technical ID: ");
				if (resp.TechnicalId.Length == 0)
					Console.WriteLine("<none>");
				else 
					Console.WriteLine (resp.TechnicalId);

				Console.WriteLine ("*******************************************");

			} catch (Exception e) {
				Console.Error.WriteLine ("*******************************************");
				Console.Error.WriteLine ("** Exception caught                      **");
				Console.Error.WriteLine ("*******************************************");
				Console.Error.WriteLine (e.Message);
				Console.Error.Write (e.StackTrace);
			} finally {
				Console.ReadKey ();
			}
		}
	}
}
