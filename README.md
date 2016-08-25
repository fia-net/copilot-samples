# copilot-samples

Several examples that demonstrate how to call *copilot*, the pre-payment
scoring engine from Certissim, as described in the Technical 
Integration Guide.

Each sub-directory provide an example using one language or platform:

- **dotNET/** .NET 4 platform in C#. Written with MonoDevelop, should be 
compatible with VisualStudio.
- **java/** sample implementation in Java7, using Apache HttpComponents and 
Apache Codec. Built with Apache Maven, should be easily build with Ant when 
using a modern IDE.
- **php-5/** PHP 5.3+ using libcurl and DOM (or simplexml) extensions.
- **python/** should be compatible with Python versions 2 and 3, uses the
the pycurl module.

## IMPORTANT:

- The provided source code has been written for demonstration purpose only.
It *must not* be used as-is on a production platform!

- The samples will not work before the proper configuration parameters are
set (in the *CopilotConfig* objects or *COPILOT_CONFIG* structures). Those 
parameters are provided by the [Certissim support team](mailto:integration-boutique@fia-net.com) upon request:
  - Your account identifier.
  - Your private authentication key.
  - The URI where copilot.cgi can be reached.

