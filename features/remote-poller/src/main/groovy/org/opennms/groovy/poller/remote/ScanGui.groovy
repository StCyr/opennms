/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2016 The OpenNMS Group, Inc.
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

package org.opennms.groovy.poller.remote

import java.awt.Color
import java.awt.Dimension
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.SwingUtilities

import org.apache.batik.swing.JSVGCanvas
import org.opennms.netmgt.config.monitoringLocations.LocationDef
import org.opennms.netmgt.model.ScanReport
import org.opennms.netmgt.model.ScanReportPollResult
import org.opennms.netmgt.poller.remote.PollerBackEnd
import org.opennms.netmgt.poller.remote.metadata.MetadataField
import org.opennms.netmgt.poller.remote.support.ScanReportPollerFrontEnd
import org.opennms.poller.remote.FrontEndInvoker
import org.opennms.poller.remote.MetadataUtils
import org.opennms.poller.remote.ScanReportHandler
import org.springframework.beans.factory.InitializingBean
import org.springframework.util.Assert

class ScanGui extends AbstractGui implements ScanReportHandler, PropertyChangeListener, InitializingBean {
    def m_metadataFieldTypes = new TreeSet<MetadataField>();
    def m_locations = new ArrayList<String>()
    def m_applications = new HashMap<Set<String>>()
    def m_geoMetadata
    def m_scanReport

    def m_backEnd
    def m_frontEnd

    def m_metadataFields = new HashMap<String, JTextField>()
    def m_progressPanel
    def m_progressBar
    def m_passFailPanel
    def m_updateDetails
    def m_detailsPanel

    public ScanGui() {
        super()
    }

    public void setPollerBackEnd(final PollerBackEnd pollerBackEnd) {
        m_backEnd = pollerBackEnd
    }

    @Override
    protected String getApplicationTitle() {
        def title = m_backEnd.getScanReportTitle()
        if (title != null && !title.trim().isEmpty()) {
            return title;
        }
        return "Network Scanner"
    }

    /**
     * This method is injected by Spring by using a <lookup-method>.
     *
     * @see applicationContext-scan-gui.xml
     */
    protected ScanReportPollerFrontEnd createPollerFrontEnd() {
    }

    protected void updateImage() {
        final URL imageUrl = m_backEnd.getScanReportImage()
        if (imageUrl != null) {
            def image = MetadataUtils.getImageFromURL(imageUrl)
            if (image != null) {
                super.setLogoComponent(image)
            }
        }
    }

    public void afterPropertiesSet() {
        Assert.notNull(m_backEnd)
        Collection<LocationDef> monitoringLocations = m_backEnd.getMonitoringLocations()
        for (final LocationDef d : monitoringLocations) {
            def name = d.getLocationName()
            m_locations.add(name)
            def apps = m_backEnd.getApplicationsForLocation(name)
            System.err.println("location=" + name + ", applications=" + apps)
            m_applications.put(name, apps)
        }
        m_metadataFieldTypes = m_backEnd.getMetadataFields()
        m_geoMetadata = MetadataUtils.fetchGeodata()
        updateImage()
        createAndShowGui()
    }

    public String validateFields() {
        for (final MetadataField fieldType : m_metadataFieldTypes) {
            final JTextField field = m_metadataFields.get(fieldType.getKey())
            if (field == null) {
                System.err.println("WARNING: missing field control for " + fieldType.getKey())
            } else {
                def fieldText = field.getText()
                if (fieldType.isRequired()) {
                    if (fieldText == null || fieldText.trim().isEmpty()) {
                        return fieldType.description + " is required!"
                    }
                }
                if (fieldType.validator != null) {
                    def isValid = fieldType.validator.isValid(fieldText)
                    System.err.println("validator=" + fieldType.validator.getClass().getName() + ", text=" + fieldText + ", valid=" + isValid)
                    if (!isValid) {
                        return fieldType.description + " is invalid!"
                    }
                }
            }
        }
        return null
    }

    protected String getFieldKey(final String name) {
        if (name == null) {
            return null
        }
        return name.replace(" ", "-").toLowerCase()
    }

    @Override
    public JPanel getMainPanel() {
        def errorLabel

        def updateValidation = {
            def errorMessage = validateFields()
            if (errorLabel != null) {
                if (errorMessage == null) {
                    errorLabel.setText("")
                    errorLabel.setVisible(false)
                    return false
                } else {
                    errorLabel.setText(errorMessage)
                    errorLabel.setVisible(true)
                    return true
                }
            } else {
                return true
            }
        }

        getGui().setMinimumSize(new Dimension(750, 550))
        getGui().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

        return swing.panel(background:getBackgroundColor(), opaque:true, constraints:"grow") {
            migLayout(
                    layoutConstraints:"fill" + debugString,
                    columnConstraints:"[right,grow][left,grow]",
                    rowConstraints:"[grow]"
                    )

            m_progressPanel = panel(constraints:"top", opaque:false) {
                migLayout(
                        layoutConstraints:"fill" + debugString,
                        columnConstraints:"[right,grow][left][left]",
                        rowConstraints:"[grow]"
                        )

                def resetProgressBar = {
                    if (m_progressBar != null) {
                        m_progressBar.setValue(0)
                        m_progressBar.setString("0%")
                        m_progressBar.setVisible(false)
                    }
                    if (m_passFailPanel != null) {
                        m_passFailPanel.removeAll()
                        m_passFailPanel.updateUI()
                    }
                }

                def currentLocation = m_locations.get(0)
                def currentApplications = m_applications.get(currentLocation)
                def applicationLabel
                def applicationCombo
                def locationCombo
                def updateApplicationCombo = {
                    if (applicationCombo != null && locationCombo != null) {
                        def model = applicationCombo.getModel()
                        currentLocation = locationCombo.getSelectedItem()
                        currentApplications = m_applications.get(currentLocation)
                        System.err.println("Updating application combo. currentLocation=" + currentLocation + ", currentApplications=" + currentApplications);
                        model.removeAllElements()
                        if (currentApplications == null || currentApplications.size() < 1) {
                            System.err.println("Location combo changed, but no applications found!");
                            applicationLabel.setVisible(false);
                            applicationCombo.setVisible(false);
                        } else {
                            for (final String app : currentApplications) {
                                model.addElement(app);
                            }
                            applicationLabel.setVisible(true);
                            applicationCombo.setVisible(true);
                        }
                    }
                    applicationCombo.updateUI()
                }

                label(text:"Location:", font:getLabelFont())
                locationCombo = comboBox(items:m_locations, toolTipText:"Choose your location.", foreground:getForegroundColor(), background:getBackgroundColor(), renderer:getRenderer(), actionPerformed:{
                    updateApplicationCombo()
                    resetProgressBar()
                })
                button(text:'Go', font:getLabelFont(), foreground:getBackgroundColor(), background:getDetailColor(), opaque:true, constraints:"top, spany 2, wrap", actionPerformed:{
                    if (updateValidation()) {
                        return
                    }
                    m_scanResult = null
                    m_progressBar.setValue(0)
                    m_progressBar.setStringPainted(true)
                    m_progressBar.setString("0%")
                    m_progressBar.setVisible(true)
                    m_progressBar.updateUI()

                    if (m_updateDetails != null) {
                        m_updateDetails()
                    }

                    m_frontEnd = createPollerFrontEnd()

                    final Map<String,String> metadata = new HashMap<>(m_geoMetadata)
                    for (final Map.Entry<String,JTextField> field : m_metadataFields) {
                        metadata.put(field.getKey(), field.getValue().getText())
                    }
                    m_frontEnd.setMetadata(metadata)
                    m_frontEnd.setSelectedApplications(Collections.singleton(applicationCombo.getSelectedItem()))

                    final FrontEndInvoker invoker = new FrontEndInvoker(m_frontEnd, this, currentLocation)
                    invoker.addPropertyChangeListener(this)
                    invoker.execute()
                })

                applicationLabel = label(text:"Application:", font:getLabelFont(), visible:false)
                applicationCombo = comboBox(toolTipText:"Choose your application.", foreground:getForegroundColor(), background:getBackgroundColor(), renderer:getRenderer(), constraints:"wrap", visible: false, actionPerformed:{
                    resetProgressBar()
                })
                updateApplicationCombo()

                m_progressBar = progressBar(borderPainted:false, visible:false, value:0, constraints:"grow, spanx 3, wrap")

                m_passFailPanel = panel(background:getBackgroundColor(), constraints:"center, spanx 3, spany 2, height 200!, grow, wrap") {
                    migLayout(
                            layoutConstraints:"fill" + debugString,
                            columnConstraints:"[center grow,fill]",
                            rowConstraints:"[center grow,fill]"
                            )

                }
            }
            panel(constraints:"top", opaque:false) {
                migLayout(
                        layoutConstraints:"fill" + debugString,
                        columnConstraints:"[right][left grow,fill, 200::]",
                        rowConstraints:""
                        )

                for (def fieldType : m_metadataFieldTypes) {
                    label(text:fieldType.description, font:getLabelFont(), constraints:"")
                    def textField = textField(columns:25, constraints:"wrap", actionPerformed:updateValidation, focusGained:updateValidation, focusLost:updateValidation)
                    m_metadataFields.put(fieldType.key, textField)
                }
                /*
                 for (def field : m_metadataFieldNames) {
                 final String key = getFieldKey(field)
                 label(text:field, font:getLabelFont(), constraints:"")
                 def textField = textField(toolTipText:"Enter your " + field.toLowerCase() + ".", columns:25, constraints:"wrap", actionPerformed:updateValidation, focusGained:updateValidation, focusLost:updateValidation)
                 m_metadataFields.put(key, textField)
                 }
                 */

                errorLabel = label(text:"", visible:false, foreground:Color.RED, constraints:"grow, skip 1, wrap")
            }

            def detailsOpen = false
            def pendingResize = true
            def detailsButton
            def detailsParent

            m_updateDetails = {
                if (detailsButton == null || detailsParent == null) {
                    return
                }
                if (detailsParent != null && m_detailsPanel != null) {
                    detailsParent.remove(m_detailsPanel)
                }

                if (m_scanReport == null) {
                    detailsButton.setVisible(false)
                    return
                }

                detailsButton.setVisible(true)

                if (detailsOpen) {
                    detailsButton.setText("Details \u25BC")
                    m_detailsPanel = panel(opaque:false) {
                        migLayout(
                                layoutConstraints:"fill" + debugString,
                                columnConstraints:"[center grow,fill]",
                                rowConstraints:""
                                )

                        def results = m_scanReport.getPollResults()

                        def tab = table(constraints:"grow, wrap", gridColor:getDetailColor()) {
                            tableModel( list : results) {
                                propertyColumn(header:"Service", propertyName:"serviceName", editable:false)
                                propertyColumn(header:"Node", propertyName:"nodeLabel", editable:false)
                                propertyColumn(header:"IP Address", propertyName:"ipAddress", editable:false)
                                propertyColumn(header:"Result", propertyName:"result", editable:false)
                            }
                        }
                        widget(constraints:"dock north, grow, wrap", tab.tableHeader)
                    }
                    m_detailsPanel.setVisible(true)
                    detailsParent.add(m_detailsPanel)
                } else {
                    detailsButton.setText("Details \u25B2")
                    if (detailsParent != null && m_detailsPanel != null) {
                        detailsParent.remove(m_detailsPanel)
                        m_detailsPanel = null
                    }
                }

                pendingResize = true

                repaint()
            }

            detailsParent = panel(constraints:"bottom, center, spanx 2, shrinky 1000, dock south", opaque:false) {
                migLayout(
                        layoutConstraints:"fill" + debugString,
                        columnConstraints:"[center grow, fill]",
                        rowConstraints:"0[]5[]0"
                        )

                detailsButton = button(text:"Details \u25BC", font:getLabelFont(), foreground:getDetailColor(), background:getBackgroundColor(), opaque:false, border:null, constraints:"wrap", actionPerformed:{
                    detailsOpen = !detailsOpen
                    if (m_updateDetails != null) {
                        m_updateDetails()
                    }
                })
            }

            def lastDimension = detailsParent.getSize()
            detailsParent.addComponentListener(new ComponentAdapter() {
                        public void componentResized(final ComponentEvent e) {
                            if (!pendingResize) {
                                return
                            }
                            def newDimension = e.getComponent().getSize()
                            def difference = Math.abs(newDimension.height - lastDimension.height)
                            def guiSize = getWindowSize()
                            def newSize
                            if (newDimension.height > lastDimension.height) {
                                newSize = new Dimension(Double.valueOf(guiSize.width).intValue(), Double.valueOf(guiSize.height + difference).intValue())
                            } else {
                                newSize = new Dimension(Double.valueOf(guiSize.width).intValue(), Double.valueOf(guiSize.height - difference).intValue())
                            }
                            lastDimension = newDimension
                            pendingResize = false

                            if (m_detailsPanel != null) {
                                m_detailsPanel.setVisible(true)
                            }
                            setWindowSize(newSize)
                        }
                    })

            if (m_updateDetails != null) {
                m_updateDetails()
            }

        }
    }

    public void setProgress(final Integer progress) {
    }

    public void scanComplete(final ScanReport report) {
        System.err.println("scanComplete()")
        m_scanReport = report

        boolean passed = true

        if (report == null) {
            System.err.println("Something went wrong.  ScanReport is null!")
            passed = false
        } else {
            for (final ScanReportPollResult result : report.getPollResults()) {
                if (!result.getPollStatus().isUp()) {
                    passed = false
                    break
                }
            }
        }

        m_passFailPanel.removeAll()
        def svg = new JSVGCanvas()
        def uri = this.getClass().getResource(passed? "/passed.svg":"/failed.svg")
        svg.setURI(uri.toString())
        m_passFailPanel.add(svg)
        m_passFailPanel.revalidate()

        if (m_updateDetails == null) {
            System.err.println("Can't update details!")
        } else {
            m_updateDetails()
        }
        m_backEnd.reportSingleScan(report)
    }

    public static void main(String[] args) {
        def g = new ScanGui()
        SwingUtilities.invokeAndWait({
            g.createAndShowGui()
        })
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if ("progress".equals(evt.getPropertyName())) {
            def value = (Integer)evt.getNewValue()
            System.err.println("progress: " + value)
            m_progressBar.setValue(value)
            m_progressBar.setString(value + "%")
        }
    }
}
