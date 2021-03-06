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

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.meveo.model.admin.User;
import org.meveo.model.crm.Provider;
import org.meveo.service.admin.local.UserServiceLocal;
import org.meveo.service.base.PersistenceService;
import org.meveo.service.crm.local.ProviderServiceLocal;

/**
 * Provider service implementation.
 * 
 * @author Gediminas Ubartas
 * @created 2011.03.01
 */
@Stateless
@Name("providerService")
@AutoCreate
public class ProviderService extends PersistenceService<Provider> implements ProviderServiceLocal {
    @In
    private UserServiceLocal userService;

    public Provider findByCode(String code) {
        try {
            return (Provider) em.createQuery("from " + Provider.class.getSimpleName() + " where code=:code").setParameter("code", code).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Provider> findUsersProviders(String userName) {
        User user = userService.findByUsername(userName);
        if (user != null) {
            return user.getProviders();
        } else {
            return null;
        }
	}

    @SuppressWarnings("unchecked")
    public List<Provider> getProviders() {
        List<Provider> providers = (List<Provider>) em.createQuery("from " + Provider.class.getSimpleName())
                .getResultList();
        return providers;
    }
}
