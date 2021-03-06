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
package org.meveo.admin.action;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.persistence.EntityExistsException;

import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.log.Log;
import org.meveo.admin.util.pagination.PaginationDataModel;
import org.meveo.model.BaseEntity;
import org.meveo.model.IEntity;
import org.meveo.model.crm.Provider;
import org.meveo.service.base.local.IPersistenceService;

/**
 * Base bean class. Other seam backing beans extends this class if they need
 * functionality it provides.
 * 
 * @author Ignas
 * @created 2009.09.21
 */
public abstract class BaseBean<T extends IEntity> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Logger. */
    @Logger
    protected Log log;

    /** Status messages. */
    @In
    protected StatusMessages statusMessages;

    @In(required = false)
    protected Provider currentProvider;

    /** Search filters. */
    protected Map<String, Object> filters;

    /** List of loaded entities. */
    protected PaginationDataModel<T> entities;

    /** Entity to edit/view. */
    protected T entity;
    
    /** Class of backing bean. */
    private Class<T> clazz;

    /** List of checked entities in DataModel */
    protected Map<Long, Boolean> checked = new HashMap<Long, Boolean>();

    /**
     * Request parameter. Used for loading in object by its id.
     */
    @RequestParameter
    private Long objectId;

    private String sortField;
    private String sortOrder;

   
    /**
     * Constructor.
     * 
     * @param clazz
     *            Class.
     */
    public BaseBean(Class<T> clazz) {
        super();
        this.clazz = clazz;
    }

    /**
     * Returns entity class
     * 
     * @return Class
     */
    public Class<T> getClazz() {
        return clazz;
    }

    /**
     * Initiates entity from request parameter id.
     * 
     * @param objectClass
     *            Class of the object.
     * @return Entity from database.
     */
    @SuppressWarnings("unchecked")
    public T initEntity() {
        
        if (getObjectId() != null) {
            if (getFormFieldsToFetch() == null) {
                entity = (T)getPersistenceService().findById(getObjectId());
            } else {
                entity = (T)getPersistenceService().findById(getObjectId(), getFormFieldsToFetch());
            }
            // getPersistenceService().detach(entity);
        } else {
            try {
                entity = getInstance();
                if (entity instanceof BaseEntity) {
                    ((BaseEntity) entity).setProvider(currentProvider);
                }
                // FIXME: If entity is Auditable, set here the creator and
                // creation time
            } catch (InstantiationException e) {
                log.error("Unexpected error!", e);
                throw new IllegalStateException("could not instantiate a class, abstract class");
            } catch (IllegalAccessException e) {
                log.error("Unexpected error!", e);
                throw new IllegalStateException("could not instantiate a class, constructor not accessible");
            }
        }
        return entity;
    }

    /**
     * Refresh entities data model and adds search filters. Usually it is enough
     * to invoke this method in factory on concrete seam bean which subclasses
     * BaseBean.
     */
    public void list() {
        if (entities == null) {
            entities = new PaginationDataModel<T>(getPersistenceService());
        }
        getFilters();
        // if (!filters.containsKey("provider")) {
        // filters.put("provider", currentProvider);
        // }
        entities.addFilters(filters);
        entities.addFetchFields(getListFieldsToFetch());
        entities.forceRefresh();
    }

    /**
     * Refresh entities data model and removes search filters.
     */
    public void clean() {
        if (entities == null) {
            entities = new PaginationDataModel<T>(getPersistenceService());
        }
        filters.clear();
        filters.put("provider", currentProvider);
        entities.addFilters(filters);
        entities.addFetchFields(getListFieldsToFetch());
        entities.forceRefresh();
    }

    /**
     * Save or update entity depending on if entity is transient.
     * 
     * @param entity
     *            Entity to save.
     */
    @SuppressWarnings("unchecked")
    public String saveOrUpdate(IEntity entity) {
        if (entity.isTransient()) {
            getPersistenceService().create(entity);
            statusMessages.addFromResourceBundle(Severity.INFO, "save.successful");
        } else {
            getPersistenceService().update(entity);
            statusMessages.addFromResourceBundle(Severity.INFO, "update.successful");
        }

        return back();
    }

    /**
     * Method to get Back link. If default view name is different than override
     * the method. Default name: entity's name + s;
     * 
     * @return string for navigation
     */
    public String back() {
        Object backViewParameter = null;
        if (FacesContext.getCurrentInstance() != null) {
            backViewParameter = FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get("backView");
        }
        if (backViewParameter != null) {
            return backViewParameter.toString();
        } else {
            return getDefaultViewName();
        }
    }

    /**
     * Go back and end conversation. BeforeRedirect flag is set to true, so
     * conversation is first ended and then redirect is proceeded, that means
     * that after redirect new conversation will have to be created (temp or
     * long running) so that view will have all most up to date info because it
     * will load everything from db when starting new conversation.
     * 
     * @return string for navigation
     */
    @End(beforeRedirect = true, root=false)
    public String backAndEndConversation() {
        return back();
    }

    /**
     * TODO
     */
    public String getEditViewName() {
        String className = clazz.getSimpleName();
        StringBuilder sb = new StringBuilder(className);
        sb.append("Detail");
        char[] dst = new char[1];
        sb.getChars(0, 1, dst, 0);
        sb.replace(0, 1, new String(dst).toLowerCase());
        return sb.toString();
    }

    /**
     * Generating back link.
     */
    protected String getDefaultViewName() {
        String className = clazz.getSimpleName();
        StringBuilder sb = new StringBuilder(className);
        char[] dst = new char[1];
        sb.getChars(0, 1, dst, 0);
        sb.replace(0, 1, new String(dst).toLowerCase());
        sb.append("s");
        return sb.toString();
    }

    /**
     * Delete Entity using it's ID. Add error message to {@link statusMessages}
     * if unsuccessful.
     * 
     * @param id
     *            Entity id to delete
     */
    public void delete(Long id) {
        try {
            log.info(String.format("Deleting entity %s with id = %s", clazz.getName(), id));
            getPersistenceService().remove(id);
            statusMessages.addFromResourceBundle(Severity.INFO, "delete.successful");
        } catch (Throwable t) {
            if (t.getCause() instanceof EntityExistsException) {
                log.info("delete was unsuccessful because entity is used in the system", t);
                statusMessages.addFromResourceBundle(Severity.ERROR, "error.delete.entityUsed");
            } else {
                log.info("unexpected exception when deleting!", t);
                statusMessages.addFromResourceBundle(Severity.ERROR, "error.delete.unexpected");
            }
        }
        entities.forceRefresh();
    }

    /**
     * Delete checked entities. Add error message to {@link statusMessages} if
     * unsuccessful.
     */
    @SuppressWarnings("unchecked")
    public void deleteMany() {
        try {
            // log.info(String.format("Deleting entities %s with id = %s",
            // clazz.getName(), id));
            Set<Long> idsToDelete = new HashSet<Long>();
            for (Long id : checked.keySet()) {
                if (checked.get(id)) {
                    idsToDelete.add(id);
                }
            }
            getPersistenceService().remove(idsToDelete);
            statusMessages.addFromResourceBundle(Severity.INFO, "delete.entitities.successful");
        } catch (Throwable t) {
            if (t.getCause() instanceof EntityExistsException) {
                log.info("delete was unsuccessful because entity is used in the system", t);
                statusMessages.addFromResourceBundle(Severity.ERROR, "error.delete.entityUsed");
            } else {
                log.info("unexpected exception when deleting!", t);
                statusMessages.addFromResourceBundle(Severity.ERROR, "error.delete.unexpected");
            }
        }
        entities.forceRefresh();
    }

    /**
     * Gets search filters map.
     * 
     * @return Filters map.
     */
    public Map<String, Object> getFilters() {
        if (filters == null)
            filters = new HashMap<String, Object>();
        return filters;
    }

    /**
     * Get new instance for backing bean class.
     * 
     * @return New instance.
     * 
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public T getInstance() throws InstantiationException, IllegalAccessException {
        return clazz.newInstance();
    }

    /**
     * Method that returns concrete PersistenceService. That service is then
     * used for operations on concrete entities (eg. save, delete etc).
     * 
     * @return Persistence service
     */
    @SuppressWarnings("unchecked")
    protected abstract IPersistenceService getPersistenceService();

    /**
     * Override this method if you need to fetch any fields when selecting list
     * of entities in data table. Return list of field names that has to be
     * fetched.
     */
    protected List<String> getListFieldsToFetch() {
        return null;
    }

    /**
     * Override this method if you need to fetch any fields when selecting one
     * entity to show it a form. Return list of field names that has to be
     * fetched.
     */
    protected List<String> getFormFieldsToFetch() {
        return null;
    }

    /**
     * Override this method when pop up with additional entity information is
     * needed.
     */
    protected String getPopupInfo() {
        return "No popup information. Override BaseBean.getPopupInfo() method.";
    }

    /**
     * Delete Entity using it's ID. Add error message to {@link statusMessages}
     * if unsuccessful.
     * 
     * @param id
     *            Entity id to delete
     */
    public void disable(Long id) {
        try {
            log.info(String.format("Disabling entity %s with id = %s", clazz.getName(), id));
            getPersistenceService().disable(id);
            statusMessages.addFromResourceBundle(Severity.INFO, "disabled.successful");
        } catch (Throwable t) {
            if (t.getCause() instanceof EntityExistsException) {
                log.info("delete was unsuccessful because entity is used in the system", t);
                statusMessages.addFromResourceBundle(Severity.ERROR, "error.delete.entityUsed");
            } else {
                log.info("unexpected exception when deleting!", t);
                statusMessages.addFromResourceBundle(Severity.ERROR, "error.delete.unexpected");
            }
        }
        entities.forceRefresh();
    }

    public String getSortField() {
        if (sortField == null) {
            return "";
        } else
            return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    
    /**
     * Gets checked entities.
     * 
     * @return Checked entities list
     */
    public Map<Long, Boolean> getChecked() {
        return checked;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }
}