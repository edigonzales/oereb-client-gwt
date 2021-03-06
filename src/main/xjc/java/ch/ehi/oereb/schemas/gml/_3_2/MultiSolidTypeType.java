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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MultiSolidType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MultiSolidType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/gml/3.2}AbstractGeometricAggregateType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/gml/3.2}solidMember" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/gml/3.2}solidMembers" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MultiSolidType", propOrder = {
    "solidMember",
    "solidMembers"
})
public class MultiSolidTypeType
    extends AbstractGeometricAggregateTypeType
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElementRef(name = "solidMember", namespace = "http://www.opengis.net/gml/3.2", type = SolidMember.class, required = false)
    protected List<SolidMember> solidMember;
    @XmlElementRef(name = "solidMembers", namespace = "http://www.opengis.net/gml/3.2", type = SolidMembers.class, required = false)
    protected SolidMembers solidMembers;

    /**
     * Gets the value of the solidMember property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the solidMember property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSolidMember().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SolidMember }
     * 
     * 
     */
    public List<SolidMember> getSolidMember() {
        if (solidMember == null) {
            solidMember = new ArrayList<SolidMember>();
        }
        return this.solidMember;
    }

    /**
     * Gets the value of the solidMembers property.
     * 
     * @return
     *     possible object is
     *     {@link SolidMembers }
     *     
     */
    public SolidMembers getSolidMembers() {
        return solidMembers;
    }

    /**
     * Sets the value of the solidMembers property.
     * 
     * @param value
     *     allowed object is
     *     {@link SolidMembers }
     *     
     */
    public void setSolidMembers(SolidMembers value) {
        this.solidMembers = value;
    }

}
