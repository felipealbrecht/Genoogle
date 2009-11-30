
package bio.pih.genoogle.interfaces.webservices;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the bio.pih.genoogle.interfaces.webservices package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _SearchWithParameters_QNAME = new QName("http://webservices.interfaces.genoogle.pih.bio", "searchWithParameters");
    private final static QName _DatabanksResponse_QNAME = new QName("http://webservices.interfaces.genoogle.pih.bio", "databanksResponse");
    private final static QName _Parameters_QNAME = new QName("http://webservices.interfaces.genoogle.pih.bio", "parameters");
    private final static QName _SearchWithParametersResponse_QNAME = new QName("http://webservices.interfaces.genoogle.pih.bio", "searchWithParametersResponse");
    private final static QName _Name_QNAME = new QName("http://webservices.interfaces.genoogle.pih.bio", "name");
    private final static QName _SetParameter_QNAME = new QName("http://webservices.interfaces.genoogle.pih.bio", "setParameter");
    private final static QName _Search_QNAME = new QName("http://webservices.interfaces.genoogle.pih.bio", "search");
    private final static QName _VersionResponse_QNAME = new QName("http://webservices.interfaces.genoogle.pih.bio", "versionResponse");
    private final static QName _Version_QNAME = new QName("http://webservices.interfaces.genoogle.pih.bio", "version");
    private final static QName _ParametersResponse_QNAME = new QName("http://webservices.interfaces.genoogle.pih.bio", "parametersResponse");
    private final static QName _Databanks_QNAME = new QName("http://webservices.interfaces.genoogle.pih.bio", "databanks");
    private final static QName _SearchResponse_QNAME = new QName("http://webservices.interfaces.genoogle.pih.bio", "searchResponse");
    private final static QName _NameResponse_QNAME = new QName("http://webservices.interfaces.genoogle.pih.bio", "nameResponse");
    private final static QName _SetParameterResponse_QNAME = new QName("http://webservices.interfaces.genoogle.pih.bio", "setParameterResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: bio.pih.genoogle.interfaces.webservices
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link SearchResponse }
     * 
     */
    public SearchResponse createSearchResponse() {
        return new SearchResponse();
    }

    /**
     * Create an instance of {@link DatabanksResponse }
     * 
     */
    public DatabanksResponse createDatabanksResponse() {
        return new DatabanksResponse();
    }

    /**
     * Create an instance of {@link VersionResponse }
     * 
     */
    public VersionResponse createVersionResponse() {
        return new VersionResponse();
    }

    /**
     * Create an instance of {@link Version }
     * 
     */
    public Version createVersion() {
        return new Version();
    }

    /**
     * Create an instance of {@link SetParameter }
     * 
     */
    public SetParameter createSetParameter() {
        return new SetParameter();
    }

    /**
     * Create an instance of {@link Name }
     * 
     */
    public Name createName() {
        return new Name();
    }

    /**
     * Create an instance of {@link NameResponse }
     * 
     */
    public NameResponse createNameResponse() {
        return new NameResponse();
    }

    /**
     * Create an instance of {@link SearchWithParametersResponse }
     * 
     */
    public SearchWithParametersResponse createSearchWithParametersResponse() {
        return new SearchWithParametersResponse();
    }

    /**
     * Create an instance of {@link SearchWithParameters }
     * 
     */
    public SearchWithParameters createSearchWithParameters() {
        return new SearchWithParameters();
    }

    /**
     * Create an instance of {@link SetParameterResponse }
     * 
     */
    public SetParameterResponse createSetParameterResponse() {
        return new SetParameterResponse();
    }

    /**
     * Create an instance of {@link ParametersResponse }
     * 
     */
    public ParametersResponse createParametersResponse() {
        return new ParametersResponse();
    }

    /**
     * Create an instance of {@link Search }
     * 
     */
    public Search createSearch() {
        return new Search();
    }

    /**
     * Create an instance of {@link Parameters }
     * 
     */
    public Parameters createParameters() {
        return new Parameters();
    }

    /**
     * Create an instance of {@link Databanks }
     * 
     */
    public Databanks createDatabanks() {
        return new Databanks();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SearchWithParameters }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.interfaces.genoogle.pih.bio", name = "searchWithParameters")
    public JAXBElement<SearchWithParameters> createSearchWithParameters(SearchWithParameters value) {
        return new JAXBElement<SearchWithParameters>(_SearchWithParameters_QNAME, SearchWithParameters.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DatabanksResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.interfaces.genoogle.pih.bio", name = "databanksResponse")
    public JAXBElement<DatabanksResponse> createDatabanksResponse(DatabanksResponse value) {
        return new JAXBElement<DatabanksResponse>(_DatabanksResponse_QNAME, DatabanksResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Parameters }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.interfaces.genoogle.pih.bio", name = "parameters")
    public JAXBElement<Parameters> createParameters(Parameters value) {
        return new JAXBElement<Parameters>(_Parameters_QNAME, Parameters.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SearchWithParametersResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.interfaces.genoogle.pih.bio", name = "searchWithParametersResponse")
    public JAXBElement<SearchWithParametersResponse> createSearchWithParametersResponse(SearchWithParametersResponse value) {
        return new JAXBElement<SearchWithParametersResponse>(_SearchWithParametersResponse_QNAME, SearchWithParametersResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Name }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.interfaces.genoogle.pih.bio", name = "name")
    public JAXBElement<Name> createName(Name value) {
        return new JAXBElement<Name>(_Name_QNAME, Name.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SetParameter }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.interfaces.genoogle.pih.bio", name = "setParameter")
    public JAXBElement<SetParameter> createSetParameter(SetParameter value) {
        return new JAXBElement<SetParameter>(_SetParameter_QNAME, SetParameter.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Search }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.interfaces.genoogle.pih.bio", name = "search")
    public JAXBElement<Search> createSearch(Search value) {
        return new JAXBElement<Search>(_Search_QNAME, Search.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link VersionResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.interfaces.genoogle.pih.bio", name = "versionResponse")
    public JAXBElement<VersionResponse> createVersionResponse(VersionResponse value) {
        return new JAXBElement<VersionResponse>(_VersionResponse_QNAME, VersionResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Version }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.interfaces.genoogle.pih.bio", name = "version")
    public JAXBElement<Version> createVersion(Version value) {
        return new JAXBElement<Version>(_Version_QNAME, Version.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ParametersResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.interfaces.genoogle.pih.bio", name = "parametersResponse")
    public JAXBElement<ParametersResponse> createParametersResponse(ParametersResponse value) {
        return new JAXBElement<ParametersResponse>(_ParametersResponse_QNAME, ParametersResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Databanks }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.interfaces.genoogle.pih.bio", name = "databanks")
    public JAXBElement<Databanks> createDatabanks(Databanks value) {
        return new JAXBElement<Databanks>(_Databanks_QNAME, Databanks.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SearchResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.interfaces.genoogle.pih.bio", name = "searchResponse")
    public JAXBElement<SearchResponse> createSearchResponse(SearchResponse value) {
        return new JAXBElement<SearchResponse>(_SearchResponse_QNAME, SearchResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NameResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.interfaces.genoogle.pih.bio", name = "nameResponse")
    public JAXBElement<NameResponse> createNameResponse(NameResponse value) {
        return new JAXBElement<NameResponse>(_NameResponse_QNAME, NameResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SetParameterResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservices.interfaces.genoogle.pih.bio", name = "setParameterResponse")
    public JAXBElement<SetParameterResponse> createSetParameterResponse(SetParameterResponse value) {
        return new JAXBElement<SetParameterResponse>(_SetParameterResponse_QNAME, SetParameterResponse.class, null, value);
    }

}
