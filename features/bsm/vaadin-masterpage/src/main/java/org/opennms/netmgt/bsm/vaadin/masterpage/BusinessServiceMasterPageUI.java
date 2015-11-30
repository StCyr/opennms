/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.bsm.vaadin.masterpage;

import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.model.BusinessServiceDTO;
import org.opennms.netmgt.model.OnmsSeverity;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Theme("bsm")
@Title("Business Service Master Page")
@SuppressWarnings("serial")
public class BusinessServiceMasterPageUI extends UI {

	private final TransactionAwareBeanProxyFactory transactionAwareBeanProxyFactory;

	private BusinessServiceManager businessServiceManager;

	public BusinessServiceMasterPageUI(TransactionAwareBeanProxyFactory transactionAwareBeanProxyFactory) {
		this.transactionAwareBeanProxyFactory = Objects.requireNonNull(transactionAwareBeanProxyFactory);
	}

	@Override
	protected void init(VaadinRequest request) {
		final VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setMargin(true);

		final List<BusinessServiceDTO> serviceDTOs = businessServiceManager.findAll();
		if (serviceDTOs.isEmpty()) {
			mainLayout.addComponent(new Label("There are no Business Services defined."));
		} else {
			for (BusinessServiceDTO eachService : serviceDTOs) {
				mainLayout.addComponent(createRow(eachService));
			}
		}
		final Panel mainPanel = new Panel();
		mainPanel.setCaption("Business Services Master Page");
		mainPanel.setContent(mainLayout);
		setContent(mainPanel);
	}

	public void setBusinessServiceManager(BusinessServiceManager businessServiceManager) {
		Objects.requireNonNull(businessServiceManager);
		this.businessServiceManager = transactionAwareBeanProxyFactory.createProxy(businessServiceManager);
	}

	private HorizontalLayout createRow(BusinessServiceDTO serviceDTO) {
		HorizontalLayout rowLayout = new HorizontalLayout();
		rowLayout.setSizeFull();
		rowLayout.setSpacing(true);

		final OnmsSeverity severity = businessServiceManager.calculateStatus(serviceDTO.getId());
		Label nameLabel = new Label(serviceDTO.getName());
		nameLabel.setSizeFull();
		nameLabel.setStyleName("h1");
		nameLabel.addStyleName("bright");
		nameLabel.addStyleName("severity");
		nameLabel.addStyleName(severity.getLabel());

		rowLayout.addComponent(nameLabel);
		return rowLayout;
	}
}
