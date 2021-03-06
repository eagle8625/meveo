/*
* (C) Copyright 2009-2013 Manaty SARL (http://manaty.net/) and contributors.
*
* Licensed under the GNU Public Licence, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.gnu.org/licenses/gpl-2.0.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2-147 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.02.01 at 08:25:37 PM WET 
//


package org.meveo.model.jaxb.customer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}description"/>
 *         &lt;element ref="{}externalRef1"/>
 *         &lt;element ref="{}externalRef2"/>
 *         &lt;element ref="{}company"/>
 *         &lt;element ref="{}name"/>
 *         &lt;element ref="{}address"/>
 *         &lt;element ref="{}paymentMethod"/>
 *         &lt;element ref="{}email"/>
 *         &lt;element ref="{}tel1"/>
 *         &lt;element ref="{}tel2"/>
 *         &lt;element ref="{}SIRET"/>
 *       &lt;/sequence>
 *       &lt;attribute name="code" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="creditCategory" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "description",
    "externalRef1",
    "externalRef2",
    "company",
    "name",
    "address",
    "paymentMethod",
    "email",
    "tel1",
    "tel2",
    "siret"
})
@XmlRootElement(name = "customerAccount")
public class CustomerAccount {

    @XmlElement(required = true)
    protected String description;
    @XmlElement(required = true)
    protected String externalRef1;
    @XmlElement(required = true)
    protected String externalRef2;
    @XmlElement(required = true)
    protected String company;
    @XmlElement(required = true)
    protected Name name;
    @XmlElement(required = true)
    protected Address address;
    @XmlElement(required = true)
    protected String paymentMethod;
    @XmlElement(required = true)
    protected String email;
    @XmlElement(required = true)
    protected String tel1;
    @XmlElement(required = true)
    protected String tel2;
    @XmlElement(name = "SIRET", required = true)
    protected String siret;
    @XmlAttribute(name = "code")
    protected String code;
    @XmlAttribute(name = "creditCategory")
    protected String creditCategory;

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the externalRef1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExternalRef1() {
        return externalRef1;
    }

    /**
     * Sets the value of the externalRef1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExternalRef1(String value) {
        this.externalRef1 = value;
    }

    /**
     * Gets the value of the externalRef2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExternalRef2() {
        return externalRef2;
    }

    /**
     * Sets the value of the externalRef2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExternalRef2(String value) {
        this.externalRef2 = value;
    }

    /**
     * Gets the value of the company property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCompany() {
        return company;
    }

    /**
     * Sets the value of the company property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCompany(String value) {
        this.company = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link Name }
     *     
     */
    public Name getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link Name }
     *     
     */
    public void setName(Name value) {
        this.name = value;
    }

    /**
     * Gets the value of the address property.
     * 
     * @return
     *     possible object is
     *     {@link Address }
     *     
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Sets the value of the address property.
     * 
     * @param value
     *     allowed object is
     *     {@link Address }
     *     
     */
    public void setAddress(Address value) {
        this.address = value;
    }

    /**
     * Gets the value of the paymentMethod property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPaymentMethod() {
        return paymentMethod;
    }

    /**
     * Sets the value of the paymentMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPaymentMethod(String value) {
        this.paymentMethod = value;
    }

    /**
     * Gets the value of the email property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the value of the email property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmail(String value) {
        this.email = value;
    }

    /**
     * Gets the value of the tel1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTel1() {
        return tel1;
    }

    /**
     * Sets the value of the tel1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTel1(String value) {
        this.tel1 = value;
    }

    /**
     * Gets the value of the tel2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTel2() {
        return tel2;
    }

    /**
     * Sets the value of the tel2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTel2(String value) {
        this.tel2 = value;
    }

    /**
     * Gets the value of the siret property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSIRET() {
        return siret;
    }

    /**
     * Sets the value of the siret property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSIRET(String value) {
        this.siret = value;
    }

    /**
     * Gets the value of the code property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the value of the code property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCode(String value) {
        this.code = value;
    }

    /**
     * Gets the value of the creditCategory property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreditCategory() {
        return creditCategory;
    }

    /**
     * Sets the value of the creditCategory property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreditCategory(String value) {
        this.creditCategory = value;
    }

}
