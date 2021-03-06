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
package org.meveo.oudaya.billing.impl;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.meveo.admin.exception.BusinessException;
import org.meveo.commons.utils.DateUtils;
import org.meveo.commons.utils.EjbUtils;
import org.meveo.commons.utils.NumberUtils;
import org.meveo.model.AccountEntity;
import org.meveo.model.billing.BillingAccount;
import org.meveo.model.billing.BillingCycle;
import org.meveo.model.billing.CategoryInvoiceAgregate;
import org.meveo.model.billing.InstanceStatusEnum;
import org.meveo.model.billing.Invoice;
import org.meveo.model.billing.InvoiceAgregate;
import org.meveo.model.billing.InvoiceCategory;
import org.meveo.model.billing.InvoiceSubCategory;
import org.meveo.model.billing.RatedTransaction;
import org.meveo.model.billing.ServiceInstance;
import org.meveo.model.billing.SubCategoryInvoiceAgregate;
import org.meveo.model.billing.TaxInvoiceAgregate;
import org.meveo.model.billing.UserAccount;
import org.meveo.model.billing.XMLInvoiceHeaderCategoryDTO;
import org.meveo.model.crm.Customer;
import org.meveo.model.payments.CustomerAccount;
import org.meveo.oudaya.OudayaConfig;
import org.meveo.persistence.MeveoPersistence;
import org.meveo.service.payments.remote.CustomerAccountServiceRemote;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * @author R.AITYAAZZA
 */
public class XMLInvoiceCreator {

    private static final String dueDateFormat = "dd/MM/yyyy";

    protected final Logger logger = Logger.getLogger(this.getClass());

    public void createXMLInvoice(Invoice invoice, File billingRundir) throws BusinessException {
        try {
            boolean entreprise = invoice.getProvider().isEntreprise();

            DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            Element invoiceTag = doc.createElement("invoice");
            Element header = doc.createElement("header");
            invoiceTag.setAttribute("number", "XXXXXXX");
            invoiceTag.setAttribute("id", invoice.getId().toString());
            invoiceTag.setAttribute("customerId", invoice.getBillingAccount().getCustomerAccount().getCustomer()
                    .getCode()+ "");
            invoiceTag.setAttribute("customerAccountCode",
                    invoice.getBillingAccount().getCustomerAccount().getCode() != null ? invoice.getBillingAccount()
                            .getCustomerAccount().getCode() : "");
            BillingCycle billingCycle = invoice.getBillingRun().getBillingCycle();
            invoiceTag.setAttribute("templateName",billingCycle!=null && billingCycle.getBillingTemplateName()!=null?
            		billingCycle.getBillingTemplateName():"default");
            doc.appendChild(invoiceTag);
            invoiceTag.appendChild(header);
            
            
            
            Customer customer = invoice.getBillingAccount().getCustomerAccount().getCustomer();
            Element customerTag = doc.createElement("customer");
            customerTag.setAttribute("id", customer.getId() + "");
            customerTag.setAttribute("code", customer.getCode() + "");
            customerTag.setAttribute("externalRef1",
            		customer.getExternalRef1() != null ? customer
                            .getExternalRef1() : "");
            customerTag.setAttribute("externalRef2",
            		customer.getExternalRef2() != null ? customer
                            .getExternalRef2() : "");
            header.appendChild(customerTag);
            addNameAndAdress(customer, doc, customerTag);
            
            
            CustomerAccount customerAccount = invoice.getBillingAccount().getCustomerAccount();
            Element customerAccountTag = doc.createElement("customerAccount");
            customerAccountTag.setAttribute("id", customerAccount.getId() + "");
            customerAccountTag.setAttribute("code", customerAccount.getCode() + "");
            customerAccountTag.setAttribute("description", customerAccount.getDescription() + "");
            customerAccountTag.setAttribute("externalRef1",
            		customerAccount.getExternalRef1() != null ? customerAccount
                            .getExternalRef1() : "");
            customerAccountTag.setAttribute("externalRef2",
            		customerAccount.getExternalRef2() != null ? customerAccount
                            .getExternalRef2() : "");
            
		   EntityManager em = MeveoPersistence.getEntityManager();
		   Query billingQuery = em
		                .createQuery("select si from ServiceInstance si join si.subscription s join s.userAccount ua join ua.billingAccount ba join ba.customerAccount ca where ca.id = :customerAccountId");
		   billingQuery.setParameter("customerAccountId", customerAccount.getId());
		   List<ServiceInstance> services = (List<ServiceInstance>) billingQuery.getResultList();
		   boolean terminated = isAllServiceInstancesTerminated(services);
		    
		    customerAccountTag.setAttribute("accountTerminated",terminated+"");
            
            header.appendChild(customerAccountTag);
            addNameAndAdress(customerAccount, doc, customerAccountTag);
            addproviderContact(customerAccount, doc, customerAccountTag);
            

            BillingAccount billingAccount = invoice.getBillingAccount();
            Element billingAccountTag = doc.createElement("billingAccount");
            if (billingCycle == null) {
                billingCycle = billingAccount.getBillingCycle();
            }
            String billingCycleCode = billingCycle != null ? billingCycle.getCode() + "" : "";
            billingAccountTag.setAttribute("billingCycleCode", billingCycleCode);
            String billingAccountId=billingAccount.getId()+ "";
            String billingAccountCode=billingAccount.getCode()+ "";
            billingAccountTag.setAttribute("id", billingAccountId);
            billingAccountTag.setAttribute("code",billingAccountCode);
            billingAccountTag.setAttribute("description", billingAccount.getDescription() + "");
            billingAccountTag.setAttribute("externalRef1",
            		billingAccount.getExternalRef1() != null ? billingAccount
                            .getExternalRef1() : "");
            billingAccountTag.setAttribute("externalRef2",
            		billingAccount.getExternalRef2() != null ? billingAccount
                            .getExternalRef2() : "");
            header.appendChild(billingAccountTag);

            if(billingAccount.getName()!=null && billingAccount.getName().getTitle()!=null){
           	 Element company = doc.createElement("company");
           	 Text companyTxt = doc.createTextNode(billingAccount.getName().getTitle().getIsCompany()+"");
                billingAccountTag.appendChild(companyTxt);
           }
            
            Element email = doc.createElement("email");
			Text emailTxt = doc.createTextNode(billingAccount.getEmail()!=null?billingAccount.getEmail():"");
			email.appendChild(emailTxt);
			billingAccountTag.appendChild(email);
			
            addNameAndAdress(billingAccount, doc, billingAccountTag);

            addPaymentInfo(billingAccount, doc, billingAccountTag);

            Element invoiceDate = doc.createElement("invoiceDate");
            Text invoiceDateTxt = doc.createTextNode(DateUtils.formatDateWithPattern(invoice.getInvoiceDate(),
                    "dd/MM/yyyy"));
            invoiceDate.appendChild(invoiceDateTxt);
            header.appendChild(invoiceDate);

            Element dueDate = doc.createElement("dueDate");
            Text dueDateTxt = doc.createTextNode(DateUtils.formatDateWithPattern(invoice.getDueDate(), dueDateFormat));
            dueDate.appendChild(dueDateTxt);
            header.appendChild(dueDate);
            addHeaderCategories(invoice, doc, header);

            Element amount = doc.createElement("amount");
            invoiceTag.appendChild(amount);

            Element currency = doc.createElement("currency");
            Text currencyTxt = doc.createTextNode("EUR");
            currency.appendChild(currencyTxt);
            amount.appendChild(currency);

            Element amountWithoutTax = doc.createElement("amountWithoutTax");
            Text amountWithoutTaxTxt = doc.createTextNode(round(invoice.getAmountWithoutTax()));
            amountWithoutTax.appendChild(amountWithoutTaxTxt);
            amount.appendChild(amountWithoutTax);

            Element amountWithTax = doc.createElement("amountWithTax");
            Text amountWithTaxTxt = doc.createTextNode(round(invoice.getAmountWithTax()));
            amountWithTax.appendChild(amountWithTaxTxt);
            amount.appendChild(amountWithTax);

            BigDecimal balance =computeBalance(invoice.getBillingAccount().getCustomerAccount().getCode(), invoice.getDueDate());

            if (balance == null) {
                throw new BusinessException("account balance calculation failed");
            }
            BigDecimal netToPay=BigDecimal.ZERO;
            if (entreprise) {
                netToPay = invoice.getAmountWithTax();
            } else {
                netToPay = invoice.getAmountWithTax().add(balance);
            }

            Element balanceElement = doc.createElement("balance");
            Text balanceTxt = doc.createTextNode(round(balance));
            balanceElement.appendChild(balanceTxt);
            amount.appendChild(balanceElement);

            Element netToPayElement = doc.createElement("netToPay");
            Text netToPayTxt = doc.createTextNode(round(netToPay));
            netToPayElement.appendChild(netToPayTxt);
            amount.appendChild(netToPayElement);

            addTaxes(invoice, doc, amount);

            Element detail = doc.createElement("detail");
            invoiceTag.appendChild(detail);
            addUserAccounts(invoice, doc, detail);

            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans = transfac.newTransformer();
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.setOutputProperty(OutputKeys.INDENT, "yes");

            // create string from xml tree
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(billingRundir + File.separator + invoice.getTemporaryInvoiceNumber()
                    + ".xml");
            logger.info("source=" + source.toString());
            trans.transform(source, result);

        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(e.getMessage());
        }
    }

    public static void addUserAccounts(Invoice invoice, Document doc, Element parent) {

        Element userAccounts = doc.createElement("userAccounts");
        parent.appendChild(userAccounts);
        BillingAccount billingAccount = invoice.getBillingAccount();

        for (UserAccount userAccount : billingAccount.getUsersAccounts()) {
            Element userAccountTag = doc.createElement("userAccount");
            userAccountTag.setAttribute("id", userAccount.getId() + "");
            userAccountTag.setAttribute("code", userAccount.getCode() != null ? userAccount.getCode()
                    : "");
            userAccountTag.setAttribute("description", userAccount.getDescription() != null ? userAccount.getDescription()
                    : "");
            userAccounts.appendChild(userAccountTag);
            addNameAndAdress(userAccount, doc, userAccountTag);
            addCategories(userAccount, invoice, doc, userAccountTag, true);
        }

    }

    public static void addNameAndAdress(AccountEntity account, Document doc, Element parent) {
    	if(!(account instanceof Customer)){
    		 Element nameTag = doc.createElement("name");
    	        parent.appendChild(nameTag);

    	        Element quality = doc.createElement("quality");
    	        if (account.getName().getTitle() != null) {
    	            Text qualityTxt = doc.createTextNode(account.getName().getTitle().getCode());
    	            quality.appendChild(qualityTxt);
    	        }
    	        nameTag.appendChild(quality);
    	        if (account.getName().getFirstName() != null) {
    	            Element firstName = doc.createElement("firstName");
    	            Text firstNameTxt = doc.createTextNode(account.getName().getFirstName());
    	            firstName.appendChild(firstNameTxt);
    	            nameTag.appendChild(firstName);
    	        }

    	        Element name = doc.createElement("name");
    	        if (account.getName().getLastName() != null) {
    	            Text nameTxt = doc.createTextNode(account.getName().getLastName());
    	            name.appendChild(nameTxt);
    	        }
    	        nameTag.appendChild(name);
    	}
        Element addressTag = doc.createElement("address");
        Element address1 = doc.createElement("address1");
        if (account.getAddress().getAddress1() != null) {
            Text adress1Txt = doc.createTextNode(account.getAddress().getAddress1());
            address1.appendChild(adress1Txt);
        }
        addressTag.appendChild(address1);

        Element address2 = doc.createElement("address2");
        if (account.getAddress().getAddress2() != null) {
            Text adress2Txt = doc.createTextNode(account.getAddress().getAddress2());
            address2.appendChild(adress2Txt);
        }
        addressTag.appendChild(address2);

        Element address3 = doc.createElement("address3");
        if (account.getAddress().getAddress3() != null) {
            Text adress3Txt = doc.createTextNode(account.getAddress().getAddress3() != null ? account.getAddress()
                    .getAddress3() : "");
            address3.appendChild(adress3Txt);
        }
        addressTag.appendChild(address3);

        Element city = doc.createElement("city");
        Text cityTxt = doc.createTextNode(account.getAddress().getCity() != null ? account.getAddress().getCity() : "");
        city.appendChild(cityTxt);
        addressTag.appendChild(city);

        Element postalCode = doc.createElement("postalCode");
        Text postalCodeTxt = doc.createTextNode(account.getAddress().getZipCode() != null ? account.getAddress()
                .getZipCode() : "");
        postalCode.appendChild(postalCodeTxt);
        addressTag.appendChild(postalCode);

        Element state = doc.createElement("state");
        addressTag.appendChild(state);

        Element country = doc.createElement("country");
        Text countryTxt = doc.createTextNode(account.getAddress().getCountry() != null ? account.getAddress()
                .getCountry() : "");
        country.appendChild(countryTxt);
        addressTag.appendChild(country);

        parent.appendChild(addressTag);
    }

	public static void addproviderContact(AccountEntity account, Document doc,
			Element parent) {
		if(account.getPrimaryContact()!=null){
			Element providerContactTag = doc.createElement("providerContact");
			parent.appendChild(providerContactTag);
			if (account.getPrimaryContact().getFirstName() != null) {
				Element firstName = doc.createElement("firstName");
				Text firstNameTxt = doc.createTextNode(account.getPrimaryContact()
						.getFirstName());
				firstName.appendChild(firstNameTxt);
				providerContactTag.appendChild(firstName);
			}

			
			if (account.getPrimaryContact().getLastName() != null) {
				Element name = doc.createElement("lastname");
				Text nameTxt = doc.createTextNode(account.getPrimaryContact()
						.getLastName());
				name.appendChild(nameTxt);
				providerContactTag.appendChild(name);
			}
			
			if (account.getPrimaryContact().getEmail() != null) {
				Element email = doc.createElement("email");
				Text emailTxt = doc.createTextNode(account.getPrimaryContact().getEmail());
				email.appendChild(emailTxt);
				providerContactTag.appendChild(email);
			}
			if (account.getPrimaryContact().getFax() != null) {
				Element fax = doc.createElement("fax");
				Text faxTxt = doc.createTextNode(account.getPrimaryContact().getFax());
				fax.appendChild(faxTxt);
				providerContactTag.appendChild(fax);
				
			}
			if (account.getPrimaryContact().getMobile() != null) {
				
				Element mobile = doc.createElement("mobile");
				Text mobileTxt = doc.createTextNode(account.getPrimaryContact().getMobile());
				mobile.appendChild(mobileTxt);
				providerContactTag.appendChild(mobile);
			}
			if (account.getPrimaryContact().getPhone() != null) {
				Element phone = doc.createElement("phone");
				Text phoneTxt = doc.createTextNode(account.getPrimaryContact().getPhone());
				phone.appendChild(phoneTxt);
				providerContactTag.appendChild(phone);
			}
		}
		

	}


    public static void addPaymentInfo(BillingAccount billingAccount, Document doc, Element parent) {

        Element paymentMethod = doc.createElement("paymentMethod");
        parent.appendChild(paymentMethod);
        paymentMethod.setAttribute("type", billingAccount.getPaymentMethod().toString());
        Element bankCoordinates = doc.createElement("bankCoordinates");
        Element bankCode = doc.createElement("bankCode");
        Element branchCode = doc.createElement("branchCode");
        Element accountNumber = doc.createElement("accountNumber");
        Element accountOwner = doc.createElement("accountOwner");
        Element key = doc.createElement("key");
        Element iban = doc.createElement("IBAN");
        bankCoordinates.appendChild(bankCode);
        bankCoordinates.appendChild(branchCode);
        bankCoordinates.appendChild(accountNumber);
        bankCoordinates.appendChild(accountOwner);
        bankCoordinates.appendChild(key);
        bankCoordinates.appendChild(iban);
        paymentMethod.appendChild(bankCoordinates);

        if (billingAccount.getBankCoordinates() != null && billingAccount.getBankCoordinates().getBankCode() != null) {
            Text bankCodeTxt = doc
                    .createTextNode(billingAccount.getBankCoordinates().getBankCode() != null ? billingAccount
                            .getBankCoordinates().getBankCode() : "");
            bankCode.appendChild(bankCodeTxt);

            Text branchCodeTxt = doc
                    .createTextNode(billingAccount.getBankCoordinates().getBranchCode() != null ? billingAccount
                            .getBankCoordinates().getBranchCode() : "");
            branchCode.appendChild(branchCodeTxt);

            Text accountNumberTxt = doc
                    .createTextNode(billingAccount.getBankCoordinates().getAccountNumber() != null ? billingAccount
                            .getBankCoordinates().getAccountNumber() : "");
            accountNumber.appendChild(accountNumberTxt);

            Text accountOwnerTxt = doc
                    .createTextNode(billingAccount.getBankCoordinates().getAccountOwner() != null ? billingAccount
                            .getBankCoordinates().getAccountOwner() : "");
            accountOwner.appendChild(accountOwnerTxt);

            Text keyTxt = doc.createTextNode(billingAccount.getBankCoordinates().getKey() != null ? billingAccount
                    .getBankCoordinates().getKey() : "");
            key.appendChild(keyTxt);
            if (billingAccount.getBankCoordinates().getIban() != null) {
                Text ibanTxt = doc
                        .createTextNode(billingAccount.getBankCoordinates().getIban() != null ? billingAccount
                                .getBankCoordinates().getIban() : "");
                iban.appendChild(ibanTxt);
            }

        }
    }

    public static void addCategories(UserAccount userAccount, Invoice invoice, Document doc, Element parent,
            boolean generateSubCat) {

        Element categories = doc.createElement("categories");
        parent.appendChild(categories);
        boolean entreprise = invoice.getProvider().isEntreprise();
        List<CategoryInvoiceAgregate> categoryInvoiceAgregates=new ArrayList<CategoryInvoiceAgregate>();
        for (InvoiceAgregate invoiceAgregate : invoice.getInvoiceAgregates()) {
            if (invoiceAgregate.getUserAccount().getId() == userAccount.getId()) {
                if (invoiceAgregate instanceof CategoryInvoiceAgregate) {
                	CategoryInvoiceAgregate categoryInvoiceAgregate = (CategoryInvoiceAgregate) invoiceAgregate;
                    
                	categoryInvoiceAgregates.add(categoryInvoiceAgregate);
                }
            }
        }
        Collections.sort(categoryInvoiceAgregates, new Comparator<CategoryInvoiceAgregate>() {
                                public int compare(CategoryInvoiceAgregate c0, CategoryInvoiceAgregate c1) {
                                    if (c0.getInvoiceCategory() != null && c1.getInvoiceCategory() != null
                                    		&& c0.getInvoiceCategory().getSortIndex()!=null 
                                    		&& c1.getInvoiceCategory().getSortIndex()!=null) {
                                        return c0.getInvoiceCategory().getSortIndex().compareTo(
                                                c1.getInvoiceCategory().getSortIndex());
                                    }
                                    return 0;
                                }
                            });
        for (CategoryInvoiceAgregate categoryInvoiceAgregate : categoryInvoiceAgregates) {
           InvoiceCategory invoiceCategory = categoryInvoiceAgregate.getInvoiceCategory();
                    Element category = doc.createElement("category");
                    category.setAttribute("label",
                            invoiceCategory != null && invoiceCategory.getDescription() != null ? invoiceCategory
                                    .getDescription() : "");
                    category.setAttribute("code",
                            invoiceCategory != null && invoiceCategory.getCode() != null ? invoiceCategory
                                    .getCode() : "");
                    categories.appendChild(category);

                    Element amountWithoutTax = doc.createElement("amountWithoutTax");
                    Text amountWithoutTaxTxt = doc.createTextNode(round(categoryInvoiceAgregate.getAmountWithoutTax()));
                    amountWithoutTax.appendChild(amountWithoutTaxTxt);
                    category.appendChild(amountWithoutTax);

                    Element amountWithTax = doc.createElement("amountWithTax");
                    Text amountWithTaxTxt = doc.createTextNode(round(categoryInvoiceAgregate.getAmountWithTax()));
                    amountWithTax.appendChild(amountWithTaxTxt);
                    category.appendChild(amountWithTax);

                    if (generateSubCat) {
                        Element subCategories = doc.createElement("subCategories");
                        category.appendChild(subCategories);
                        Set<SubCategoryInvoiceAgregate> subCategoryInvoiceAgregates = categoryInvoiceAgregate
                                .getSubCategoryInvoiceAgregates();

                        for (SubCategoryInvoiceAgregate subCatInvoiceAgregate : subCategoryInvoiceAgregates) {
                            InvoiceSubCategory invoiceSubCat = subCatInvoiceAgregate.getInvoiceSubCategory();
                            List<RatedTransaction> transactions = subCatInvoiceAgregate.getRatedtransactions();

                            boolean createSubCatElement = false;
                            for (RatedTransaction ratedTrnsaction : transactions) {
                                BigDecimal transactionAmount = entreprise ? ratedTrnsaction.getAmount1WithTax()
                                        : ratedTrnsaction.getAmount2WithoutTax();
                                if (transactionAmount != null && !transactionAmount.equals(BigDecimal.ZERO)) {
                                    createSubCatElement = true;
                                    break;
                                }
                            }
                            if (!createSubCatElement) {
                                continue;
                            }

                            Element subCategory = doc.createElement("subCategory");
                            subCategories.appendChild(subCategory);
                            subCategory.setAttribute("label", invoiceSubCat != null ? invoiceSubCat.getDescription()
                                    + "" : "");

                            Collections.sort(transactions, new Comparator<RatedTransaction>() {
                                public int compare(RatedTransaction c0, RatedTransaction c1) {
                                    if (c0.getChargeApplication() != null && c1.getChargeApplication() != null) {
                                        return c0.getChargeApplication().getId().compareTo(
                                                c1.getChargeApplication().getId());
                                    }
                                    return 0;
                                }
                            });

                            for (RatedTransaction ratedTrnsaction : transactions) {
                                BigDecimal transactionAmount = entreprise ? ratedTrnsaction.getAmount1WithTax()
                                        : ratedTrnsaction.getAmount2WithoutTax();
                                if (transactionAmount != null && !transactionAmount.equals(BigDecimal.ZERO)) {

                                    Element line = doc.createElement("line");
                                    line.setAttribute("code", ratedTrnsaction.getUsageCode() != null ? ratedTrnsaction
                                            .getUsageCode() : "");
                                    line.setAttribute("taxPercent", round(ratedTrnsaction.getTaxPercent()));

                                    Element lebel = doc.createElement("label");
                                    Text lebelTxt = doc
                                            .createTextNode(ratedTrnsaction.getDescription() != null ? ratedTrnsaction
                                                    .getDescription() : "");
                                    lebel.appendChild(lebelTxt);
                                    line.appendChild(lebel);

                                    Element lineAmountWithoutTax = doc.createElement("amountWithoutTax");
                                    Text lineAmountWithoutTaxTxt = doc.createTextNode(round(ratedTrnsaction
                                            .getAmount1WithoutTax()));
                                    lineAmountWithoutTax.appendChild(lineAmountWithoutTaxTxt);
                                    line.appendChild(lineAmountWithoutTax);

                                    Element lineAmountWithTax = doc.createElement("amountWithTax");
                                    Text lineAmountWithTaxTxt = doc.createTextNode(round(entreprise ? ratedTrnsaction
                                            .getAmount1WithTax() : ratedTrnsaction.getAmount2WithoutTax()));
                                    lineAmountWithTax.appendChild(lineAmountWithTaxTxt);
                                    line.appendChild(lineAmountWithTax);

                                    Element unitPrice1 = doc.createElement("unitPrice1");
                                    Text unitPrice1Txt = doc.createTextNode(round(ratedTrnsaction.getUnitPrice1()));
                                    unitPrice1.appendChild(unitPrice1Txt);
                                    line.appendChild(unitPrice1);

                                    Element unitPrice2 = doc.createElement("unitPrice2");
                                    Text unitPrice2Txt = doc.createTextNode(round(ratedTrnsaction.getUnitPrice2()));
                                    unitPrice2.appendChild(unitPrice2Txt);
                                    line.appendChild(unitPrice2);

                                    Element quantity = doc.createElement("quantity");
                                    Text quantityTxt = doc
                                            .createTextNode(ratedTrnsaction.getUsageQuantity() != null ? ratedTrnsaction
                                                    .getUsageQuantity()
                                                    + ""
                                                    : "");
                                    quantity.appendChild(quantityTxt);
                                    line.appendChild(quantity);

                                    subCategory.appendChild(line);
                                }
                            }
                        }
                    }


        }

    }

    private void addTaxes(Invoice invoice, Document doc, Element parent) {

        Element taxes = doc.createElement("taxes");
        taxes.setAttribute("total", round(invoice.getAmountTax()));
        parent.appendChild(taxes);
        Map<Long, TaxInvoiceAgregate> taxInvoiceAgregateMap = new HashMap<Long, TaxInvoiceAgregate>();
        for (InvoiceAgregate invoiceAgregate : invoice.getInvoiceAgregates()) {
            if (invoiceAgregate instanceof TaxInvoiceAgregate) {
                TaxInvoiceAgregate taxInvoiceAgregate = (TaxInvoiceAgregate) invoiceAgregate;
                TaxInvoiceAgregate taxAgregate = null;
                if (taxInvoiceAgregateMap.containsKey(taxInvoiceAgregate.getTax().getId())) {
                    taxAgregate = taxInvoiceAgregateMap.get(taxInvoiceAgregate.getTax().getId());
                    taxAgregate.setAmountTax(taxAgregate.getAmountTax().add(taxInvoiceAgregate.getAmountTax()));
                    taxAgregate.setAmountWithoutTax(taxAgregate.getAmountWithoutTax().add(
                            taxInvoiceAgregate.getAmountWithoutTax()));
                } else {
                    taxAgregate = new TaxInvoiceAgregate();
                    taxAgregate.setTaxPercent(taxInvoiceAgregate.getTaxPercent());
                    taxAgregate.setTax(taxInvoiceAgregate.getTax());
                    taxAgregate.setAmountTax(taxInvoiceAgregate.getAmountTax());
                    taxAgregate.setAmountWithoutTax(taxInvoiceAgregate.getAmountWithoutTax());
                    taxInvoiceAgregateMap.put(taxInvoiceAgregate.getTax().getId(), taxAgregate);
                }
            }

        }

        int taxId = 0;
        for (TaxInvoiceAgregate taxInvoiceAgregate : taxInvoiceAgregateMap.values()) {

            Element tax = doc.createElement("tax");

            tax.setAttribute("id", ++taxId + "");
            tax.setAttribute("code", taxInvoiceAgregate.getTax().getCode() + "");

            Element taxName = doc.createElement("name");
            Text taxNameTxt = doc
                    .createTextNode(taxInvoiceAgregate.getTax()!=null?(taxInvoiceAgregate.getTax().getDescription() != null ? taxInvoiceAgregate.getTax()
                            .getDescription() : ""):"");
            taxName.appendChild(taxNameTxt);
            tax.appendChild(taxName);

            Element percent = doc.createElement("percent");
            Text percentTxt = doc.createTextNode(round(taxInvoiceAgregate.getTaxPercent()));
            percent.appendChild(percentTxt);
            tax.appendChild(percent);

            Element taxAmount = doc.createElement("amount");
            Text amountTxt = doc.createTextNode(round(taxInvoiceAgregate.getAmountTax()));
            taxAmount.appendChild(amountTxt);
            tax.appendChild(taxAmount);

            Element amountHT = doc.createElement("amountHT");
            Text amountHTTxt = doc.createTextNode(round(taxInvoiceAgregate.getAmountWithoutTax()));
            amountHT.appendChild(amountHTTxt);
            tax.appendChild(amountHT);

            taxes.appendChild(tax);
        }

    }

    private void addHeaderCategories(Invoice invoice, Document doc, Element parent) {
        boolean entreprise = invoice.getProvider().isEntreprise();
        LinkedHashMap<String, XMLInvoiceHeaderCategoryDTO> headerCategories = new LinkedHashMap<String, XMLInvoiceHeaderCategoryDTO>();
        List<CategoryInvoiceAgregate> categoryInvoiceAgregates=new ArrayList<CategoryInvoiceAgregate>();
        for (InvoiceAgregate invoiceAgregate : invoice.getInvoiceAgregates()) {
                if (invoiceAgregate instanceof CategoryInvoiceAgregate) {
                	CategoryInvoiceAgregate categoryInvoiceAgregate = (CategoryInvoiceAgregate) invoiceAgregate;
                	categoryInvoiceAgregates.add(categoryInvoiceAgregate);
                }
        }
        Collections.sort(categoryInvoiceAgregates, new Comparator<CategoryInvoiceAgregate>() {
                                public int compare(CategoryInvoiceAgregate c0, CategoryInvoiceAgregate c1) {
                                    if (c0.getInvoiceCategory() != null && c1.getInvoiceCategory() != null
                                    		&& c0.getInvoiceCategory().getSortIndex()!=null 
                                    		&& c1.getInvoiceCategory().getSortIndex()!=null) {
                                        return c0.getInvoiceCategory().getSortIndex().compareTo(
                                                c1.getInvoiceCategory().getSortIndex());
                                    }
                                    return 0;
                                }
                            });
        
        
        for (CategoryInvoiceAgregate categoryInvoiceAgregate: categoryInvoiceAgregates) {
                InvoiceCategory invoiceCategory = categoryInvoiceAgregate.getInvoiceCategory();
                System.out.println("invoiceCategory:::"+invoiceCategory.getDescription());
                XMLInvoiceHeaderCategoryDTO headerCat = null;
                if (headerCategories.containsKey(invoiceCategory.getCode())) {
                    headerCat = headerCategories.get(invoiceCategory.getCode());
                    headerCat.addAmountWithoutTax(categoryInvoiceAgregate.getAmountWithoutTax());
                    headerCat.addAmountWithTax(categoryInvoiceAgregate.getAmountWithTax());
                } else {
                    headerCat = new XMLInvoiceHeaderCategoryDTO();
                    headerCat.setDescription(invoiceCategory.getDescription());
                    headerCat.setCode(invoiceCategory.getCode());
                    headerCat.setAmountWithoutTax(categoryInvoiceAgregate.getAmountWithoutTax());
                    headerCat.setAmountWithTax(categoryInvoiceAgregate.getAmountWithTax());
                    headerCategories.put(invoiceCategory.getCode(), headerCat);
                }
                if (entreprise) {
                    Set<SubCategoryInvoiceAgregate> subCategoryInvoiceAgregates = categoryInvoiceAgregate
                            .getSubCategoryInvoiceAgregates();

                    for (SubCategoryInvoiceAgregate subCatInvoiceAgregate : subCategoryInvoiceAgregates) {
                        List<RatedTransaction> transactions = subCatInvoiceAgregate.getRatedtransactions();

                        logger.info("subCatInvoiceAgregate code=" + subCatInvoiceAgregate.getId() + ",transactions="
                                + subCatInvoiceAgregate.getRatedtransactions().size());

                        Collections.sort(transactions, new Comparator<RatedTransaction>() {
                            public int compare(RatedTransaction c0, RatedTransaction c1) {
                                if (c0.getChargeApplication() != null && c1.getChargeApplication() != null) {
                                    return c0.getChargeApplication().getId().compareTo(
                                            c1.getChargeApplication().getId());
                                }
                                return 0;
                            }
                        });
                        Map<String, RatedTransaction> headerRatedTransactions = headerCat.getRatedtransactions();
                        for (RatedTransaction ratedTrnsaction : transactions) {
                            BigDecimal transactionAmountWithTax = ratedTrnsaction.getAmount1WithTax();
                            if (transactionAmountWithTax == null || transactionAmountWithTax.equals(BigDecimal.ZERO)) {
                                continue;
                            }
                            RatedTransaction headerRatedTransaction = null;
                            logger.info("headerRatedTransaction id=" + ratedTrnsaction.getId() + ",code="
                                    + ratedTrnsaction.getUsageCode() + ",Amount1WithoutTax="
                                    + ratedTrnsaction.getAmount1WithoutTax());

                            if (headerRatedTransactions.containsKey(ratedTrnsaction.getUsageCode())) {
                                headerRatedTransaction = headerRatedTransactions.get(ratedTrnsaction.getUsageCode());
                                headerRatedTransaction.setAmount1WithoutTax(headerRatedTransaction
                                        .getAmount1WithoutTax().add(ratedTrnsaction.getAmount1WithoutTax()));
                                headerRatedTransaction.setAmount1WithTax(headerRatedTransaction.getAmount1WithTax()
                                        .add(transactionAmountWithTax));

                            } else {
                                headerRatedTransaction = new RatedTransaction();
                                headerRatedTransaction.setUsageCode(ratedTrnsaction.getUsageCode());
                                headerRatedTransaction.setDescription(ratedTrnsaction.getChargeApplication()!=null?ratedTrnsaction.getChargeApplication()
                                        .getDescription():"");
                                headerRatedTransaction.setAmount1WithoutTax(ratedTrnsaction.getAmount1WithoutTax());
                                headerRatedTransaction.setAmount1WithTax(ratedTrnsaction.getAmount1WithTax());
                                headerRatedTransaction.setTaxPercent(ratedTrnsaction.getTaxPercent());
                                headerRatedTransactions.put(ratedTrnsaction.getUsageCode(), headerRatedTransaction);
                            }
                            logger.info("addHeaderCategories headerRatedTransaction amoutHT="
                                    + headerRatedTransaction.getAmount1WithoutTax());

                        }

                        logger.info("addHeaderCategories headerRatedTransactions.size="
                                + headerRatedTransactions.size());
                        logger.info("addHeaderCategories headerCat.getRatedtransactions().size="
                                + headerCat.getRatedtransactions().size());

                    }
                }

            

        }
        addHeaderCategories(headerCategories, doc, parent, entreprise);
    }

    private void addHeaderCategories(LinkedHashMap<String, XMLInvoiceHeaderCategoryDTO> headerCategories, Document doc,
            Element parent, boolean entreprise) {

        Element categories = doc.createElement("categories");
        parent.appendChild(categories);
        for (XMLInvoiceHeaderCategoryDTO xmlInvoiceHeaderCategoryDTO : headerCategories.values()) {

            Element category = doc.createElement("category");
            category.setAttribute("label", xmlInvoiceHeaderCategoryDTO.getDescription());
            category.setAttribute("code",
            		xmlInvoiceHeaderCategoryDTO != null && xmlInvoiceHeaderCategoryDTO.getCode() != null ? xmlInvoiceHeaderCategoryDTO
                            .getCode() : "");
            categories.appendChild(category);

            Element amountWithoutTax = doc.createElement("amountWithoutTax");
            Text amountWithoutTaxTxt = doc.createTextNode(round(xmlInvoiceHeaderCategoryDTO.getAmountWithoutTax()));
            amountWithoutTax.appendChild(amountWithoutTaxTxt);
            category.appendChild(amountWithoutTax);

            Element amountWithTax = doc.createElement("amountWithTax");
            Text amountWithTaxTxt = doc.createTextNode(round(xmlInvoiceHeaderCategoryDTO.getAmountWithTax()));
            amountWithTax.appendChild(amountWithTaxTxt);
            category.appendChild(amountWithTax);
            if (entreprise) {
                for (RatedTransaction headerTransaction : xmlInvoiceHeaderCategoryDTO.getRatedtransactions().values()) {

                    Element line = doc.createElement("line");
                    line.setAttribute("code", headerTransaction.getUsageCode());
                    line.setAttribute("taxPercent", round(headerTransaction.getTaxPercent()) + "");

                    Element lebel = doc.createElement("label");
                    Text lebelTxt = doc.createTextNode(headerTransaction.getDescription() != null ? headerTransaction
                            .getDescription() : "");
                    lebel.appendChild(lebelTxt);
                    line.appendChild(lebel);
                    logger.info("addHeaderCategories2 headerRatedTransaction amountHT="
                            + headerTransaction.getAmount1WithoutTax());
                    Element lineAmountWithoutTax = doc.createElement("amountWithoutTax");
                    Text lineAmountWithoutTaxTxt = doc.createTextNode(round(headerTransaction.getAmount1WithoutTax())
                            + "");
                    lineAmountWithoutTax.appendChild(lineAmountWithoutTaxTxt);
                    line.appendChild(lineAmountWithoutTax);

                    Element lineAmountWithTax = doc.createElement("amountWithTax");
                    Text lineAmountWithTaxTxt = doc.createTextNode(round(headerTransaction.getAmount1WithTax()) + "");
                    lineAmountWithTax.appendChild(lineAmountWithTaxTxt);
                    line.appendChild(lineAmountWithTax);

                    category.appendChild(line);
                }
            }

        }
    }

    public static String round(BigDecimal amount) {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        amount = amount.setScale(2, RoundingMode.HALF_UP);
        return NumberUtils.format(amount, OudayaConfig.getDecimalFormat());
    }

    public BigDecimal computeBalance(String customerAccountCode, Date dueDate) throws BusinessException {
        CustomerAccountServiceRemote customerAccountService;
        BigDecimal balance = BigDecimal.ZERO;
        try {
            String serviceName = OudayaConfig.getCustomerAccountServiceName();
            String providerURL = OudayaConfig.getServiceProviderUrl();
            logger.info("computeBalance serviceName=" + serviceName + ",serviceName=" + serviceName);
            customerAccountService = (CustomerAccountServiceRemote) EjbUtils.getRemoteInterface(serviceName,
                    providerURL);
            balance = customerAccountService.customerAccountBalanceDue(null, customerAccountCode, dueDate);
            logger.info("computeBalance customerAccountCode=" + customerAccountCode + ",dureDAte=" + dueDate
                    + ",balance=" + balance);
            return balance;
        } catch (Exception e) {
            e.printStackTrace();
            balance = null;
        }
        return balance;
    }
    private boolean isAllServiceInstancesTerminated(List<ServiceInstance> serviceInstances) {
        for (ServiceInstance service : serviceInstances) {
            boolean serviceActive = service.getStatus() == InstanceStatusEnum.ACTIVE;
            if (serviceActive) {
                return false;
            }
        }
        return true;
    }


}
