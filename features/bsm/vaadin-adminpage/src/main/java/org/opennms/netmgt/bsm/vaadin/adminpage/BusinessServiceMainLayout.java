/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.bsm.vaadin.adminpage;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.graph.BusinessServiceGraph;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;
import org.opennms.netmgt.vaadin.core.TransactionAwareUI;
import org.opennms.netmgt.vaadin.core.UIHelper;

import com.vaadin.data.Container;
import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;
import org.opennms.netmgt.vaadin.core.TransactionAwareUI;
import org.opennms.netmgt.vaadin.core.UIHelper;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

/**
 * This class represents the main  Vaadin component for editing Business Service definitions.
 *
 * @author Markus Neumann <markus@opennms.com>
 * @author Christian Pape <christian@opennms.org>
 */
public class BusinessServiceMainLayout extends VerticalLayout {
    private static final long serialVersionUID = -6753816061488048389L;

    /**
     * the Business Service Manager instance
     */
    private final BusinessServiceManager m_businessServiceManager;

    /**
     * the table instance
     */
    private final TreeTable m_table;

    /**
     * the bean item container for the listed Business Service DTOs
     */
    private final BeanContainer<Long, BusinessServiceRow> m_beanContainer = new BeanContainer<>(BusinessServiceRow.class);

    /**
     * Used to allocate unique IDs to the table rows.
     */
    private AtomicLong m_rowIdCounter = new AtomicLong();

    public BusinessServiceMainLayout(BusinessServiceManager businessServiceManager) {
        m_businessServiceManager = Objects.requireNonNull(businessServiceManager);

        setSizeFull();

        /**
         * construct the upper layout for the createBusinessService button and field
         */
        HorizontalLayout upperLayout = new HorizontalLayout();

        // Reload button to allow manual reloads of the state machine
        final Button reloadButton = UIHelper.createButton("Reload Daemon", "Reloads the Business Service State Machine", null, (Button.ClickListener) event -> {
            m_businessServiceManager.triggerDaemonReload();
        });

        // create Button
        final Button createButton = new Button("Create");
        createButton.setId("createButton");
        createButton.addClickListener((Button.ClickListener) event -> {
            final BusinessService businessService = m_businessServiceManager.createBusinessService();
            final BusinessServiceEditWindow window = new BusinessServiceEditWindow(businessService, m_businessServiceManager);
            window.addCloseListener(e -> refreshTable());
            getUI().addWindow(window);
        });

        /**
         * add to the upper layout
         */
        upperLayout.setSpacing(true);
        upperLayout.addComponent(reloadButton);
        upperLayout.addComponent(createButton);
        addComponent(upperLayout);
        /**
         * and set the upper-right alignment
         */
        setComponentAlignment(upperLayout, Alignment.TOP_RIGHT);

        /**
         * now construct the table...
         */
        m_table = new TreeTable();
        m_table.setSizeFull();
        m_table.setContainerDataSource(m_beanContainer);

        /**
         * ...and configure the visible columns
         */
        m_table.setVisibleColumns("name");

        /**
         * add edit and delete buttons
         */
        m_table.addGeneratedColumn("edit / delete", new Table.ColumnGenerator() {
            private static final long serialVersionUID = 7113848887128656685L;

            @Override
            public Object generateCell(Table source, Object itemId, Object columnId) {
                HorizontalLayout layout = new HorizontalLayout();
                layout.setSpacing(true);

                Button editButton = new Button("Edit", FontAwesome.PENCIL_SQUARE_O);
               //editButton.addStyleName("small");
                editButton.setId("editButton-" + m_beanContainer.getItem(itemId).getBean().getName());

                editButton.addClickListener(UIHelper.getCurrent(TransactionAwareUI.class).wrapInTransactionProxy((Button.ClickListener) event -> {
                    final Long businessServiceId = m_beanContainer.getItem(itemId).getBean().getBusinessService().getId();
                    BusinessService businessService = m_businessServiceManager.getBusinessServiceById(businessServiceId);
                    final BusinessServiceEditWindow window = new BusinessServiceEditWindow(businessService, m_businessServiceManager);
                    window.addCloseListener(e -> refreshTable());

                    getUI().addWindow(window);
                }));
                layout.addComponent(editButton);

                Button deleteButton = new Button("Delete", FontAwesome.TRASH_O);
                //deleteButton.addStyleName("small");
                deleteButton.setId("deleteButton-" + m_beanContainer.getItem(itemId).getBean().getName());

                deleteButton.addClickListener((Button.ClickListener)event -> {
                    final Long businessServiceId = m_beanContainer.getItem(itemId).getBean().getBusinessService().getId();
                    BusinessService businessService = m_businessServiceManager.getBusinessServiceById(businessServiceId);
                    if (businessService.getParentServices().isEmpty() && businessService.getChildEdges().isEmpty()) {
                        UIHelper.getCurrent(TransactionAwareUI.class).runInTransaction(() -> {
                            m_businessServiceManager.getBusinessServiceById(businessServiceId).delete();
                            refreshTable();
                        });
                    } else {
                        new org.opennms.netmgt.vaadin.core.ConfirmationDialog()
                            .withOkAction((org.opennms.netmgt.vaadin.core.ConfirmationDialog.Action) UIHelper.getCurrent(TransactionAwareUI.class).wrapInTransactionProxy(new org.opennms.netmgt.vaadin.core.ConfirmationDialog.Action() {
                                @Override
                                public void execute(org.opennms.netmgt.vaadin.core.ConfirmationDialog window) {
                                    m_businessServiceManager.getBusinessServiceById(businessServiceId).delete();
                                    refreshTable();
                                }
                            }))
                            .withOkLabel("Delete anyway")
                            .withCancelLabel("Cancel")
                            .withCaption("Warning")
                            .withDescription("This entry is referencing or is referenced by other Business Services! Do you really want to delete this entry?")
                            .open();
                    }
                });
                layout.addComponent(deleteButton);

                return layout;
            }
        });

        m_table.setColumnExpandRatio("name", 5);
        m_table.setColumnExpandRatio("edit / delete", 1);

        /**
         * add the table to the layout
         */
        addComponent(m_table);
        setExpandRatio(m_table, 1.0f);

        /**
         * initial refresh of table
         */
        refreshTable();
    }

    /**
     * Returns the Business Service Manager instance associated with this instance.
     *
     * @return the instance of the associated Business Service Manager
     */
    public BusinessServiceManager getBusinessServiceManager() {
        return m_businessServiceManager;
    }

    private void createRowForVertex(BusinessServiceGraph graph, GraphVertex graphVertex, Long parentRowId) {
        final BusinessService businessService = graphVertex.getBusinessService();
        if (businessService == null) {
            return;
        }

        final long rowId = m_rowIdCounter.incrementAndGet();
        m_beanContainer.addBean(new BusinessServiceRow(businessService, rowId));
        if (parentRowId != null) {
            m_table.setParent(rowId, parentRowId.longValue());
        }

        // Recurse with all of the children
        graph.getOutEdges(graphVertex).stream()
            .map(e -> graph.getOpposite(graphVertex, e))
            .sorted((v1, v2) -> v1.getBusinessService().getName().compareTo(v2.getBusinessService().getName()))
            .forEach(v -> createRowForVertex(graph, v, rowId));
    }

    /**
     * Refreshes the entries of the table used for listing the DTO instances.
     */
    private void refreshTable() {
        m_beanContainer.setBeanIdProperty("id");
        m_beanContainer.removeAllItems();
        m_rowIdCounter.set(0);

        // Build a graph using all of the business services stored in the database
        // We don't use the existing graph, since it only contains the services know by the state machine
        final BusinessServiceGraph graph = m_businessServiceManager.getGraph(m_businessServiceManager.getAllBusinessServices());

        // Recursively generate the table rows, starting with the roots
        graph.getVerticesByLevel(0).stream()
            .filter(v -> v.getBusinessService() != null)
            .sorted((v1, v2) -> v1.getBusinessService().getName().compareTo(v2.getBusinessService().getName()))
            .forEach(v -> createRowForVertex(graph, v, null));

        for (Object itemId: m_table.getContainerDataSource().getItemIds()) {
            // Disable the collapse flag on items without any children
            m_table.setChildrenAllowed(itemId, m_table.hasChildren(itemId));
            // Expand the tree on refresh
            m_table.setCollapsed(itemId, false);
        }

        // Let the ContainerStrategy know that we changed the item set
        m_table.containerItemSetChange(new ItemSetChangeEvent() {
            private static final long serialVersionUID = 1L;
            @Override
            public Container getContainer() {
                return m_beanContainer;
            }
        });
    }
}
