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
namespace Fianet.Certissim.Copilot.Example
{
	public class CopilotConfig
	{
		private string uri;
		private int timeoutMs;
		private string defaultAuthLevel;
		private int siteId;
		private string authKeyHex;


		public string Uri 
		{
			get {
				return uri;
			}
			internal set {
				uri = value;
			}
		}
		public int TimeoutMs
		{
			get {
				return timeoutMs;
			}
			internal set {
				timeoutMs = value;
			}
		}
		public string DefaultAuthLevel
		{
			get {
				return defaultAuthLevel;
			}
			internal set {
				defaultAuthLevel = value;
			}
		}
		public int SiteId
		{
			get {
				return siteId;
			}
			internal set {
				siteId = value;
			}
		}
		public string AuthKeyHex
		{
			get {
				return authKeyHex;
			}
			internal set {
				authKeyHex = value;
			}
		}


		/// <summary>
		/// Initializes a new instance of the <see cref="Fianet.Certissim.Copilot.Example.CopilotConfig"/> class.
		/// </summary>
		/// <param name="uri">The webservice URI.</param>
		/// <param name="mySiteId">My site identifier.</param>
		/// <param name="myAuthKeyHex">My secret key in hexadecimal.</param>
		/// <param name="timeoutMs">Request timeout in milliseconds.</param>
		/// <param name="defaultAuthLevel">The default authentication level, to be used in case of error.</param>
		public CopilotConfig (string uri, int mySiteId, string myAuthKeyHex, int timeoutMs, string defaultAuthLevel)
		{
			Uri = uri;
			TimeoutMs = timeoutMs;
			DefaultAuthLevel = defaultAuthLevel;
			SiteId = mySiteId;
			AuthKeyHex = myAuthKeyHex;
		}

		/// <summary>
		/// Initializes a new instance of the <see cref="Fianet.Certissim.Copilot.Example.CopilotConfig"/> class.
		/// </summary>
		/// <param name="uri">The webservice URI.</param>
		/// <param name="timeoutMs">Request timeout in milliseconds.</param>
		/// <param name="defaultAuthLevel">Default authentication level.</param>
		public CopilotConfig (string uri, int timeoutMs, string defaultAuthLevel) 
		{
			Uri = uri;
			TimeoutMs = timeoutMs;
			DefaultAuthLevel = defaultAuthLevel;
		}

	}
}

