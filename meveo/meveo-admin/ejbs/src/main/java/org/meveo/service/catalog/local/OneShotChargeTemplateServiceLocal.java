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
package org.meveo.service.catalog.local;

import java.util.List;

import javax.ejb.Local;

import org.meveo.model.catalog.OneShotChargeTemplate;

/**
 * Charge Template Service service interface.
 * 
 */
@Local
public interface OneShotChargeTemplateServiceLocal extends ChargeTemplateServiceLocal<OneShotChargeTemplate> {

    /**
     * @return List of TERMINATION type charge templates.
     */
    public List<? extends OneShotChargeTemplate> getTerminationChargeTemplates();

    /**
     * @return List of SUBSCRIPTION type charge templates.
     */
    public List<? extends OneShotChargeTemplate> getSubscriptionChargeTemplates();

}
