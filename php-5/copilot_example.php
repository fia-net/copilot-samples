<?php
/*
		copilot_example.php

		Sample source code to call the Payment Authentication web-service
		in PHP, using libcurl and simplexml extensions.
		
		This file has been written to demonstrate the logic to be used in
		order to call the copilot.cgi web-service and handle errors, as
		described in the Technical Integration Guide.
		
		Copyright (c) 2014 FIA-NET
		
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

/**
 * Configuration
 * This array regroups some settings used to call the copilot
 * web-service.
 */
$COPILOT_CONFIG = array (
	// The web-service location:
	"url" => "https://change-here-to/copilot.cgi",

	// My merchant account identifier
	"siteid" => 0 /*MY_SITE_ID HERE */,

	// My secret key (in hexadecimal, WITHOUT the "0x" prefix)
	"key" => 'abcdef' /*MY_KEY HERE */,

	// The web-service must reply within 2 seconds.
	"timeout" => 2,

	/*
	 * When a timeout or an error occurs, we want to use the highest
	 * authorizaton level possible (the value comes from the
	 * Technical Integration Guide)
	 */
	"defaultAuthLevel" => "high/required"
);


/**
 * Call the Certissim web-service, using libcurl.
 *
 * @param string $url is the location of the web-service
 * @param int $timeout is the number of seconds allowed for receiving the
 * response.
 * @param int $mysiteid is my merchant account identifier.
 * @param string $mykey is my merchand account secret key (hexadecimal string)
 * @param string $xmldoc is the XML document to send (the "payload" parameter).
 * @param array $response is the response obtained. It is an associative array
 *        of the following form : 
 * array (
 * 			"errorCode" => int       // libcurl error code (0 if success).
 * 			"errorText" => string    // libcurl error text (null if success).
 *			"statusCode" => string   // HTTP status code (numeric value only, null if libcurl error).
 *			"contentType" => string  // HTTP Content-Type header value, null if libcurl error.
 *			"body" => string         // HTTP response body, null if libcurl error.
 *		)
 */
function copilot_send_request ($url, $timeout, $mysiteid, $mykey, &$xmldoc, &$response)
{
	// Compute the "auth" parameter (in its hexadecimal form)
	$auth = hash_hmac ("sha1", $xmldoc, pack('H*', $mykey), false);

	// Build the URL-encoded parameters
	$postdata = "siteid=".(int)$mysiteid. '&auth='.$auth.'&payload='.urlencode($xmldoc);

	// Use libcurl for transfering data
	$curl = curl_init();

	$ops = array (
		CURLOPT_RETURNTRANSFER => true,
		CURLOPT_POST => true,
		CURLOPT_TIMEOUT => $timeout,
		CURLOPT_URL => $url,
		CURLOPT_POSTFIELDS => $postdata
	);
	curl_setopt_array ($curl, $ops);

	$body = curl_exec($curl);
	$curlErrno = curl_errno($curl);

	if ($curlErrno == 0) {
		$statusCode = (int) curl_getinfo ($curl, CURLINFO_HTTP_CODE);
		$contentType = curl_getinfo ($curl, CURLINFO_CONTENT_TYPE);

		$response = array (
			"internalErrorCode" => 0,
			"internalErrorText" => null,
			"httpStatusCode"  => $statusCode,
			"httpContentType" => $contentType,
			"body"        => $body
		);

	} else {
		$response = array (
			"internalErrorCode" => $curlErrno,
			"internalErrorText" => curl_error($curl),
			"httpStatusCode"  => null,
			"httpContentType" => null,
			"body"        => null
		);
	}

	curl_close ($curl);

	return ($curlErrno == 0);
}


/**
 * Simple error logging function.
 *
 * A function like this one should be used to store error details,
 * in order to provide some data for analysis.
 *
 * @param string $msg: log message.
 * @param string $callId the unique identifier of my call.
 * @param array $config the copilot web-service configuration.
 * @param string $xmldoc the XML document sent to the web-service ("payload"
 * parameter).
 * @param array $response the response returned by the copilot_send_request()
 * function.
 */
function record_error ($msg, $callId, &$config, &$xmldoc, &$response)
{
	echo "<hr width=\"100%\">";
	echo "<h2>Error</h2>";
	echo "<h3>An error occured for call '". $callId. "' :</h3>";
	echo "<h4>".$msg."</h4>";
	echo "<p>Configuration: </p>";
	var_dump ($config);
	echo "<p>XML document sent: </p>";
	var_dump ($xmldoc);
	echo "<p>Response :</p>";
	var_dump ($response);
	echo "<br />";
	echo "<hr width=\"100%\">";
}

/**
 * Get the payment authorization level from Certissim, using :
 * - the copilot_send_request() function defined herein
 * - the simplexml PHP extention OR the DOM PHP extension.
 *
 * When an error occurs, the authorization level is set to the highest
 * possible (from the value set in the $config parameter).
 *
 * @param string $callId the unique identifier of the call (e.g. the client
 * session id).
 * @param array $config the configuration used to call the web-service.
 * @param string $xmldoc the XML document to be sent to the web-service (must
 * have a "/paymentAuthRequest/" root element).
 *
 * @return array containing the authentication level to apply and
 * the technical id to be sent to Certissim Eval.
 */
function get_payment_auth ($callId, &$config, &$xmldoc)
{
	$technicalId = null;
	$authLevel = null;

	// Send the HTTP request using the default configuration info
	$success = copilot_send_request ($config['url'], $config['timeout'], $config['siteid'], $config['key'], $xmldoc, $response);

	// Handle external (e.g. libcurl) failures (including timeouts)
	if (!$success) {

		// Use the default auth Level.
		$authLevel = $config['defaultAuthLevel'];

		// And perform some specific treatments here, for example:
		// Save the state for later examination
		record_error ("get_payment_auth(): libcurl error. Using default auth level.", $callId, $config, $xmldoc, $response);

	} else {
		// Examine the HTTP status code
		$status = $response['httpStatusCode'];

		if ($status == 200) { // "200 OK" : full success

			// The reply must be an XML document.
			if (strncmp ($response['httpContentType'], "text/xml", strlen("text/xml")) != 0) {
				echo "ERROR ! the web-service did not return an XML document !";
				return false;
			}
/**********
			// Uncomment the following code when using the DOM extension (and comment the simplexml variant) :
  			$doc = new DOMDocument();
			$doc->loadXML($response['body']);
			$xpath = new DOMXPath($doc);

			$technicalId = $xpath->query("/paymentAuthResponse[@version='1.0']")->item(0)->getAttribute('id');
			$authLevel = $xpath->query("/paymentAuthResponse[@version='1.0']/authLevel")->item(0)->nodeValue;
/**********/

/**********/
			// Uncomment the following code when using the simplexml extension :
			$xml = simplexml_load_string ($response['body']);
			$node = $xml->xpath("/paymentAuthResponse[@version='1.0']");

			$technicalId = (string) $node[0]['id'];
			$authLevel = (string) $node[0]->authLevel;
/**********/

		} elseif ($status >= 400  && $status < 500) { // 4xx errors: client-side

			// Use the default auth Level.
			$authLevel = $config['defaultAuthLevel'];

			// And perform some specific treatments here, for example:
			// Save the state for later examination
			record_error ("get_payment_auth(): Client error. Using default auth level.", $callId, $config, $xmldoc, $response);

		} elseif ($status >= 500 && $status < 600) { // 5xx errors: server-side

			// Use the default auth Level.
			$authLevel = $config['defaultAuthLevel'];

			// And perform some specific treatments here, for example:
			// Save the state for later examination
			record_error ("get_payment_auth(): Server error. Using default auth level.", $callId, $config, $xmldoc, $response);

		} else { // Unknown status code

			// Use the default auth Level.
			$authLevel = $config['defaultAuthLevel'];

			// And perform some specific treatments here, for example:
			// Save the state for later examination
			record_error ("get_payment_auth(): Unknown HTTP status code. Using default auth level.", $callId, $config, $xmldoc, $response);
		}
	}

	return array (
		"technicalId" => $technicalId,
		"authLevel" => $authLevel
	);
}

// Set this to a unique identifier (e.g the user's shopping cart id or session)
$myCallId = "My identifier";

// Set this to my SiteID
$mySiteId = $COPILOT_CONFIG['siteid'];

$xml = <<<ENDXML
<?xml version="1.0" encoding="ISO-8859-1" ?>
<paymentAuthRequest version="1.0" id="{$myCallId}">
<control>
  <adresse type="facturation" format="1">
    <rue1>15 rue spencer &amp; spencer</rue1>
    <cpostal>75008</cpostal>
    <ville>paris</ville>
    <pays>FRANCE</pays>
  </adresse>
  <utilisateur type="facturation" qualite="1">
   <nom>dupont</nom>
   <prenom>jean rené</prenom>
   <email>jean.dupont@test.com</email>
  </utilisateur>
  <infocommande>
   <siteid>{$mySiteId}</siteid>
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
ENDXML;
?>
<!doctype html>
<html>
<body>
	<h1>Calling...</h1>
<?php
	$results = get_payment_auth ($myCallId, $COPILOT_CONFIG, $xml);
?>
	<h1> Call result </h1>
<?php
	var_dump ($results);
?>
</body>
</html>
