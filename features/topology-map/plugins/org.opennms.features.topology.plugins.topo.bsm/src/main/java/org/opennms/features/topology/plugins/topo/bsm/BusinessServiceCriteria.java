/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.topo.bsm;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.opennms.features.topology.api.NamespaceAware;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.VertexHopCriteria;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.model.BusinessServiceDTO;

public class BusinessServiceCriteria extends VertexHopCriteria implements NamespaceAware {
    private final String businessServiceId;
    private final BusinessServiceManager businessServiceManager;

    public BusinessServiceCriteria(String businessServiceId, String businessServiceName, BusinessServiceManager businessServiceManager) {
        super(businessServiceId, businessServiceName);
        this.businessServiceId = businessServiceId;
        this.businessServiceManager = Objects.requireNonNull(businessServiceManager);
    }

    @Override
    public String getNamespace() {
        return BusinessServicesTopologyProvider.TOPOLOGY_NAMESPACE;
    }

    public boolean apply(VertexRef vertexRef) {
        if (contributesTo(vertexRef.getNamespace())) {
            final AbstractBusinessServiceVertex businessServiceVertex = (AbstractBusinessServiceVertex) vertexRef;
            return businessServiceVertex.isPartOf(businessServiceId);
        }
        return false;
    }

    @Override
    public boolean contributesTo(String namespace) {
        return getNamespace() != null && Objects.equals(namespace, getNamespace());
    }

    @Override
    public int hashCode() {
        return Objects.hash(businessServiceId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BusinessServiceCriteria other = (BusinessServiceCriteria) obj;
        return Objects.equals(businessServiceId, other.businessServiceId);
    }

    @Override
    public String toString() {
        return String.format("BusinessServiceCriteria[id=%s]", businessServiceId);
    }

    public String getBusinessServiceId() {
        return businessServiceId;
    }

    /**
     * This is called by the VertexHopGraphProvider in order to get the set of
     * vertices that are in focus.
     */
    @Override
    public Set<VertexRef> getVertices() {
        Set<VertexRef> vertices = new HashSet<>();

        BusinessServiceDTO businessService = businessServiceManager.getById(Long.parseLong(businessServiceId));
        if (businessService != null) {
            vertices.add(new BusinessServiceVertex(businessService));
        }

        return vertices;
    }
}