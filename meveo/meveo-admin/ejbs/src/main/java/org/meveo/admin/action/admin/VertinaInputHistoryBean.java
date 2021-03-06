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
package org.meveo.admin.action.admin;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.meveo.admin.action.BaseBean;
import org.meveo.admin.util.pagination.PaginationDataModel;
import org.meveo.model.admin.VertinaInputHistory;
import org.meveo.model.billing.ApplicationChgStatusEnum;
import org.meveo.model.billing.ChargeApplication;
import org.meveo.model.billing.RatedTransactionStatusEnum;
import org.meveo.service.admin.local.VertinaInputHistoryServiceLocal;
import org.meveo.service.base.PersistenceService;
import org.meveo.service.base.local.IPersistenceService;

/**
 * Standard backing bean for {@link VertinaInputHistory} (extends
 * {@link BaseBean} that provides almost all common methods to handle entities
 * filtering/sorting in datatable, their create, edit, view, delete operations).
 * It works with Manaty custom JSF components.
 * 
 * @author Ignas
 * @created 2009.10.13
 */
@Name("vertinaInputHistoryBean")
@Scope(ScopeType.CONVERSATION)
public class VertinaInputHistoryBean extends BaseBean<VertinaInputHistory> {

    private static final long serialVersionUID = 1L;

    /**
     * Injected @{link VertinaInputHistory} service. Extends
     * {@link PersistenceService}.
     */
    @In
    private VertinaInputHistoryServiceLocal vertinaInputHistoryService;

    /**
     * Constructor. Invokes super constructor and provides class type of this
     * bean for {@link BaseBean}.
     */
    public VertinaInputHistoryBean() {
        super(VertinaInputHistory.class);
    }

    /**
     * Factory method for entity to edit. If objectId param set load that entity
     * from database, otherwise create new.
     * 
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @Begin(nested = true)
    @Factory("vertinaInputHistory")
    public VertinaInputHistory init() {
        return initEntity();
    }

    /**
     * Data model of entities for data table in GUI.
     * 
     * @return filtered entities.
     */
    @Out(value = "vertinaInputs", required = false)
    protected PaginationDataModel<VertinaInputHistory> getDataModel() {
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
    @Factory("vertinaInputs")
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
        return saveOrUpdate(entity);
    }

    /**
     * Override default list view name. (By default view name is class name
     * starting lower case + ending 's').
     * 
     * @see org.meveo.admin.action.BaseBean#getDefaultViewName()
     */
    protected String getDefaultViewName() {
        return "vertinaInputs";
    }

    /**
     * @see org.meveo.admin.action.BaseBean#getPersistenceService()
     */
    @Override
    protected IPersistenceService<VertinaInputHistory> getPersistenceService() {
        return vertinaInputHistoryService;
    }

    /**
     * Loads all charge applications. This info is shown when clicked on link in
     * ticket numbers in GUI.
     * 
     * @param id
     *            InputHistory id.
     * @return Charge application that were processed by batch with provided id.
     */
    public List<ChargeApplication> getAllChargeTickets(Long id, int status) {
        switch (status) {
        case 0:
            return vertinaInputHistoryService.getChargeApplications(id, null);
        case 1:
            return vertinaInputHistoryService.getChargeApplications(id, ApplicationChgStatusEnum.TREATED);
        case 2:
            return vertinaInputHistoryService.getChargeApplications(id, ApplicationChgStatusEnum.REJECTED);
        }
        return null;
    }

    /**
     * Loads all charge applications. This info is shown when clicked on link in
     * ticket numbers in GUI.
     * 
     * @param id
     *            InputHistory id.
     * @return Charge application that were processed by batch with provided id.
     */
    public List<ChargeApplication> getAllTransactionTickets(Long id, int status) {
        switch (status) {
        case 0:
            return vertinaInputHistoryService.getTransactions(id, null);
        case 1:
            return vertinaInputHistoryService.getTransactions(id, RatedTransactionStatusEnum.OPEN);
        case 2:
            return vertinaInputHistoryService.getTransactions(id, RatedTransactionStatusEnum.REJECTED);
        }
        return null;
    }
}
