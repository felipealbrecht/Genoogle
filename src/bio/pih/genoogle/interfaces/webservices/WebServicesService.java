
package bio.pih.genoogle.interfaces.webservices;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.6 in JDK 6
 * Generated source version: 2.1
 * 
 */
@WebServiceClient(name = "WebServicesService", targetNamespace = "http://webservices.interfaces.genoogle.pih.bio", wsdlLocation = "http://localhost:8090/webservices?wsdl")
public class WebServicesService
    extends Service
{

    private final static URL WEBSERVICESSERVICE_WSDL_LOCATION;
    private final static Logger logger = Logger.getLogger(bio.pih.genoogle.interfaces.webservices.WebServicesService.class.getName());

    static {
        URL url = null;
        try {
            URL baseUrl;
            baseUrl = bio.pih.genoogle.interfaces.webservices.WebServicesService.class.getResource(".");
            url = new URL(baseUrl, "http://localhost:8090/webservices?wsdl");
        } catch (MalformedURLException e) {
            logger.warning("Failed to create URL for the wsdl Location: 'http://localhost:8090/webservices?wsdl', retrying as a local file");
            logger.warning(e.getMessage());
        }
        WEBSERVICESSERVICE_WSDL_LOCATION = url;
    }

    public WebServicesService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public WebServicesService() {
        super(WEBSERVICESSERVICE_WSDL_LOCATION, new QName("http://webservices.interfaces.genoogle.pih.bio", "WebServicesService"));
    }

    /**
     * 
     * @return
     *     returns WebServices
     */
    @WebEndpoint(name = "WebServicesPort")
    public WebServices getWebServicesPort() {
        return super.getPort(new QName("http://webservices.interfaces.genoogle.pih.bio", "WebServicesPort"), WebServices.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns WebServices
     */
    @WebEndpoint(name = "WebServicesPort")
    public WebServices getWebServicesPort(WebServiceFeature... features) {
        return super.getPort(new QName("http://webservices.interfaces.genoogle.pih.bio", "WebServicesPort"), WebServices.class, features);
    }

}
