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

/**
 * Container for the copilot web-services responses.
 * This object contains the relevant information returned by
 * CopilotRequest.exec() methods.
 */
public class CopilotResponse {
	private int statusCode;
	private String statusText;
	private String authLevel;
	private String technicalId;
	private String documentBody;

	CopilotResponse (String level) {
		this.authLevel = level;
		this.technicalId = "";
		this.statusText = "";
		this.documentBody = "";
	}

	/**
	 * @return true when the web-service call was successful, false otherwise.
	 */
	public boolean isSuccess() {
		return (statusCode == 200 && documentBody.isEmpty());
	}

	/**
	 * @return the HTTP status code of the corresponding request.
	 */
	public int getStatusCode() {
		return statusCode;
	}

	void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	/**
	 * @return the HTTP status text of the corresponding request.
	 */
	public String getStatusText() {
		return statusText;
	}

	void setStatusText(String statusText) {
		this.statusText = statusText;
	}

	public String getAuthLevel() {
		return authLevel;
	}

	void setAuthLevel(String authLevel) {
		this.authLevel = authLevel;
	}

	/**
	 * @return the technical ID given by the web-service or an empty String if
	 * no technical id was given.
	 */
	public String getTechnicalId() {
		return technicalId;
	}

	void setTechnicalId(String technicalId) {
		this.technicalId = technicalId;
	}

	/**
	 * Get the document body, when applicable.
	 * The data is copied from the HTTP response body whenever it does not
	 * contain an XML document, or from the Exception thrown from the
	 * CopilotRequest instance.
	 *
	 * @return the response document body.
	 */
	public String getDocumentBody() {
		return documentBody;
	}

	void setDocumentBody(String documentBody) {
		this.documentBody = documentBody;
	}

}
