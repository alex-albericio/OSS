//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.02.10 at 11:20:59 AM CST 
//


package org.hibernate.boot.jaxb.hbm.spi;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TimestampSourceEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="TimestampSourceEnum"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="db"/&gt;
 *     &lt;enumeration value="vm"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "TimestampSourceEnum")
@XmlEnum
public enum JaxbHbmTimestampSourceEnum {

    @XmlEnumValue("db")
    DB("db"),
    @XmlEnumValue("vm")
    VM("vm");
    private final String value;

    JaxbHbmTimestampSourceEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static JaxbHbmTimestampSourceEnum fromValue(String v) {
        for (JaxbHbmTimestampSourceEnum c: JaxbHbmTimestampSourceEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
