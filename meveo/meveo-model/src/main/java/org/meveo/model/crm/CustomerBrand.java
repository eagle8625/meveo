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
package org.meveo.model.crm;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.meveo.model.BusinessEntity;

/**
 * @author R.AITYAAZZA
 * 
 */
@Entity
@Table(name = "CRM_CUSTOMER_BRAND")
@SequenceGenerator(name = "ID_GENERATOR", sequenceName = "CRM_CUSTOMER_BRAND_SEQ")
public class CustomerBrand extends BusinessEntity {

    private static final long serialVersionUID = 1L;

}
