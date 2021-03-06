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
package org.meveo.service.payments.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.log.Log;
import org.meveo.admin.exception.BusinessException;
import org.meveo.commons.utils.ParamBean;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.model.admin.User;
import org.meveo.model.billing.InstanceStatusEnum;
import org.meveo.model.billing.ServiceInstance;
import org.meveo.model.crm.Customer;
import org.meveo.model.payments.AccountOperation;
import org.meveo.model.payments.CreditCategoryEnum;
import org.meveo.model.payments.CustomerAccount;
import org.meveo.model.payments.CustomerAccountStatusEnum;
import org.meveo.model.payments.DunningLevelEnum;
import org.meveo.model.payments.MatchingStatusEnum;
import org.meveo.model.payments.OperationCategoryEnum;
import org.meveo.model.payments.PaymentMethodEnum;
import org.meveo.model.shared.Address;
import org.meveo.model.shared.ContactInformation;
import org.meveo.service.base.AccountService;
import org.meveo.service.catalog.local.TitleServiceLocal;
import org.meveo.service.crm.local.CustomerServiceLocal;
import org.meveo.service.payments.local.CustomerAccountServiceLocal;
import org.meveo.service.payments.local.OtherCreditAndChargeServiceLocal;
import org.meveo.service.payments.remote.CustomerAccountServiceRemote;

/**
 * Customer Account service implementation.
 * 
 * @author Ignas
 * @created 2009.09.04
 */
@Stateless
@Name("customerAccountService")
@AutoCreate
public class CustomerAccountService extends AccountService<CustomerAccount> implements CustomerAccountServiceLocal, CustomerAccountServiceRemote {

	@In
	private CustomerServiceLocal customerService;

	@In
	private OtherCreditAndChargeServiceLocal otherCreditAndChargeService;

	@Logger
	protected Log log;

	@In
	private TitleServiceLocal titleService;

	ResourceBundle recourceMessage = ResourceBundle.getBundle("messages");

	/**
	 * @see org.meveo.service.payments.local.CustomerAccountServiceLocal#isCustomerAccountWithIdExists(java.lang.Long)
	 */
	public boolean isCustomerAccountWithIdExists(Long id) {
		Query query = em.createQuery("select count(*) from CustomerAccount a where a.id = :id");
		query.setParameter("id", id);
		return (Integer) query.getSingleResult() > 0;
	}

	@SuppressWarnings("unchecked")
	public List<String> getAllBillingKeywords() {
		Query query = em.createQuery("select distinct(billingKeyword) from CustomerAccount");
		return query.getResultList();
	}

	public List<CustomerAccount> importCustomerAccounts(List<CustomerAccount> customerAccountsToImport) {
		List<CustomerAccount> failedImports = new ArrayList<CustomerAccount>();
		return failedImports;
	}

	private BigDecimal computeOccAmount(CustomerAccount customerAccount, OperationCategoryEnum operationCategoryEnum, boolean isDue, Date to,
			MatchingStatusEnum... status) throws Exception {

		BigDecimal balance = null;
		QueryBuilder queryBuilder = new QueryBuilder("select sum(unMatchingAmount) from AccountOperation");
		queryBuilder.addCriterionEnum("transactionCategory", operationCategoryEnum);
		if (isDue) {
			queryBuilder.addCriterion("dueDate", "<=", to, false);
		} else {
			queryBuilder.addCriterion("transactionDate", "<=", to, false);
		}
		queryBuilder.addCriterionEntity("customerAccount", customerAccount);
		if (status.length == 1) {
			queryBuilder.addCriterionEnum("matchingStatus", status[0]);
		} else {
			queryBuilder.startOrClause();
			for (MatchingStatusEnum st : status) {
				queryBuilder.addCriterionEnum("matchingStatus", st);
			}
			queryBuilder.endOrClause();
		}
		Query query = queryBuilder.getQuery(em);
		balance = (BigDecimal) query.getSingleResult();
		return balance;
	}

	private BigDecimal computeBalance(CustomerAccount customerAccount, Date to, boolean isDue, MatchingStatusEnum... status) throws BusinessException {
		log.info("computeBalance  customerAccount:" + (customerAccount == null ? "null" : customerAccount.getCode()) + " toDate:" + to);
		if (customerAccount == null) {
			log.warn("Error when customerAccount is null!");
			throw new BusinessException("customerAccount is null");
		}
		if (to == null) {
			log.warn("Error when toDate is null!");
			throw new BusinessException("toDate is null");
		}
		BigDecimal balance = null, balanceDebit = null, balanceCredit = null;
		try {
			balanceDebit = computeOccAmount(customerAccount, OperationCategoryEnum.DEBIT, isDue, to, status);
			balanceCredit = computeOccAmount(customerAccount, OperationCategoryEnum.CREDIT, isDue, to, status);
			if (balanceDebit == null) {
				balanceDebit = BigDecimal.ZERO;
			}
			if (balanceCredit == null) {
				balanceCredit = BigDecimal.ZERO;
			}
			balance = balanceDebit.subtract(balanceCredit);
			ParamBean param = ParamBean.getInstance("meveo-admin.properties");
			int balanceFlag = Integer.parseInt(param.getProperty("balance.flagLDE"));
			balance = balance.multiply(new BigDecimal(balanceFlag));
			log.info("successfully end customerAccountBalanceExligible with customerAccount code:#0 , balanceExigible:#1 ", customerAccount.getCode(), balance);
		} catch (Exception e) {
			throw new BusinessException("Internal error");
		}
		return balance;

	}

	public BigDecimal customerAccountBalanceExigible(CustomerAccount customerAccount, Date to) throws BusinessException {
		log.info("customerAccountBalanceExigible  customerAccount:" + (customerAccount == null ? "null" : customerAccount.getCode()) + " toDate:" + to);
		return computeBalance(customerAccount, to, true, MatchingStatusEnum.O, MatchingStatusEnum.P, MatchingStatusEnum.I);

	}

	public BigDecimal customerAccountBalanceExigibleWithoutLitigation(Long customerAccountId, String customerAccountCode, Date to) throws BusinessException {
		log.info("customerAccountBalanceExigibleWithoutLitigation with id:#0,code:#1,toDate:#2", customerAccountId, customerAccountCode, to);
		return customerAccountBalanceExigibleWithoutLitigation(findCustomerAccount(customerAccountId, customerAccountCode), to);
	}

	public BigDecimal customerAccountBalanceExigibleWithoutLitigation(CustomerAccount customerAccount, Date to) throws BusinessException {
		log.info("customerAccountBalanceExigibleWithoutLitigation  customerAccount:" + (customerAccount == null ? "null" : customerAccount.getCode())
				+ " toDate:" + to);
		return computeBalance(customerAccount, to, true, MatchingStatusEnum.O, MatchingStatusEnum.P);
	}

	public BigDecimal customerAccountBalanceDue(CustomerAccount customerAccount, Date to) throws BusinessException {
		log.info("customerAccountBalanceDue  customerAccount:" + (customerAccount == null ? "null" : customerAccount.getCode()) + " toDate:" + to);
		return computeBalance(customerAccount, to, false, MatchingStatusEnum.O, MatchingStatusEnum.P, MatchingStatusEnum.I);
	}

	public BigDecimal customerAccountBalanceDueWithoutLitigation(Long customerAccountId, String customerAccountCode, Date to) throws BusinessException {
		log.info("customerAccountBalanceDueWithoutLitigation with id:#0,code:#1,toDate:#2", customerAccountId, customerAccountCode, to);
		return customerAccountBalanceDueWithoutLitigation(findCustomerAccount(customerAccountId, customerAccountCode), to);
	}

	public BigDecimal customerAccountBalanceDueWithoutLitigation(CustomerAccount customerAccount, Date to) throws BusinessException {
		log.info("customerAccountBalanceDueWithoutLitigation  customerAccount:" + (customerAccount == null ? "null" : customerAccount.getCode()) + " toDate:"
				+ to);
		return computeBalance(customerAccount, to, false, MatchingStatusEnum.O, MatchingStatusEnum.P);
	}

	/**
	 * @see org.meveo.service.payments.local.CustomerAccountServiceLocal#customerAccountBalanceExigible(java.lang.Long,
	 *      java.lang.String, java.util.Date)
	 */
	public BigDecimal customerAccountBalanceExigible(Long customerAccountId, String customerAccountCode, Date to) throws BusinessException {
		log.info("customerAccountBalanceExligible with id:#0,code:#1,toDate:#2", customerAccountId, customerAccountCode, to);
		return customerAccountBalanceExigible(findCustomerAccount(customerAccountId, customerAccountCode), to);
	}

	/**
	 * @see org.meveo.service.payments.local.CustomerAccountServiceLocal#customerAccountBalanceDue(java.lang.Long,
	 *      java.lang.String, java.util.Date)
	 */
	public BigDecimal customerAccountBalanceDue(Long customerAccountId, String customerAccountCode, Date to) throws BusinessException {
		log.info("start customerAccountBalanceDue with id:#0,code:#1,toDate:#2", customerAccountId, customerAccountCode, to);
		return customerAccountBalanceDue(findCustomerAccount(customerAccountId, customerAccountCode), to);
	}

	public void createCustomerAccount(String code, String title, String firstName, String lastName, String address1, String address2, String zipCode,
			String city, String state, String email, Long customerId, CreditCategoryEnum creditCategory, PaymentMethodEnum paymentMethod, User user)
			throws BusinessException {
		log.info("start createCustomerAccount with code:#0,customerId:#1", code, customerId);
		if (code == null || code.trim().equals("") || customerId == null || user == null) {
			log.warn("Error: requried value(s) is null with code:#0,customerId:#1,creator:#2", code, customerId, user != null ? user.getUserName() : "NULL");
			throw new BusinessException("Error when required value(s) is required");
		}
		log.info("create customer account with code:#0 by creator:#1", code, user.getUserName());
		CustomerAccount customerAccount = null;
		try {
			customerAccount = findCustomerAccount(null, code);
		} catch (Exception e) {
		}

		if (customerAccount != null) {
			log.warn("Error when one customer account existed with code:#0", code);
			throw new BusinessException("Error: one customer account existed with code:" + code + " when create new customer account!");
		}
		Customer customer = getCustomerById(customerId);

		customerAccount = new CustomerAccount();
		customerAccount.setCustomer(customer);
		customerAccount.setCode(code);
		customerAccount.setName(new org.meveo.model.shared.Name());
		customerAccount.getName().setTitle(titleService.findByCode(customer.getProvider(), title));
		customerAccount.getName().setFirstName(firstName);
		customerAccount.getName().setLastName(lastName);
		customerAccount.setAddress(new Address());
		customerAccount.getAddress().setAddress1(address1);
		customerAccount.getAddress().setAddress2(address2);
		customerAccount.getAddress().setZipCode(zipCode);
		customerAccount.getAddress().setCity(city);
		customerAccount.getAddress().setState(state);
		customerAccount.setContactInformation(new ContactInformation());
		customerAccount.getContactInformation().setEmail(email);
		customerAccount.setStatus(CustomerAccountStatusEnum.ACTIVE);
		customerAccount.setDunningLevel(DunningLevelEnum.R0);
		customerAccount.setDateStatus(new Date());
		customerAccount.setDateDunningLevel(new Date());
		customerAccount.setPaymentMethod(paymentMethod);
		customerAccount.setCreditCategory(creditCategory);
		customerAccount.setProvider(customer.getProvider());

		try {
			this.create(customerAccount, user);
		} catch (Exception e) {
			log.warn("Error when create one customer account with code:#0,customerId:#1,creator:#2", code, customerId, user.getUserName());
			throw new BusinessException("Error:" + e.getMessage() + " when create a new customer account with code:" + code + ",customerId:" + customerId
					+ ",creator:" + user.getUserName());
		}
		log.info("successfully create one customer account with code:#0,customerId:#1,creator:#2", code, customerId, user.getUserName());
	}

	/**
	 * @see org.meveo.service.payments.local.CustomerAccountServiceLocal#updateCustomerAccount(java.lang.Long,
	 *      java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String, org.meveo.model.payments.CreditCategoryEnum,
	 *      org.meveo.model.payments.PaymentMethodEnum,
	 *      org.meveo.model.admin.User)
	 */
	public void updateCustomerAccount(Long id, String code, String title, String firstName, String lastName, String address1, String address2, String zipCode,
			String city, String state, String email, CreditCategoryEnum creditCategory, PaymentMethodEnum paymentMethod, User user) throws BusinessException {
		log.info("start updateCustomerAccount with code:#0,id:#1,updator=#2", code, id, (user != null ? user.getUserName() : "NULL"));
		if ((code == null || code.trim().equals("")) || user == null) {
			log.warn("Error when require value(s) is null!");
			throw new BusinessException("Error when required values(s) is null!");
		}
		CustomerAccount customerAccount = findCustomerAccount(id, code);

		if (customerAccount.getName() == null)
			customerAccount.setName(new org.meveo.model.shared.Name());
		customerAccount.getName().setTitle(titleService.findByCode(customerAccount.getProvider(), code));
		customerAccount.getName().setFirstName(firstName);
		customerAccount.getName().setLastName(lastName);
		if (customerAccount.getAddress() == null)
			customerAccount.setAddress(new Address());
		customerAccount.getAddress().setAddress1(address1);
		customerAccount.getAddress().setAddress2(address2);
		customerAccount.getAddress().setZipCode(zipCode);
		customerAccount.getAddress().setCity(city);
		customerAccount.getAddress().setState(state);
		customerAccount.setPaymentMethod(paymentMethod);
		customerAccount.setCreditCategory(creditCategory);
		if (customerAccount.getContactInformation() == null) {
			customerAccount.setContactInformation(new ContactInformation());
		}
		customerAccount.getContactInformation().setEmail(email);

		try {
			this.update(customerAccount, user);
		} catch (Exception e) {
			log.warn("Error: #0 whne update one customer acount with code:#0", e.getMessage(), code);
			throw new BusinessException("Error: " + e.getMessage() + " when update one customer account with code:" + code);
		}
		log.info("successfully update customer account with code:#0", code);
	}

	@Transactional
	public void closeCustomerAccount(CustomerAccount customerAccount, User user) throws BusinessException, Exception {
		log.info("closeCustomerAccount customerAccount:" + (customerAccount == null ? "null" : customerAccount.getCode()) + " user:"
				+ (user != null ? user.getUserName() : "NULL"));
		if (user == null) {
			log.warn("Error in closeCustomerAccount when required user is null!");
			throw new BusinessException("Error in closeCustomerAccount when required user is null!");
		}
		if (customerAccount == null) {
			log.warn("closeCustomerAccount customerAccount is null");
			throw new BusinessException("customerAccount is null");
		}
		if (customerAccount.getStatus() == CustomerAccountStatusEnum.CLOSE) {
			log.warn("closeCustomerAccount customerAccount already closed");
			throw new BusinessException("customerAccount already closed");
		}
		try {
			log.debug("closeCustomerAccount  update customerAccount ok");
			ParamBean param = ParamBean.getInstance("meveo-admin.properties");
			String codeOCCTemplate = param.getProperty("occ.codeOccCloseAccount");
			BigDecimal balanceDue = customerAccountBalanceDue(customerAccount, new Date());
			if (balanceDue == null) {
				log.warn("closeCustomerAccount balanceDue is null");
				throw new BusinessException("balanceDue is null");
			}
			log.debug("closeCustomerAccount  balanceDue:" + balanceDue);
			if (balanceDue.compareTo(BigDecimal.ZERO) < 0) {
				throw new BusinessException(recourceMessage.getString("closeCustomerAccount.balanceDueNegatif"));
			}
			if (balanceDue.compareTo(BigDecimal.ZERO) > 0) {
				otherCreditAndChargeService.addOCC(codeOCCTemplate, null, customerAccount, balanceDue, new Date(), user);
				log.debug("closeCustomerAccount  add occ ok");
			}
			customerAccount.setStatus(CustomerAccountStatusEnum.CLOSE);
			customerAccount.setDateStatus(new Date());
			update(customerAccount, user);
			log.info("closeCustomerAccount customerAccountCode:" + customerAccount.getCode() + " closed successfully");
		} catch (BusinessException be) {
			throw be;
		} catch (Exception e) {
			throw e;
		}
	}

	public void closeCustomerAccount(Long customerAccountId, String customerAccountCode, User user) throws BusinessException, Exception {
		log.info("closeCustomerAccount customerAccountCode:" + customerAccountCode + ", customerAccountID:" + customerAccountId + "user:"
				+ (user != null ? user.getUserName() : "NULL"));
		closeCustomerAccount(findCustomerAccount(customerAccountId, customerAccountCode), user);
	}

	@Transactional
	public void transferAccount(CustomerAccount fromCustomerAccount, CustomerAccount toCustomerAccount, BigDecimal amount, User user) throws BusinessException,
			Exception {
		log.info("transfertAccount fromCustomerAccount:" + (fromCustomerAccount == null ? "null" : fromCustomerAccount.getCode()) + " toCustomerAccount:"
				+ (toCustomerAccount == null ? "null" : toCustomerAccount.getCode()) + "amount :" + amount + " user:"
				+ (user != null ? user.getUserName() : "NULL"));

		if (fromCustomerAccount == null) {
			log.warn("transfertAccount fromCustomerAccount is null");
			throw new BusinessException("fromCustomerAccount is null");
		}
		if (toCustomerAccount == null) {
			log.warn("transfertAccount toCustomerAccount is null");
			throw new BusinessException("toCustomerAccount is null");
		}
		if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
			log.warn("Error in transfertAccount amount is null");
			throw new BusinessException("amount is null");
		}
		if (user == null) {
			log.warn("Error in transfertAccount when required user is null!");
			throw new BusinessException("user is null!");
		}

		try {
			ParamBean param = ParamBean.getInstance("meveo-admin.properties");
			String occTransferAccountCredit = param.getProperty("occ.templateTransferAccountCredit");
			String occTransferAccountDebit = param.getProperty("occ.templateTransferAccountDebit");
			ResourceBundle resource = ResourceBundle.getBundle("messages");
			String descTransfertFrom = resource.getString("occ.descTransfertFrom");
			String descTransfertTo = resource.getString("occ.descTransfertFrom");

			otherCreditAndChargeService.addOCC(occTransferAccountDebit, descTransfertFrom + " " + toCustomerAccount.getCode(), fromCustomerAccount, amount,
					new Date(), user);
			otherCreditAndChargeService.addOCC(occTransferAccountCredit, descTransfertTo + " " + fromCustomerAccount.getCode(), toCustomerAccount, amount,
					new Date(), user);
			log.info("Successful transfertAccount fromCustomerAccountCode:" + fromCustomerAccount.getCode() + " toCustomerAccountCode:"
					+ toCustomerAccount.getCode());

		} catch (Exception e) {
			throw e;
		}

	}

	/**
	 * @see org.meveo.service.payments.local.CustomerAccountServiceLocal#transferAccount(java.lang.Long,
	 *      java.lang.String, java.lang.Long, java.lang.String,
	 *      java.math.BigDecimal, org.meveo.model.admin.User)
	 */
	public void transferAccount(Long fromCustomerAccountId, String fromCustomerAccountCode, Long toCustomerAccountId, String toCustomerAccountCode,
			BigDecimal amount, User user) throws BusinessException, Exception {
		log.info("transfertAccount fromCustomerAccountCode:" + fromCustomerAccountCode + " fromCustomerAccountId:" + fromCustomerAccountId
				+ " toCustomerAccountCode:" + toCustomerAccountCode + " toCustomerAccountId:" + toCustomerAccountId + "toCustomerAccountId :"
				+ toCustomerAccountId + "amount :" + amount + " user:" + (user != null ? user.getUserName() : "NULL"));
		transferAccount(findCustomerAccount(fromCustomerAccountId, fromCustomerAccountCode), findCustomerAccount(toCustomerAccountId, toCustomerAccountCode),
				amount, user);
	}

	public CustomerAccount consultCustomerAccount(Long id, String code) throws BusinessException {
		return findCustomerAccount(id, code);
	}

	/**
	 * @see org.meveo.service.payments.local.CustomerAccountServiceLocal#updateCreditCategory(java.lang.Long,
	 *      java.lang.String, org.meveo.model.payments.CreditCategoryEnum,
	 *      org.meveo.model.admin.User)
	 */
	public void updateCreditCategory(Long id, String code, CreditCategoryEnum creditCategory, User updator) throws BusinessException {
		log.info("start updateCreditCategory with id:#0,code:#1", id, code);
		if (creditCategory == null) {
			log.warn("Error when required creditCategory is null!");
			throw new BusinessException("Error when required creditCategory is null");
		}
		if (updator == null || updator.getId() == null) {
			throw new BusinessException("Error when user is null!");
		}
		CustomerAccount customerAccount = findCustomerAccount(id, code);
		customerAccount.setCreditCategory(creditCategory);
		update(customerAccount, updator);
		log.info("successfully end updateCreditCategory!");
	}

	/**
	 * update dunningLevel for one existed customer account by id or code
	 */
	public void updateDunningLevel(Long id, String code, DunningLevelEnum dunningLevel, User updator) throws BusinessException {
		log.info("start updateDunningLevel with id:#0,code:#1", id, code);
		if (dunningLevel == null) {
			log.warn("Error when required dunningLevel is null!");
			throw new BusinessException("Error when required dunningLevel is null");
		}
		if (updator == null || updator.getId() == null) {
			throw new BusinessException("Error when user is null!");
		}
		CustomerAccount customerAccount = findCustomerAccount(id, code);
		customerAccount.setDunningLevel(dunningLevel);
		customerAccount.setDateDunningLevel(new Date());
		update(customerAccount, updator);
		log.info("successfully end updateDunningLevel!");
	}

	/**
	 * update paymentMethod for one existed customer account by id or code
	 */
	public void updatePaymentMethod(Long id, String code, PaymentMethodEnum paymentMethod, User updator) throws BusinessException {
		log.info("start updatePaymentMethod with id:#0,code:#1", id, code);
		if (paymentMethod == null) {
			log.warn("Error when required paymentMethod is null!");
			throw new BusinessException("Error when required paymentMethod is null");
		}
		if (updator == null || updator.getId() == null) {
			throw new BusinessException("Error when user is null!");
		}
		CustomerAccount customerAccount = findCustomerAccount(id, code);
		customerAccount.setPaymentMethod(paymentMethod);
		update(customerAccount, updator);
		log.info("successfully end updatePaymentMethod!");

	}

	/**
	 * get operations from one existed customerAccount by id or code
	 */
	public List<AccountOperation> consultOperations(Long id, String code, Date from, Date to) throws BusinessException {
		log.info("start consultOperations with id:#0,code:#1,from:#2,to:#3", id, code, from, to);
		CustomerAccount customerAccount = findCustomerAccount(id, code);
		List<AccountOperation> operations = customerAccount.getAccountOperations();
		log.info("found accountOperation size:#0 from customerAccount code:#1,id:#2", operations != null ? operations.size() : 0, code, id);
		if (to == null) {
			to = new Date();
		}
		if (operations != null) {
			Iterator<AccountOperation> it = operations.iterator();
			while (it.hasNext()) {
				Date transactionDate = it.next().getTransactionDate();
				if (transactionDate == null)
					continue;
				if (from == null) {
					if (transactionDate.after(to)) {
						it.remove();
					}
				} else if (transactionDate.before(from) || transactionDate.after(to)) {
					it.remove();
				}
			}
		}
		log.info("found effective operations size:#0 from customerAccount code:#1,id:#2", operations != null ? operations.size() : 0, code, id);
		log.info("successfully end consultOperations");
		return operations;
	}

	private Customer getCustomerById(Long id) throws BusinessException {
		log.info("start to find one customer with id:#0", id);
		Customer result = customerService.findById(id);
		if (result == null) {
			log.warn("retrieve a null customer with id:#0", id);
			throw new BusinessException("Error when find null customer with id:" + id);
		}
		log.info("successfully end getCustomerById with id:#0", id);
		return result;
	}

	public CustomerAccount findCustomerAccount(Long id, String code) throws BusinessException {
		log.info("findCustomerAccount with code:#0,id:#1", code, id);
		if ((code == null || code.equals("")) && (id == null || id == 0)) {
			log.warn("Error: require code and id are null!");
			throw new BusinessException("Error: required code and ID are null!");
		}
		CustomerAccount customerAccount = null;
		try {
			customerAccount = (CustomerAccount) em.createQuery("from CustomerAccount where id=:id or code=:code ").setParameter("id", id)
					.setParameter("code", code).getSingleResult();
		} catch (Exception e) {
		}
		if (customerAccount == null) {
			log.warn("Errow when find nonexisted customer account ");
			throw new BusinessException("Error when find nonexisted customer account code:" + code + " , id:" + id);
		}
		return customerAccount;
	}
	public boolean isDuplicationExist(CustomerAccount customerAccount){
		if(customerAccount==null || !customerAccount.getDefaultLevel()){
			return false;
		}
		Customer customer=customerAccount.getCustomer();
		if(customer!=null){
			for(CustomerAccount ca : customer.getCustomerAccounts()){
				 if (ca.getDefaultLevel()!=null && ca.getDefaultLevel()
	                     && (customerAccount.getId() == null || (customerAccount.getId() != null && !customerAccount
	                             .getId().equals(ca.getId())))) {
		                 	return true;
		              }
		     }
		}
		 
	  return false;
    
	}
	   public boolean isAllServiceInstancesTerminated(CustomerAccount customerAccount) {

	        Query billingQuery = em.createQuery("select si from ServiceInstance si join si.subscription s join s.userAccount ua join ua.billingAccount ba join ba.customerAccount ca where ca.id = :customerAccountId");
	        billingQuery.setParameter("customerAccountId", customerAccount.getId());
	        List<ServiceInstance> services = (List<ServiceInstance>) billingQuery.getResultList();
	        for (ServiceInstance service : services) {
	            boolean serviceActive = service.getStatus() == InstanceStatusEnum.ACTIVE;
	            if (serviceActive) {
	                return false;
	            }
	        }
	        return true;
	    }
}