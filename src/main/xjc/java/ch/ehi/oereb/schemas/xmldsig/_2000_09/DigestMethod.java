//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.07.28 at 05:34:43 PM CEST 
//


package ch.ehi.oereb.schemas.xmldsig._2000_09;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

public class DigestMethod
    extends JAXBElement<DigestMethodType>
{

    protected final static QName NAME = new QName("http://www.w3.org/2000/09/xmldsig#", "DigestMethod");

    public DigestMethod(DigestMethodType value) {
        super(NAME, ((Class) DigestMethodType.class), null, value);
    }

    public DigestMethod() {
        super(NAME, ((Class) DigestMethodType.class), null, null);
    }

}
