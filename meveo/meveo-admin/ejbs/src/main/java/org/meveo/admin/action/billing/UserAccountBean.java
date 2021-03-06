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
package org.meveo.admin.action.billing;

import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.StatusMessage.Severity;
import org.meveo.admin.action.BaseBean;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.DuplicateDefaultAccountException;
import org.meveo.admin.util.pagination.PaginationDataModel;
import org.meveo.model.admin.User;
import org.meveo.model.billing.BillingAccount;
import org.meveo.model.billing.RatedTransaction;
import org.meveo.model.billing.UserAccount;
import org.meveo.service.base.PersistenceService;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.billing.local.BillingAccountServiceLocal;
import org.meveo.service.billing.local.RatedTransactionServiceLocal;
import org.meveo.service.billing.local.UserAccountServiceLocal;

/**
 * Standard backing bean for {@link UserAccount} (extends {@link BaseBean} that
 * provides almost all common methods to handle entities filtering/sorting in
 * datatable, their create, edit, view, delete operations). It works with Manaty
 * custom JSF components.
 * 
 * @author Ignas Lelys
 * @created Dec 7, 2010
 */
@Name("userAccountBean")
@Scope(ScopeType.CONVERSATION)
public class UserAccountBean extends BaseBean<UserAccount> {

    private static final long serialVersionUID = 1L;

    /**
     * Injected
     * 
     * @{link UserAccount} service. Extends {@link PersistenceService} .
     */
    @In
    private UserAccountServiceLocal userAccountService;

    @In
    private RatedTransactionServiceLocal ratedTransactionService;

    @RequestParameter
    private Long billingAccountId;

    @In
    private BillingAccountServiceLocal billingAccountService;

    @In(required = false)
    private User currentUser;

    /**
     * Constructor. Invokes super constructor and provides class type of this
     * bean for {@link BaseBean}.
     */
    public UserAccountBean() {
        super(UserAccount.class);
    }

    /**
     * Factory method for entity to edit. If objectId param set load that entity
     * from database, otherwise create new.
     * 
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @Begin(nested = true)
    @Factory("userAccount")
    public UserAccount init() {
        initEntity();
        if (entity.getId() == null && billingAccountId != null) {
        	BillingAccount billingAccount=billingAccountService.findById(billingAccountId);
        	entity.setBillingAccount(billingAccount);
            populateAccounts(billingAccount);
        }
        return entity;
    }

    /**
     * Data model of entities for data table in GUI.
     * 
     * @return filtered entities.
     */
    @Out(value = "userAccounts", required = false)
    protected PaginationDataModel<UserAccount> getDataModel() {
        return entities;
    }

    /**
     * Factory method, that is invoked if data model is empty. Invokes
     * BaseBean.list() method that handles all data model loading. Overriding is
     * needed only to put factory name on it.
     * 
     * @see org.meveo.admin.action.BaseBean#list()
     */
    @Begin(join = true)
    @Factory("userAccounts")
    public void list() {
        super.list();
    }

    /**
     * Conversation is ended and user is redirected from edit to his previous
     * window.
     * 
     * @see org.meveo.admin.action.BaseBean#saveOrUpdate(org.meveo.model.IEntity)
     */
    @End(beforeRedirect = true, root=false)
  public String saveOrUpdate() {
		try{
			if (entity.getDefaultLevel()) {
				if (userAccountService.isDuplicationExist(entity)) {
				    entity.setDefaultLevel(false);
				       throw new DuplicateDefaultAccountException();
				}

			}
			saveOrUpdate(entity);
			Redirect.instance().setParameter("edit", "false");
			Redirect.instance().setParameter("objectId", entity.getId());
			Redirect.instance().setViewId(
					"/pages/billing/userAccounts/userAccountDetail.xhtml");
			Redirect.instance().execute();
		} catch (DuplicateDefaultAccountException e1) {
			 statusMessages.addFromResourceBundle(Severity.ERROR, "error.account.duplicateDefautlLevel");
		}catch (Exception e) {
			e.printStackTrace();
			statusMessages.addFromResourceBundle(Severity.ERROR, "javax.el.ELException");
				
		}
		
		return null;
	}

    /**
     * @see org.meveo.admin.action.BaseBean#getPersistenceService()
     */
    @Override
    protected IPersistenceService<UserAccount> getPersistenceService() {
        return userAccountService;
    }

    public String saveOrUpdate(UserAccount entity) {
        try {
            if (entity.isTransient()) {
                userAccountService.createUserAccount(entity.getBillingAccount().getCode(), entity,
                        currentUser);
                statusMessages.addFromResourceBundle("save.successful");
            } else {
                userAccountService.updateUserAccount(entity, currentUser);
                statusMessages.addFromResourceBundle("update.successful");
            }

        } catch (Exception e) {
            e.printStackTrace();
            statusMessages.add(e.getMessage());
        }

        return back();
    }

    public String terminateAccount() {
        log.info("resiliateAccount userAccountId:" + entity.getId());
        try {
            userAccountService.userAccountTermination(entity.getCode(), new Date(), currentUser);
            statusMessages.addFromResourceBundle("resiliation.resiliateSuccessful");
            return "/pages/billing/userAccounts/userAccountDetail.seam?objectId=" + entity.getId() + "&edit=false";
        } catch (BusinessException e) {
            e.printStackTrace();
            statusMessages.add(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            statusMessages.add(e.getMessage());
        }
        return null;
    }

    public String cancelAccount() {
        log.info("cancelAccount userAccountId:" + entity.getId());
        try {
            userAccountService.userAccountCancellation(entity.getCode(), new Date(), currentUser);
            statusMessages.addFromResourceBundle("cancellation.cancelSuccessful");
            return "/pages/billing/userAccounts/userAccountDetail.seam?objectId=" + entity.getId() + "&edit=false";
        } catch (BusinessException e) {
            e.printStackTrace();
            statusMessages.add(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            statusMessages.add(e.getMessage());
        }
        return null;
    }

    public String reactivateAccount() {
        log.info("reactivateAccount userAccountId:" + entity.getId());
        try {
            userAccountService.userAccountReactivation(entity.getCode(), new Date(), currentUser);
            statusMessages.addFromResourceBundle("reactivation.reactivateSuccessful");
            return "/pages/billing/userAccounts/userAccountDetail.seam?objectId=" + entity.getId() + "&edit=false";
        } catch (BusinessException e) {
            e.printStackTrace(); // TODO WTF printStackTrace??
            statusMessages.add(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            statusMessages.add(e.getMessage());
        }
        return null;
    }

    @Factory("getRatedTransactionsNoInvoiced")
    public List<RatedTransaction> getRatedTransactionsNoInvoiced() {
        return ratedTransactionService.getRatedTransactionsNoInvoiced(entity);
    }

    @Factory("getRatedTransactionsInvoiced")
    public List<RatedTransaction> getRatedTransactionsInvoiced() {
        return ratedTransactionService.getRatedTransactionsInvoiced(entity);
    }
	public void populateAccounts(BillingAccount billingAccount){
		
	    entity.setBillingAccount(billingAccount);
		if(userAccountService.isDuplicationExist(entity)){
		    entity.setDefaultLevel(false);
		}else{
		    entity.setDefaultLevel(true);
		}
        if(billingAccount.getProvider()!=null && billingAccount.getProvider().isLevelDuplication()){
            entity.setCode(billingAccount.getCode());
            entity.setDescription(billingAccount.getDescription());
            entity.setAddress(billingAccount.getAddress());
            entity.setExternalRef1(billingAccount.getExternalRef1());
            entity.setExternalRef2(billingAccount.getExternalRef2());
            entity.setProviderContact(billingAccount.getProviderContact());
            entity.setName(billingAccount.getName());
            entity.setProvider(billingAccount.getProvider());
            entity.setSubscriptionDate(billingAccount.getSubscriptionDate());
            entity.setPrimaryContact(billingAccount.getPrimaryContact());
        }
	}

}
