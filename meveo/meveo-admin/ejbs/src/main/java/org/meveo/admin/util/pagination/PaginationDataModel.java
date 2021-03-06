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
package org.meveo.admin.util.pagination;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;

import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.Range;
import org.ajax4jsf.model.SequenceRange;
import org.ajax4jsf.model.SerializableDataModel;
import org.meveo.commons.utils.PaginationConfiguration;
import org.meveo.model.IEntity;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.rating.impl.UsageTypeService;
import org.richfaces.model.FilterField;
import org.richfaces.model.Modifiable;
import org.richfaces.model.SortField2;

/**
 * Pagination data model. This data model is used together with
 * PersistenceService, which is responsible for actual data load from the
 * database using specific filtering and sorting. Concrete implementation of
 * Persistence service is passed through constructor.
 * 
 * @author Ignas
 * @created 2009.09.24
 */
public class PaginationDataModel<T> extends SerializableDataModel implements Modifiable {

    private static final long serialVersionUID = 1L;

    private Integer rowCount;
    private boolean isDetached = false;
    private Serializable currentPk;
    private int currentFirstRow = -1;

    @SuppressWarnings("unchecked")
    private IPersistenceService service;

    private final List<Serializable> wrappedKeys = new LinkedList<Serializable>();
    private final Map<Serializable, T> wrappedData = new HashMap<Serializable, T>();
    private Map<String, Object> filters = new HashMap<String, Object>();
    private List<String> fetchFields = new ArrayList<String>();

    /**
     * Constructor.
     * 
     * @param service
     *            Persistence service for concrete entity implementation. For
     *            example {@link UsageTypeService}.
     */
    @SuppressWarnings("unchecked")
    public PaginationDataModel(IPersistenceService service) {
        this.service = service;
    }

    /**
     * @return
     */
    protected String getSortField() {
        final FacesContext context = FacesContext.getCurrentInstance();
        final String sortField = context.getExternalContext().getRequestParameterMap().get("sortField");
        return sortField;
    }

    /**
     * @param filters
     */
    public void addFilters(Map<String, Object> filters) {
        this.filters = filters;
    }

    /**
     * @param fetch
     *            fields
     */
    public void addFetchFields(List<String> fetchFields) {
        this.fetchFields = fetchFields;
    }

    /**
     * @return
     */
    private PaginationConfiguration.Ordering getSortOrdering() {
        final FacesContext context = FacesContext.getCurrentInstance();
        final String sortField = context.getExternalContext().getRequestParameterMap().get("sortOrder");
        return "desc".equals(sortField) ? PaginationConfiguration.Ordering.DESCENDING
                : PaginationConfiguration.Ordering.ASCENDING;
    }

    /**
     * Load data using pagination configuration information for sorting, paging
     * and filtering.
     * 
     * @param paginatingData
     *            Paginating data.
     * @return
     */
    @SuppressWarnings("unchecked")
    protected List<T> loadData(PaginationConfiguration paginatingData) {
        return service.list(paginatingData);
    }

    /**
     * @param paginatingData
     * @return
     */
    protected int countRecords(PaginationConfiguration paginatingData) {
        return (int) service.count(paginatingData);
    }

    /**
     * This is main part of Visitor pattern. Method called by framework many
     * times during request processing.
     */
    @Override
    public void walk(FacesContext context, DataVisitor visitor, Range range, Object argument) throws IOException {

        final int firstRow = ((SequenceRange) range).getFirstRow();
        final int numberOfRows = ((SequenceRange) range).getRows();

        if (isDetached && (firstRow == currentFirstRow)) {
            for (Serializable key : wrappedKeys) {
                setRowKey(key);
                visitor.process(context, key, argument);
            }
        } else {
            wrappedKeys.clear();
            wrappedData.clear();
            final List<T> objects = loadData(new PaginationConfiguration(firstRow, numberOfRows, filters, fetchFields,
                    getSortField(), getSortOrdering()));
            for (T object : objects) {
                final Serializable id = getId(object);
                wrappedKeys.add(id);
                wrappedData.put(id, object);
                visitor.process(context, id, argument);
            }

            currentFirstRow = firstRow;
        }
    }

    /**
     * This method return list of filtered entities
     */
    public List<T> list() {
        final int numberOfRows = this.getRowCount();
        final List<T> objects = loadData(new PaginationConfiguration(0, numberOfRows, filters, fetchFields,
                getSortField(), getSortOrdering()));

        return objects;
    }

    /**
     * This method suppose to produce SerializableDataModel that will be
     * serialized into View State and used on a post-back.
     */
    @Override
    public SerializableDataModel getSerializableModel(Range range) {
        if (wrappedKeys != null) {
            isDetached = true;
            return this;
        }

        return null;
    }

    /**
     * This is helper method that is called by framework after model update.
     */
    @Override
    public void update() {
    }

    /**
     * This method must return actual data rows count from the Data Provider. It
     * is used by pagination control to determine total number of data items.
     */
    @Override
    public int getRowCount() {
        if (rowCount == null) {
            rowCount = countRecords(new PaginationConfiguration(filters));
        }
        return rowCount;
    }

    /**
     * This is main way to obtain data row. It is intensively used by framework.
     * (Recommend use of local cache in that method).
     */
    @Override
    public Object getRowData() {
        return getRowDataT();
    }

    /**
     * Used by getRowData() method, also if needed to get row data of T type
     * (not as object).
     */
    public T getRowDataT() {
        if (currentPk == null) {
            return null;
        } else {
            T object = wrappedData.get(currentPk);
            /**
             * if (object == null) { object = getObjectById(currentPk);
             * wrappedData.put(currentPk, object); }
             **/
            return object;
        }
    }

    /**
     * 
     */
    public Object getRowData(Serializable currentRow) {
        if (currentRow == null) {
            return null;
        } else {
            T object = wrappedData.get(currentRow);
            /**
             * if (object == null) { object = getObjectById(currentPk);
             * wrappedData.put(currentPk, object); }
             **/
            return object;
        }
    }

    /**
     * Never called by framework.
     */
    @Override
    public boolean isRowAvailable() {
        if (currentPk == null) {
            return false;
        }
        if (wrappedKeys.contains(currentPk)) {
            return true;
        }
        if (wrappedData.entrySet().contains(currentPk)) {
            return true;
        }
        /**
         * if (getObjectById(currentPk) != null) { return true; }
         **/
        return false;
    }

    /**
     * This method never called from framework.
     * 
     * @see org.ajax4jsf.model.ExtendedDataModel#getRowKey()
     */
    @Override
    public Object getRowKey() {
        return currentPk;
    }

    /**
     * This method normally called by Visitor before request Data Row.
     */
    @Override
    public void setRowKey(final Object key) {
        this.currentPk = (Serializable) key;
    }

    /**
     * Force a complete database refresh at the next call.
     */
    public void forceRefresh() {
        rowCount = null;
        currentFirstRow = -1;
    }

    protected Serializable getId(T object) {
        return ((IEntity) object).getId();
    }

    /**
     * Unused rudiment from old JSF staff.
     */
    @Override
    public Object getWrappedData() {
        return wrappedData.values();
    }

    public Set<Serializable> getKeySet() {
        return wrappedData.keySet();
    }

    /**
     * Unused rudiment from old JSF staff.
     */
    @Override
    public void setWrappedData(Object data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRowIndex(int index) {
    }

    @Override
    public int getRowIndex() {
        return 0;
    }

    public void modify(List<FilterField> filterFields, List<SortField2> sortFields) {
        isDetached = false;
    }
}