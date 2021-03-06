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
package org.meveo.service.crm.impl;

import javax.ejb.Stateless;
import javax.persistence.Query;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.meveo.model.crm.Customer;
import org.meveo.service.base.PersistenceService;
import org.meveo.service.crm.local.CustomerServiceLocal;

/**
 * Customer service implementation.
 * 
 * @author Gediminas Ubartas
 * @created 2010.11.15
 */
@Stateless
@Name("customerService")
@AutoCreate
public class CustomerService extends PersistenceService<Customer> implements CustomerServiceLocal {

    public Customer findByCode(String code) {
        Query query = em.createQuery("from " + Customer.class.getSimpleName() + " where code=:code").setParameter(
                "code", code);
        if (query.getResultList().size() == 0) {
            return null;
        }
        return (Customer) query.getResultList().get(0);
    }
}
