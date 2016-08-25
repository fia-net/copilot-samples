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
package com.fianet.certissim.copilot.example;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.Charsets;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


/**
 *
 */
public class CopilotRequest {
	CopilotConfig cfg;

	public CopilotRequest(CopilotConfig cfg) {
		this.cfg = cfg;
	}

	/**
	 * Compute the "auth" parameter value for a request, with the HMAC-SHA1
	 * algorithm.
	 * Uses Apache Common Codec package.
	 *
	 * @param mySiteID my identifier (as provided by Fia-Net)
	 * @param myAuthKeyHex my secret key (as provided by Fia-Net) in hexadecimal
	 * @param payload the payload XML document.
	 * @return the hexadecimal HMAC-SHA1 digest, as expected by copilot.

	 * @throws Exception if a problem arises during the HMAC computation.
	 */
	private String computeAuthParameter (int mySiteID, String myAuthKeyHex, String payload, Charset charset) throws Exception {
		byte[] k = Hex.decodeHex(myAuthKeyHex.toCharArray());
		SecretKeySpec key = new SecretKeySpec(k, "HmacSHA1");

		Mac mac = Mac.getInstance("HmacSHA1");
		mac.init(key);

		return Hex.encodeHexString(mac.doFinal(payload.getBytes(charset)));
	}

	/**
	 * Call copilot through an http POST request, using the CopilotConfig
	 * instance for "siteid" and "auth" HTTP parameters.
	 *
	 * @param payload the XML document containing the request data, as a string.
	 * @param charset the Charset used by the payload string. It is required
	 * for HMAC computation. Only UTF-8 and ISO-8859-1 are supported by the
	 * web-service.

	 * @return a CopilotResponse object, containing the authentication level
	 * and some status information.
	 *
	 * @throw RuntimeException when the charset parameter is neither UTF-8 not
	 * ISO-8859-1, or when SiteID or AuthKeyHex are undefined in the owned
	 * CopilotConfig instance.
	 */
	public CopilotResponse execute (String payload, Charset charset) throws RuntimeException {
		if (cfg.getSiteId() == 0 || cfg.getAuthKeyHex().isEmpty()) {
			throw new RuntimeException ("CopilotConfig instance is missing siteid or auth key values.");
		}
		return this.execute (cfg.getSiteId(), cfg.getAuthKeyHex(), payload, charset);
	}

	/**
	 * Call copilot through an http POST request, according to the
	 * Technical Integration Guide.
	 *
	 * @param mySiteID my identifier (as provided by Fia-Net).
	 * @param myAuthKeyHex my secret key (as provided by Fia-Net) in hexadecimal
	 * @param payload the XML document containing the request data, as a string.
	 * @param charset the character set used by the payload string. It is
	 * required for HMAC computation. Only UTF-8 and ISO-8859-1 are supported
	 * by the web-service.
	 * @return a CopilotResponse object, containing the authentication level
	 * and some status information.
	 * @throw RuntimeException when the charset parameter is neither UTF-8 not ISO-8859-1
	 */
	public CopilotResponse execute (int mySiteID, String myAuthKeyHex, String payload, Charset charset) throws RuntimeException {

		if (!charset.equals(Charsets.ISO_8859_1) && !charset.equals(Charsets.UTF_8)) {
			throw new RuntimeException ("Charset "+charset.name()+" not supported !");
		}

		// The response contains our default authentication level at first. It will be updated accordingly on successful HTTP requests.
		CopilotResponse response = new CopilotResponse(cfg.getDefaultAuthLevel());
		Request httpReq = org.apache.http.client.fluent.Request.Post(cfg.getUri());

		try {
			String auth = computeAuthParameter(mySiteID, myAuthKeyHex, payload, charset);

			httpReq.connectTimeout(cfg.getTimeoutMs());
			httpReq.socketTimeout(cfg.getTimeoutMs());

			httpReq.bodyForm (
				Form.form()
				.add("siteid", String.valueOf(mySiteID)).add("auth", auth)
				.add("payload", payload)
				.build(),
				charset
			);

			HttpResponse httpResp = httpReq.execute().returnResponse();
			response.setStatusCode(httpResp.getStatusLine().getStatusCode());
			response.setStatusText(httpResp.getStatusLine().getReasonPhrase());

			ContentType ctype = ContentType.getOrDefault(httpResp.getEntity());

			// Responses containing an XML document are parsed further.
			if (ctype.getMimeType().equals ("text/xml") || ctype.getMimeType().equals ("application/xml")) {
				DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document doc = builder.parse (httpResp.getEntity().getContent());
				XPath xpath = XPathFactory.newInstance().newXPath();

				String version = (String) xpath.evaluate ("/paymentAuthResponse/@version", doc, XPathConstants.STRING);
				if (!version.equals("1.0")) {
					System.err.println ("WARNING: Unexpected copilot response version "+version);
				}

				response.setTechnicalId ((String) xpath.evaluate ("/paymentAuthResponse/@id", doc, XPathConstants.STRING));
				response.setAuthLevel ((String) xpath.evaluate ("/paymentAuthResponse/authLevel/text()", doc, XPathConstants.STRING));

			// Other content-types are copied "as-is" in the CopilotResponse.documentBody property.
			} else {
				System.err.println ("ERROR: Did not receive XML data!");

				String str = IOUtils.toString (httpResp.getEntity().getContent(), ContentType.getOrDefault(httpResp.getEntity()).getCharset());

				response.setDocumentBody(str);
			}

		// Modify this section to match your exception-handling requirements !
		} catch (SocketTimeoutException e) {
			httpReq.abort();
			System.err.println ("ERROR: Socket timeout.");
			response.setDocumentBody(e.getMessage());

		} catch (ClientProtocolException e) {
			httpReq.abort();
			System.err.println ("ERROR: Client protocol error.");
			response.setDocumentBody(e.getMessage());

		} catch (IOException e) {
			httpReq.abort();
			System.err.println ("ERROR: I/O error.");
			response.setDocumentBody(e.getMessage());

		} catch (SAXException e) {
			System.err.println ("ERROR: XML parse error.");
			response.setDocumentBody(e.getMessage());

		} catch (Exception e) {
			System.err.println ("ERROR: Unexpected exception caught.");
			response.setDocumentBody(e.getMessage());
		}

		return response;
	}
}
