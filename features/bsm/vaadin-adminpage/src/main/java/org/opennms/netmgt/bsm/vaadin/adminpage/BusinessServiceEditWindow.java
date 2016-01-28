/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.bsm.vaadin.adminpage;

import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.bsm.service.model.edge.ChildEdge;
import org.opennms.netmgt.bsm.service.model.edge.Edge;
import org.opennms.netmgt.bsm.service.model.edge.IpServiceEdge;
import org.opennms.netmgt.bsm.service.model.edge.ReductionKeyEdge;
import org.opennms.netmgt.bsm.service.model.functions.reduce.MostCritical;
import org.opennms.netmgt.bsm.service.model.functions.reduce.Threshold;
import org.opennms.netmgt.bsm.service.model.mapreduce.ReductionFunction;
import org.opennms.netmgt.vaadin.core.TransactionAwareUI;
import org.opennms.netmgt.vaadin.core.UIHelper;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import org.opennms.netmgt.vaadin.core.TransactionAwareUI;
import org.opennms.netmgt.vaadin.core.UIHelper;

/**
 * Modal dialog window used to edit the properties of a Business Service definition. This class will be
 * instantiated by the {@see BusinessServiceMainLayout} main layout.
 *
 * @author Markus Neumann <markus@opennms.com>
 * @author Christian Pape <christian@opennms.org>
 */
public class BusinessServiceEditWindow extends Window {

    private final BusinessService m_businessService;

    /**
     * the name textfield
     */
    private TextField m_nameTextField;
    /**
     * Reduce function
     */
    private NativeSelect m_reduceFunctionNativeSelect;
    /**
     * list of reduction keys
     */
    private ListSelect m_edgesListSelect;

    /**
     * Constructor
     *
     * @param businessService the Business Service DTO instance to be configured
     */
    public BusinessServiceEditWindow(BusinessService businessService,
                                     BusinessServiceManager businessServiceManager) {
        /**
         * set window title...
         */
        super("Business Service Edit");

        m_businessService = businessService;

        /**
         * ...and basic properties
         */
        setModal(true);
        setClosable(false);
        setResizable(false);
        setWidth(50, Unit.PERCENTAGE);
        setHeight(85, Unit.PERCENTAGE);

        /**
         * construct the main layout
         */
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setSpacing(true);
        verticalLayout.setMargin(true);

        /**
         * add saveBusinessService button
         */
        Button saveButton = new Button("Save");
        saveButton.setId("saveButton");
        saveButton.addClickListener(UIHelper.getCurrent(TransactionAwareUI.class).wrapInTransactionProxy(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                businessService.setName(m_nameTextField.getValue().trim());

                final ReductionFunction reductionFunction;

                try {
                    reductionFunction = ((Class<? extends ReductionFunction>) m_reduceFunctionNativeSelect.getValue()).newInstance();
                } catch (final InstantiationException | IllegalAccessException e) {
                    throw Throwables.propagate(e);
                }

                businessService.setReduceFunction(reductionFunction);
                businessService.save();
                close();
            }
        }));

        /**
         * add the cancel button
         */
        Button cancelButton = new Button("Cancel");
        cancelButton.setId("cancelButton");
        cancelButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        /**
         * add the buttons to a HorizontalLayout
         */
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.addComponent(saveButton);
        buttonLayout.addComponent(cancelButton);

        /**
         * instantiate the input fields
         */
        m_nameTextField = new TextField("Business Service Name");
        m_nameTextField.setId("nameField");
        m_nameTextField.setValue(businessService.getName());
        m_nameTextField.setWidth(100, Unit.PERCENTAGE);
        verticalLayout.addComponent(m_nameTextField);

        /**
         * create the reduce function component
         */

        m_reduceFunctionNativeSelect = new NativeSelect("Reduce Function",
                                                        ImmutableList.builder()
                                                                .add(MostCritical.class)
                                                                .add(Threshold.class)
                                                                .build());
        m_reduceFunctionNativeSelect.setId("reduceFunctionNativeSelect");
        m_reduceFunctionNativeSelect.setWidth(98.0f, Unit.PERCENTAGE);
        m_reduceFunctionNativeSelect.setNullSelectionAllowed(false);
        m_reduceFunctionNativeSelect.setMultiSelect(false);
        m_reduceFunctionNativeSelect.setNewItemsAllowed(false);

        /**
         * setting the captions for items
         */
        m_reduceFunctionNativeSelect.getItemIds().forEach(itemId -> m_reduceFunctionNativeSelect.setItemCaption(itemId, ((Class<?>) itemId).getSimpleName()));
        m_reduceFunctionNativeSelect.setValue(MostCritical.class);

        verticalLayout.addComponent(m_reduceFunctionNativeSelect);

        /**
         * create the edges list box
         */
        m_edgesListSelect = new ListSelect("Edges");
        m_edgesListSelect.setId("reductionKeySelect");
        m_edgesListSelect.setWidth(98.0f, Unit.PERCENTAGE);
        m_edgesListSelect.setRows(20);
        m_edgesListSelect.setNullSelectionAllowed(false);
        m_edgesListSelect.setMultiSelect(false);
        refreshEdges();


        /**
         * wrap the reduction key list select box in a Vaadin Panel
         */
        HorizontalLayout edgesListAndButtonLayout = new HorizontalLayout();

        edgesListAndButtonLayout.setWidth(100.0f, Unit.PERCENTAGE);

        VerticalLayout edgesButtonLayout = new VerticalLayout();
        edgesButtonLayout.setWidth(140.0f, Unit.PIXELS);

        Button addEdgeButton = new Button("Add");
        addEdgeButton.setWidth(140.0f, Unit.PIXELS);
        addEdgeButton.addStyleName("small");
        edgesButtonLayout.addComponent(addEdgeButton);
        addEdgeButton.addClickListener((Button.ClickListener) event -> {
            final BusinessServiceEdgeEditWindow window = new BusinessServiceEdgeEditWindow(businessService, businessServiceManager);
            window.addCloseListener(e -> refreshEdges());
            this.getUI().addWindow(window);
        });

        final Button removeReductionKeyBtn = new Button("Remove");
        removeReductionKeyBtn.setEnabled(false);
        removeReductionKeyBtn.setWidth(140.0f, Unit.PIXELS);
        removeReductionKeyBtn.addStyleName("small");
        edgesButtonLayout.addComponent(removeReductionKeyBtn);

        m_edgesListSelect.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                removeReductionKeyBtn.setEnabled(event.getProperty().getValue() != null);
            }
        });

        removeReductionKeyBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (m_edgesListSelect.getValue() != null) {
                    m_edgesListSelect.removeItem(m_edgesListSelect.getValue());
                    removeReductionKeyBtn.setEnabled(false);

                    refreshEdges();
                }
            }
        });

        edgesListAndButtonLayout.addComponent(m_edgesListSelect);
        edgesListAndButtonLayout.setExpandRatio(m_edgesListSelect, 1.0f);

        edgesListAndButtonLayout.addComponent(edgesButtonLayout);
        edgesListAndButtonLayout.setComponentAlignment(edgesButtonLayout, Alignment.BOTTOM_CENTER);
        verticalLayout.addComponent(edgesListAndButtonLayout);

        /**
         * now add the button layout to the main layout
         */
        verticalLayout.addComponent(buttonLayout);
        verticalLayout.setExpandRatio(buttonLayout, 1.0f);

        verticalLayout.setComponentAlignment(buttonLayout, Alignment.BOTTOM_RIGHT);

        /**
         * set the window's content
         */
        setContent(verticalLayout);
    }

    private void refreshEdges() {
        m_edgesListSelect.removeAllItems();
        m_edgesListSelect.addItems(m_businessService.getEdges());
        m_edgesListSelect.getItemIds().forEach(item -> {
            m_edgesListSelect.setItemCaption(item, describeEdge((Edge) item));
        });
    }

    public static String describeBusinessService(final BusinessService businessService) {
        return businessService.getName();
    }

    public static String describeIpService(final IpService ipService) {
        return String.format("%s %s %s",
                             ipService.getNodeLabel(),
                             ipService.getIpAddress(),
                             ipService.getServiceName());
    }

    public static String describeReductionKey(final String reductionKey) {
        return reductionKey;
    }

    public static String describeEdge(final Edge edge) {
        switch (edge.getType()) {
            case CHILD_SERVICE:
                return "Child: " + describeBusinessService(((ChildEdge) edge).getChild());

            case IP_SERVICE:
                return "IPSvc: " + describeIpService(((IpServiceEdge) edge).getIpService());

            case REDUCTION_KEY:
                return "ReKey: " + describeReductionKey(((ReductionKeyEdge) edge).getReductionKey());

            default:
                throw new IllegalArgumentException();
        }
    }
}
