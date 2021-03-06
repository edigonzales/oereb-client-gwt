//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.07.28 at 05:34:43 PM CEST 
//


package ch.ehi.oereb.schemas.gml._3_2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AbstractDatumType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AbstractDatumType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/gml/3.2}IdentifiedObjectType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/gml/3.2}domainOfValidity" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/gml/3.2}scope" maxOccurs="unbounded"/>
 *         &lt;element ref="{http://www.opengis.net/gml/3.2}anchorDefinition" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/gml/3.2}realizationEpoch" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractDatumType", propOrder = {
    "domainOfValidity",
    "scope",
    "anchorDefinition",
    "realizationEpoch"
})
@XmlSeeAlso({
    ImageDatumTypeType.class,
    VerticalDatumTypeType.class,
    GeodeticDatumTypeType.class,
    EngineeringDatumTypeType.class,
    TemporalDatumBaseTypeType.class
})
public abstract class AbstractDatumTypeType
    extends IdentifiedObjectTypeType
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    protected DomainOfValidity domainOfValidity;
    @XmlElementRef(name = "scope", namespace = "http://www.opengis.net/gml/3.2", type = Scope.class)
    protected List<Scope> scope;
    @XmlElementRef(name = "anchorDefinition", namespace = "http://www.opengis.net/gml/3.2", type = AnchorDefinition.class, required = false)
    protected JAXBElement<CodeTypeType> anchorDefinition;
    @XmlElementRef(name = "realizationEpoch", namespace = "http://www.opengis.net/gml/3.2", type = RealizationEpoch.class, required = false)
    protected RealizationEpoch realizationEpoch;

    /**
     * Gets the value of the domainOfValidity property.
     * 
     * @return
     *     possible object is
     *     {@link DomainOfValidity }
     *     
     */
    public DomainOfValidity getDomainOfValidity() {
        return domainOfValidity;
    }

    /**
     * Sets the value of the domainOfValidity property.
     * 
     * @param value
     *     allowed object is
     *     {@link DomainOfValidity }
     *     
     */
    public void setDomainOfValidity(DomainOfValidity value) {
        this.domainOfValidity = value;
    }

    /**
     * Gets the value of the scope property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the scope property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getScope().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Scope }
     * 
     * 
     */
    public List<Scope> getScope() {
        if (scope == null) {
            scope = new ArrayList<Scope>();
        }
        return this.scope;
    }

    /**
     * Gets the value of the anchorDefinition property.
     * 
     * @return
     *     possible object is
     *     {@link AnchorPoint }
     *     {@link AnchorDefinition }
     *     
     */
    public JAXBElement<CodeTypeType> getAnchorDefinition() {
        return anchorDefinition;
    }

    /**
     * Sets the value of the anchorDefinition property.
     * 
     * @param value
     *     allowed object is
     *     {@link AnchorPoint }
     *     {@link AnchorDefinition }
     *     
     */
    public void setAnchorDefinition(JAXBElement<CodeTypeType> value) {
        this.anchorDefinition = value;
    }

    /**
     * Gets the value of the realizationEpoch property.
     * 
     * @return
     *     possible object is
     *     {@link RealizationEpoch }
     *     
     */
    public RealizationEpoch getRealizationEpoch() {
        return realizationEpoch;
    }

    /**
     * Sets the value of the realizationEpoch property.
     * 
     * @param value
     *     allowed object is
     *     {@link RealizationEpoch }
     *     
     */
    public void setRealizationEpoch(RealizationEpoch value) {
        this.realizationEpoch = value;
    }

}
