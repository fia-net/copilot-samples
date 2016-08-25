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
 * Configuration properties of the copilot web-service calls.
 */
public class CopilotConfig {
	private String uri;
	private int timeoutMs;
	private String defaultAuthLevel;

	private int siteId;
	private String authKeyHex;

	/**
	 * Constructs an object with all its properties.
	 *
	 * @param uri the URI from which the web-service can be reached.
	 * @param mySiteId the merchant account identifier.
	 * @param myAuthKeyHex the secret key, as an hexadecimal string.
	 * @param timeoutMs the timeout value, in milliseconds. If the web-service
	 * call takes longer than the specified value, the pending call is aborted
	 * and the default authentication level is used.
	 * @param defaultAuthLevel the default authentication level to be used in
	 * case of error or timeout.
	 */
	public CopilotConfig (String uri, int mySiteId, String myAuthKeyHex, int timeoutMs, String defaultAuthLevel) {
		this.uri = uri;
		this.timeoutMs = timeoutMs;
		this.defaultAuthLevel = defaultAuthLevel;
		this.siteId = mySiteId;
		this.authKeyHex = myAuthKeyHex;
	}

	/**
	 * Constructs an object with its "main" properties only.
	 * The missing properties (siteId and authKeyHex) must be given to the
	 * CopilotRequest.exec() method.
	 *
	 * @param uri the URI from which the web-service can be reached.
	 * @param timeoutMs the timeout value, in milliseconds. If the web-service
	 * call takes longer than the specified value, the pending call is aborted
	 * and the default authentication level is used.
	 * @param defaultAuthLevel the default authentication level to be used in
	 * case of error or timeout.
	 */
	public CopilotConfig (String uri, int timeoutMs, String defaultAuthLevel) {
		this.uri = uri;
		this.timeoutMs = timeoutMs;
		this.defaultAuthLevel = defaultAuthLevel;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public int getTimeoutMs() {
		return timeoutMs;
	}

	public void setTimeoutMs(int timeoutMs) {
		this.timeoutMs = timeoutMs;
	}

	public String getDefaultAuthLevel() {
		return defaultAuthLevel;
	}

	public void setDefaultAuthLevel(String defaultAuthLevel) {
		this.defaultAuthLevel = defaultAuthLevel;
	}

	public String getAuthKeyHex() {
		return authKeyHex;
	}

	public void setAuthKeyHex(String authKeyHex) {
		this.authKeyHex = authKeyHex;
	}

	public int getSiteId() {
		return siteId;
	}

	public void setSiteId(int siteId) {
		this.siteId = siteId;
	}

}
