
package bio.pih.genoogle.interfaces.webservices;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for createDatabank complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="createDatabank">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="fastaFiles" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="subSequenceLength" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="mask" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="numberOfSubDatabanks" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="lowComplexityFilter" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "createDatabank", propOrder = {
    "name",
    "fastaFiles",
    "subSequenceLength",
    "mask",
    "numberOfSubDatabanks",
    "lowComplexityFilter"
})
public class CreateDatabank {

    protected String name;
    protected List<String> fastaFiles;
    protected int subSequenceLength;
    protected String mask;
    protected int numberOfSubDatabanks;
    protected int lowComplexityFilter;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the fastaFiles property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fastaFiles property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFastaFiles().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getFastaFiles() {
        if (fastaFiles == null) {
            fastaFiles = new ArrayList<String>();
        }
        return this.fastaFiles;
    }

    /**
     * Gets the value of the subSequenceLength property.
     * 
     */
    public int getSubSequenceLength() {
        return subSequenceLength;
    }

    /**
     * Sets the value of the subSequenceLength property.
     * 
     */
    public void setSubSequenceLength(int value) {
        this.subSequenceLength = value;
    }

    /**
     * Gets the value of the mask property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMask() {
        return mask;
    }

    /**
     * Sets the value of the mask property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMask(String value) {
        this.mask = value;
    }

    /**
     * Gets the value of the numberOfSubDatabanks property.
     * 
     */
    public int getNumberOfSubDatabanks() {
        return numberOfSubDatabanks;
    }

    /**
     * Sets the value of the numberOfSubDatabanks property.
     * 
     */
    public void setNumberOfSubDatabanks(int value) {
        this.numberOfSubDatabanks = value;
    }

    /**
     * Gets the value of the lowComplexityFilter property.
     * 
     */
    public int getLowComplexityFilter() {
        return lowComplexityFilter;
    }

    /**
     * Sets the value of the lowComplexityFilter property.
     * 
     */
    public void setLowComplexityFilter(int value) {
        this.lowComplexityFilter = value;
    }

}
