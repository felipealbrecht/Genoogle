package bio.pih.genoogle.interfaces.webservices;

import java.util.List;

import javax.xml.ws.BindingProvider;

import bio.pih.genoogle.io.XMLConfigurationReader;


public class WebServicesClient {

	private WebServicesService webServicesService;
	private WebServices proxy;
	
	private static WebServicesClient singleton = null;

	public WebServicesClient() {
		webServicesService = new WebServicesService();
		proxy = webServicesService.getWebServicesPort();		
        if (XMLConfigurationReader.useSessions()) {
    		((BindingProvider) proxy).getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
        }
	}
	
	public synchronized static WebServicesClient getInstance() {
		if (singleton == null) {
			singleton = new WebServicesClient();
		}
		return singleton;
	}
	
	public String getName() {
		return proxy.name();
	}
	
	public Double getVersion() {
		return proxy.version();
	}
	
	public List<String> getDatabanks() {
		return proxy.databanks();
	}
	
	public List<String> getParameters() {
		return proxy.parameters();
	}
	
	public boolean setParameter(String parameter, String value) {
		return proxy.setParameter(parameter, value);
	}
	
	public String doSearch(String query, String databank) {
		return proxy.search(query, databank);
	}
	
	public String doSearch(String query, String databank, List<String> parametersList) {
		return proxy.searchWithParameters(query, databank, parametersList);
	}
	
	public static void main(String[] args) {
		new WebServicesClient();
	}
}
