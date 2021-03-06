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
package org.meveo.service.base;

import javax.persistence.Query;

import org.meveo.model.BusinessEntity;
import org.meveo.model.crm.Provider;
import org.meveo.service.base.local.BusinessServiceLocal;

/**
 * @author Ignas Lelys
 * @created Dec 6, 2010
 * 
 * @param
 * <P>
 */
public abstract class BusinessService<P extends BusinessEntity> extends PersistenceService<P> implements
        BusinessServiceLocal<P> {

    @SuppressWarnings("unchecked")
    public P findByCode(String code) {
        log.debug("start of find {0} by code (code={1}) ..", getEntityClass().getSimpleName(), code);
        final Class<? extends P> productClass = getEntityClass();
        StringBuilder queryString = new StringBuilder("from " + productClass.getName() + " a");
        queryString.append(" where a.code = :code");
        Query query = em.createQuery(queryString.toString());
        query.setParameter("code", code);
        if (query.getResultList().size() == 0) {
            return null;
        }
        P e = (P) query.getResultList().get(0);
        log.debug("end of find {0} by code (code={1}). Result found={2}.", getEntityClass().getSimpleName(), code,
                e != null);

        return e;
    }

    @SuppressWarnings("unchecked")
    public P findByCode(String code, Provider provider) {
        log.debug("start of find {0} by code (code={1}) ..", getEntityClass().getSimpleName(), code);
        final Class<? extends P> productClass = getEntityClass();
        StringBuilder queryString = new StringBuilder("from " + productClass.getName() + " a");
        queryString.append(" where a.code = :code and a.provider=:provider");
        Query query = em.createQuery(queryString.toString());
        query.setParameter("code", code);
        query.setParameter("provider", provider);
        if (query.getResultList().size() == 0) {
            return null;
        }
        P e = (P) query.getResultList().get(0);
        log.debug("end of find {0} by code (code={1}). Result found={2}.", getEntityClass().getSimpleName(), code,
                e != null);

        return e;
    }

}
