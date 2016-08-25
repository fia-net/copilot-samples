#!/usr/bin/env python
#******************************************************************************
#		copilot_example.py
#
#		Sample source code to call the Payment Authentication web-service
#		in Python and using pycurl.
#		
#		This file has been written to demonstrate the logic to be used in
#		order to call the copilot.cgi web-service and handle errors, as
#		described in the Technical Integration Guide.
#
#		Copyright (c) 2014 FIA-NET
#		
#		Permission to use, copy, modify, and/or distribute this software for
#		any purpose with or without fee is hereby granted, provided that the
#		above copyright notice and this permission notice appear in all
#		copies.
#		
#		THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
#		WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
#		WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE
#		AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
#		DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA
#		OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER
#		TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
#		PERFORMANCE OF THIS SOFTWARE.
#******************************************************************************

import sys
import hmac
import hashlib
import binascii
import pycurl
try:
	# python 3
	from urllib.parse import urlencode
except ImportError:
	# python 2
	from urllib import urlencode
try:
	# python 3
	from io import BytesIO
except ImportError:
	# python 2
	from StringIO import StringIO as BytesIO
import xml.etree.ElementTree as ET


#
# Configuration
# This array regroups some settings used to call the copilot
# web-service.
#
COPILOT_CONFIG = {
	# The web-service location:
	"url" : "https://change-here-to/copilot.cgi",

	# My merchant account identifier
	"siteid" : 0,	### MY_SITE_ID HERE

	# My secret key (in hexadecimal, WITHOUT the "0x" prefix)
	"key" : 'abcdef', ### MY_KEY HERE ###

	# The web-service must reply within 2 seconds.
	"timeout" : 2,

	# When a timeout or an error occurs, we want to use the highest
	# authorizaton level possible (the value comes from the
	# Technical Integration Guide)
	"defaultAuthLevel" : "high/required"
}


def copilot_send_request (url, timeout, mysiteid, mykey, xmldoc):
	"""
	Call the Certissim web-service.
	
	@param string url is the location of the web-service
	@param int timeout is the number of seconds allowed for receiving the
	 response.
	@param int mysiteid is my merchant account identifier.
	@param string mykey is my merchand account secret key (hexadecimal string)
	@param string xmldoc is the XML document to send (the "payload" parameter).
	@return dict the response obtained as a dictionary of the following form : 
	  {
	    "success" : bool        # True if success.
	    "errorText" : string    # error text (None if success).
	    "statusCode" : string   # HTTP status code (numeric value only, None if error).
	    "contentType" : string  # HTTP Content-Type header value, None if error.
	    "body" : string         # HTTP response body, None if error.
	  }
	"""

	# Compute the "auth" parameter (in its hexadecimal form)
	auth = hmac.HMAC(binascii.unhexlify(mykey), msg=xmldoc, digestmod=hashlib.sha1)
	auth = auth.hexdigest()

	# Build the URL-encoded parameters
	postdata = urlencode( {'siteid': mysiteid, 'auth': auth, 'payload': xmldoc} )

	# Use libcurl for transfering data
	curl = pycurl.Curl()
	curl.setopt(pycurl.URL, url)
	curl.setopt(pycurl.POST, True)
	curl.setopt(pycurl.TIMEOUT, timeout)
	curl.setopt(pycurl.POSTFIELDS, postdata)
	buffer = BytesIO()
	curl.setopt(pycurl.WRITEFUNCTION, buffer.write)
	try:
		curl.perform()
		response = {
			"success" : True,
			"internalErrorText" : None,
			"httpStatusCode"  : curl.getinfo(pycurl.HTTP_CODE),
			"httpContentType" : curl.getinfo(pycurl.CONTENT_TYPE),
			"body" : buffer.getvalue()
		}
	except pycurl.error:
		response = {
			"success" : False,
			"internalErrorText" : curl.errstr(),
			"httpStatusCode"  : None,
			"httpContentType" : None,
			"body" : None
		}
		
	curl.close()
	return response


def record_error(msg, callId, config, xmldoc, response):
	"""
	Simple error logging function.

	A function like this one should be used to store error details,
	in order to provide some data for analysis.

	@param string msg: log message.
	@param string callId the unique identifier of my call.
	@param dict config the copilot web-service configuration.
	@param string xmldoc the XML document sent to the web-service ("payload"
	parameter).
	@param dict response the response returned by the copilot_send_request()
	function.
	"""
	
	Log.write("Error\n")
	Log.write("-----\n")
	Log.write("An error occured for call %s :\n" % callId)
	Log.write(msg)
	Log.write("\nConfiguration:\n")
	Log.write(repr(config))
	Log.write("\nXML document sent:\n")
	Log.write(repr(xmldoc))
	Log.write("\nResponse\n")
	Log.write(repr(response))
	Log.write("\n")


def get_payment_auth(callId, config, xmldoc):
	"""
	Get the payment authorization level from Certissim, using :
	- the copilot_send_request() function defined herein
	- the simplexml PHP extention OR the DOM PHP extension.

	When an error occurs, the authorization level is set to the highest
	possible (from the value set in the $config parameter).

	@param string callId the unique identifier of the call (e.g. the client
	session id).
	@param dict config the configuration used to call the web-service.
	@param string xmldoc the XML document to be sent to the web-service (must
	have a "/paymentAuthRequest/" root element).

	@return tuple containing the authentication level to apply and
	the technical id to be sent to Certissim Eval.
	"""

	technicalId = None
	authLevel = None

	# Send the HTTP request using the default configuration info
	response = copilot_send_request(config['url'], config['timeout'], config['siteid'], config['key'], xmldoc)

	# Handle external (e.g. libcurl) failures (including timeouts)
	if not response['success']:
		# Use the default auth Level.
		authLevel = config['defaultAuthLevel']

		# And perform some specific treatments here, for example:
		# Save the state for later examination
		record_error("get_payment_auth(): libcurl error. Using default auth level.", callId, config, xmldoc, response)

	else:
		# Examine the HTTP status code
		status = response['httpStatusCode']

		if status == 200: # "200 OK" : full success

			# The reply must be an XML document.
			if not response['httpContentType'].startswith("text/xml"):
				record_error("get_payment_auth(): ERROR the web-service did not return an XML document !",
					callId, config, xmldoc, response)
				return ( technicalId, authLevel )

			doc = ET.fromstring(response['body'])
			if doc.tag != "paymentAuthResponse":
				record_error("get_payment_auth(): ERROR the web-service did return a XML document with a missing paymentAuthResponse !",
					callId, config, xmldoc, response)
				return ( technicalId, authLevel )

			if doc.attrib['version'] != "1.0":
				record_error("get_payment_auth(): ERROR the web-service did return a XML document with a mismatched version !",
					callId, config, xmldoc, response)
				return ( technicalId, authLevel )

			technicalId = doc.attrib['id']
			authLevel = doc.find("authLevel").text

		elif status >= 400 and status < 500:
			# 4xx errors: client-side

			# Use the default auth Level.
			authLevel = config['defaultAuthLevel']

			# And perform some specific treatments here, for example:
			# Save the state for later examination
			record_error ("get_payment_auth(): Client error. Using default auth level.", callId, config, xmldoc, response)

		elif status >= 500 and status < 600:
			# 5xx errors: server-side

			# Use the default auth Level.
			authLevel = config['defaultAuthLevel']

			# And perform some specific treatments here, for example:
			# Save the state for later examination
			record_error ("get_payment_auth(): Server error. Using default auth level.", callId, config, xmldoc, response)

		else:
			# Unknown status code

			# Use the default auth Level.
			authLevel = config['defaultAuthLevel']

			# And perform some specific treatments here, for example:
			# Save the state for later examination
			record_error ("get_payment_auth(): Unknown HTTP status code. Using default auth level.", callId, config, xmldoc, response)

	return ( technicalId, authLevel )


xmlparam = {
	# Set this to a unique identifier (e.g the user's shopping cart id or session)
	'myCallId' : "My identifier",

	# Set this to my SiteID
	'mySiteId' : COPILOT_CONFIG['siteid']
}

xml = """<?xml version="1.0" encoding="ISO-8859-1" ?>
<paymentAuthRequest version="1.0" id="%(myCallId)s">
<control>
  <adresse type="facturation" format="1">
    <rue1>15 rue spencer &amp; spencer</rue1>
    <cpostal>75008</cpostal>
    <ville>paris</ville>
    <pays>FRANCE</pays>
  </adresse>
  <utilisateur type="facturation" qualite="1">
   <nom>dupont</nom>
   <prenom>jean ren\xe9</prenom>
   <email>jean.dupont@test.com</email>
  </utilisateur>
  <infocommande>
   <siteid>%(mySiteId)s</siteid>
   <refid>000000001</refid>
   <montant devise="eur">100.00</montant>
   <ip timestamp="2012-08-08 11:11:56">127.0.0.1</ip>
   <transport>
    <type>4</type>
    <nom>La Poste</nom>
    <rapidite>2</rapidite>
   </transport>
   <list nbproduit="1">
    <produit>the first product</produit>
   </list>
  </infocommande>
  <paiement>
  	<type>carte</type>
  </paiement>
</control>
</paymentAuthRequest>
""" % xmlparam


if __name__ == '__main__':
  # Command line argument to specify the output log file
	if len(sys.argv) == 1:
		Log = sys.stderr
	else:
		Log = open(sys.argv[1], "w")
	technicalId, authLevel = get_payment_auth(xmlparam['myCallId'], COPILOT_CONFIG, xml)
	Log.write("\nAuthentication data\n")
	Log.write("-"*19 + "\n")
	Log.write("technicalId : %s\n" % technicalId)
	Log.write("authLevel : %s\n" % authLevel)
