/*
 *  Certissim pre-payment scoring - copilot webservice -
 *  Sample JAVA call implementation.
 *
 *  This file has been written for the sole purpose of demonstrating how to
 *  call the copilot.cgi web-service and handle errors, as described in
 *  the Technical Integration Guide.
 *
 *  Copyright (c) FIA-NET 2014
 *
 *  Permission to use, copy, modify, and/or distribute this software for
 *  any purpose with or without fee is hereby granted, provided that the
 *  above copyright notice and this permission notice appear in all
 *  copies.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 *  WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE
 *  AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
 *  DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA
 *  OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER
 *  TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THIS SOFTWARE.
 *
 */

import com.fianet.certissim.copilot.example.*;
import org.apache.commons.io.Charsets;




public class CopilotExampleMain {

	/*
	 * Definition of the default configuration properties.
	 *
	 * For this example to work, the following variables must be set
	 * accordingly to the values provided by Fia-Net.
	 *
	 * Those values should not change across multiple calls, unless you have
	 * multiple siteids and authentication keys.
	 *
	 */

	// The location of copilot.cgi
	private final static String COPILOT_URI = "http://the.location/of/copilot.cgi";

	// My SiteID
	private final static int MY_SITEID = 0;

	// My authentication key, in its hexadecimal form.
	private final static String MY_AUTHKEY_HEX = "0123456789abcdef";

	/*
	 * This is the authentication level to be used whenever an error occurs.
	 * Fia-Net recommends using the highest authentication level available.
	 */
	private final static String MY_DEFAULT_AUTHLEVEL = "high/required";

	/*
	 * This is the timeout (in milliseconds) above which we stop waiting for
	 * a response from copilot and use our default authentication level.
	 */
	private final static int MY_TIMEOUT_MS = 2000;


	/*
	 * A "really really basic" sample copilot call:
	 */
	public static void main(String[] args) {
		try {
			String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
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
						"   <prenom>jean rené</prenom>\n" +
						"   <email>jean.dupont@test.com</email>\n" +
						"  </utilisateur>\n" +
						"  <infocommande>\n" +
						"   <siteid>"+String.valueOf(MY_SITEID)+"</siteid>\n" +
						"   <refid>000000001</refid>\n" +
						"   <montant devise=\"eur\">100.00</montant>\n" +
						"   <ip timestamp=\"2014-08-08 11:11:56\">127.0.0.1</ip>\n" +
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

			System.out.println ("*******************************************");
			System.out.println ("** Calling copilot.cgi...               **");
			System.out.println ("*******************************************\n");


			// Our configuration object
			CopilotConfig cfg = new CopilotConfig (
					COPILOT_URI,
					MY_SITEID,
					MY_AUTHKEY_HEX,
					MY_TIMEOUT_MS,
					MY_DEFAULT_AUTHLEVEL
			);

			// Call the web-service and get the reponse :
			CopilotResponse resp = new CopilotRequest(cfg).execute (xml, Charsets.UTF_8);


			System.out.println ("*******************************************");
			System.out.println (" HTTP Status: "+resp.getStatusText());

			if (resp.isSuccess()) {
				System.out.println (" Call successful.");
			} else {
				System.out.println (" An error occured. We will be using the default authentication level.");
				System.out.println (" Response body: "+resp.getDocumentBody());
			}

			System.out.println ("*******************************************");
			System.out.println (" Authentication level: "+resp.getAuthLevel());
			System.out.println (" Technical ID: " + (resp.getTechnicalId().isEmpty()?"<none>" : resp.getTechnicalId()) );
			System.out.println ("*******************************************");

		} catch (RuntimeException e) {
			System.out.println ("*******************************************");
			System.out.println ("** Exception caught                      **");
			System.out.println ("*******************************************");
			e.printStackTrace(System.out);
		}
	}
}
