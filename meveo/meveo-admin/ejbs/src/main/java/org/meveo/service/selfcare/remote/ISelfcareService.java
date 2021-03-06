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
package org.meveo.service.selfcare.remote;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import javax.ejb.Remote;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.EmailNotFoundException;
import org.meveo.model.billing.BillingAccount;
import org.meveo.model.billing.Invoice;
import org.meveo.model.payments.CustomerAccount;
import org.meveo.service.base.local.IPersistenceService;

@Remote
public interface ISelfcareService extends IPersistenceService<CustomerAccount> {

    public Boolean authenticate(String username, String password) throws BusinessException,EmailNotFoundException;

    public void sendPassword(String email) throws BusinessException,EmailNotFoundException;

    public Boolean updatePassword(String username, String oldpassword, String password) throws BusinessException;

    public CustomerAccount getCustomerAccount(String username) throws BusinessException;

    public List<BillingAccount> getBillingAccounts(String username) throws BusinessException;

    public List<Invoice> getBillingAccountInvoices(String code) throws BusinessException;

    public List<Invoice> getBillingAccountValidatedInvoices(String code) throws BusinessException;

    public BigDecimal getAccountBalance(String code) throws BusinessException;

    public byte[] getPDFInvoice(String invoiceNumber, String providerCode) throws BusinessException;
    public byte[] getPDFInvoice(String invoiceNumber) throws BusinessException;

    public void sendMail(String from, List<String> to, List<String> cc, String subject, String body, List<File> files)
            throws BusinessException;
    
    public void sendEmailCreationSpace(String email) throws BusinessException,EmailNotFoundException ;
    
    public String getBillingAccountProviderCode(String code) throws BusinessException;
}
