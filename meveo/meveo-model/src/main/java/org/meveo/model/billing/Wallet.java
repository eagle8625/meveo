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
package org.meveo.model.billing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.meveo.model.AuditableEntity;

/**
 * @author Ignas Lelys
 * @created Dec 3, 2010
 * 
 */
@Entity
@Table(name = "BILLING_WALLET")
@SequenceGenerator(name = "ID_GENERATOR", sequenceName = "BILLING_WALLET_SEQ")
public class Wallet extends AuditableEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "NAME")
    private String name = "PRINCIPAL";

    @Enumerated(value = EnumType.STRING)
    @Column(name = "WALLET_TYPE")
    private WalletTypeEnum walletType = WalletTypeEnum.BILLABLE;

    @OneToOne
    @JoinColumn(name = "USER_ACCOUNT_ID")
    private UserAccount userAccount;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RatedTransaction> ratedTransactions;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public WalletTypeEnum getWalletType() {
        return walletType;
    }

    public void setWalletType(WalletTypeEnum walletType) {
        this.walletType = walletType;
    }

    public String toString() {
        return name;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    public List<RatedTransaction> getRatedTransactions() {
        return ratedTransactions;
    }

    public void setRatedTransactions(List<RatedTransaction> ratedTransactions) {
        this.ratedTransactions = ratedTransactions;
    }

    public Set<InvoiceSubCategory> getInvoiceSubCategories() {
        Set<InvoiceSubCategory> invoiceSubCategories = new HashSet<InvoiceSubCategory>();
        for (RatedTransaction ratedTransaction : ratedTransactions) {
            invoiceSubCategories.add(ratedTransaction.getInvoiceSubCategory());
        }
        return invoiceSubCategories;
    }

}
