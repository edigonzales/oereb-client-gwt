//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.07.28 at 05:34:43 PM CEST 
//


package ch.ehi.oereb.schemas.gml._3_2;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SurfaceInterpolationType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="SurfaceInterpolationType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="planar"/>
 *     &lt;enumeration value="spherical"/>
 *     &lt;enumeration value="elliptical"/>
 *     &lt;enumeration value="conic"/>
 *     &lt;enumeration value="tin"/>
 *     &lt;enumeration value="parametricCurve"/>
 *     &lt;enumeration value="polynomialSpline"/>
 *     &lt;enumeration value="rationalSpline"/>
 *     &lt;enumeration value="triangulatedSpline"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "SurfaceInterpolationType")
@XmlEnum
public enum SurfaceInterpolationTypeType {

    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("planar")
    PLANAR("planar"),
    @XmlEnumValue("spherical")
    SPHERICAL("spherical"),
    @XmlEnumValue("elliptical")
    ELLIPTICAL("elliptical"),
    @XmlEnumValue("conic")
    CONIC("conic"),
    @XmlEnumValue("tin")
    TIN("tin"),
    @XmlEnumValue("parametricCurve")
    PARAMETRIC_CURVE("parametricCurve"),
    @XmlEnumValue("polynomialSpline")
    POLYNOMIAL_SPLINE("polynomialSpline"),
    @XmlEnumValue("rationalSpline")
    RATIONAL_SPLINE("rationalSpline"),
    @XmlEnumValue("triangulatedSpline")
    TRIANGULATED_SPLINE("triangulatedSpline");
    private final String value;

    SurfaceInterpolationTypeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SurfaceInterpolationTypeType fromValue(String v) {
        for (SurfaceInterpolationTypeType c: SurfaceInterpolationTypeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
