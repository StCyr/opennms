/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.snmp;

import org.opennms.netmgt.config.api.SnmpAgentConfigFactory;
import org.opennms.netmgt.provision.support.SyncAbstractDetector;
import org.opennms.netmgt.snmp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.util.Map;
import java.util.regex.Pattern;

@Component
/**
 * <p>SnmpDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Scope("prototype")
public class SnmpDetector extends SyncAbstractDetector implements InitializingBean {
    
    /** Constant <code>DEFAULT_SERVICE_NAME="SNMP"</code> */
    protected static final String DEFAULT_SERVICE_NAME = "SNMP";

    /**
     * The system object identifier to retrieve from the remote agent.
     */
    private static final String DEFAULT_OID = ".1.3.6.1.2.1.1.2.0";
    
    //These are -1 so by default we use the AgentConfig
    private static final int DEFAULT_PORT = -1;
    private static final int DEFAULT_TIMEOUT = -1;
    private static final int DEFAULT_RETRIES = -1;
    
    private String m_oid = DEFAULT_OID;
    private boolean m_isTable = false;
    private boolean m_hex = false;

    private String m_forceVersion;
    private String m_vbvalue;

    private SnmpAgentConfigFactory m_agentConfigFactory;

    private static final Logger LOG = LoggerFactory.getLogger(SnmpDetector.class);
    
    /**
     * <p>Constructor for SnmpDetector.</p>
     */
    public SnmpDetector() {
        super(DEFAULT_SERVICE_NAME, DEFAULT_PORT, DEFAULT_TIMEOUT, DEFAULT_RETRIES);
    }

    /**
     * Constructor for creating a non-default service based on this protocol
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    public SnmpDetector(String serviceName, int port) {
        super(serviceName, port, DEFAULT_TIMEOUT, DEFAULT_RETRIES);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_agentConfigFactory);
    }

    /**
     * <p>setIsTable</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIsTable(String table) {
        if (this.m_isTable) {
            return new String("true");
        } else {
            return new String("false");
        }
    }

    /**
     * <p>getIsTable</p>
     *
     * @param table a {@link java.lang.String} object.
     */
    public void setIsTable(String table) {
        if ("true".equalsIgnoreCase(table)) {
            this.m_isTable = true;
        } else {
            this.m_isTable = false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isServiceDetected(InetAddress address) {
        try {

            SnmpAgentConfig agentConfig = getAgentConfigFactory().getAgentConfig(address);
            String expectedValue = null;
            
            configureAgentPTR(agentConfig);
            
            configureAgentVersion(agentConfig);
            
            if (getVbvalue() != null) {
                expectedValue = getVbvalue();
            }

            if (this.m_isTable) {
                LOG.debug(getServiceName() + ": Table detect enabled");
                SnmpObjId snmpObjId = SnmpObjId.get(getOid());

                Map<SnmpInstId, SnmpValue> table = SnmpUtils.getOidValues(agentConfig, DEFAULT_SERVICE_NAME, snmpObjId);
                for (Map.Entry<SnmpInstId, SnmpValue> e : table.entrySet()) {
                    String retrievedValue;
                    if (m_hex) {
                        retrievedValue = e.getValue().toHexString();
                    } else {
                        retrievedValue = e.getValue().toString();
                    }
                    LOG.debug(getServiceName() + ": retrieved value [" + retrievedValue + "]");

                    if (retrievedValue != null && expectedValue != null &&
                            Pattern.compile(expectedValue).matcher(retrievedValue).matches()) {
                        LOG.debug(getServiceName() + ": expected value matched");
                        return true;
                    } else if (retrievedValue != null) {
                        return true;
                    }
                }
                return false;
            } else {
                String retrievedValue = getValue(agentConfig, getOid());

                if (retrievedValue != null && expectedValue != null) {
                    return (Pattern.compile(expectedValue).matcher(retrievedValue).matches());
                } else {
                    return (retrievedValue != null);
                }
            }
        } catch (Throwable t) {
            throw new UndeclaredThrowableException(t);
        }
    }

    /**
     * <p>configureAgentVersion</p>
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     */
    protected void configureAgentVersion(SnmpAgentConfig agentConfig) {
        if (getForceVersion() != null) {
            String version = getForceVersion();
            
            // TODO: Deprecate the snmpv1, snmpv2, snmpv2c, snmpv3 params in favor of more-used v1, v2c, and v3
            // @see http://issues.opennms.org/browse/NMS-7518
            if ("v1".equalsIgnoreCase(version) || "snmpv1".equalsIgnoreCase(version)) {
                agentConfig.setVersion(SnmpAgentConfig.VERSION1);
            } else if ("v2".equalsIgnoreCase(version) || "v2c".equalsIgnoreCase(version) || "snmpv2".equalsIgnoreCase(version) || "snmpv2c".equalsIgnoreCase(version)) {
                agentConfig.setVersion(SnmpAgentConfig.VERSION2C);
            } else if ("v3".equalsIgnoreCase(version) || "snmpv3".equalsIgnoreCase(version)) {
                agentConfig.setVersion(SnmpAgentConfig.VERSION3);
            }
        }
    }

    /**
     * <p>configureAgentPTR</p>
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     */
    protected void configureAgentPTR(SnmpAgentConfig agentConfig) {
        if (getPort() > 0) {
            agentConfig.setPort(getPort());
        }
        
        if (getTimeout() > 0) {
            agentConfig.setTimeout(getTimeout());
        }
        
        if (getRetries() > -1) {
            agentConfig.setRetries(getRetries());
        }
    }
    
    /**
     * <p>getValue</p>
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     * @param oid a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    protected String getValue(SnmpAgentConfig agentConfig, String oid) {
        SnmpValue val = SnmpUtils.get(agentConfig, SnmpObjId.get(oid));
        if (val == null || val.isNull() || val.isEndOfMib() || val.isError()) {
            return null;
        }
        else {
            return val.toString();
        }
        
    }

    /**
     * <p>setOid</p>
     *
     * @param oid a {@link java.lang.String} object.
     */
    public void setOid(String oid) {
        m_oid = oid;
    }

    /**
     * <p>getOid</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getOid() {
        return m_oid;
    }

    /**
     * <p>setForceVersion</p>
     *
     * @param forceVersion a {@link java.lang.String} object.
     */
    public void setForceVersion(String forceVersion) {
        m_forceVersion = forceVersion;
    }

    /**
     * <p>getForceVersion</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getForceVersion() {
        return m_forceVersion;
    }

    /**
     * <p>setVbvalue</p>
     *
     * @param vbvalue a {@link java.lang.String} object.
     */
    public void setVbvalue(String vbvalue) {
        m_vbvalue = vbvalue;
    }

    /**
     * <p>getVbvalue</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getVbvalue() {
        return m_vbvalue;
    }
    
    /**
     * <p>setAgentConfigFactory</p>
     *
     * @param agentConfigFactory a {@link org.opennms.netmgt.config.api.SnmpAgentConfigFactory} object.
     */
    @Autowired
    public void setAgentConfigFactory(SnmpAgentConfigFactory agentConfigFactory) {
        m_agentConfigFactory = agentConfigFactory;
    }
    
    /**
     * <p>getAgentConfigFactory</p>
     *
     * @return a {@link org.opennms.netmgt.config.api.SnmpAgentConfigFactory} object.
     */
    public SnmpAgentConfigFactory getAgentConfigFactory() {
        return m_agentConfigFactory;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.detector.AbstractDetector#onInit()
     */
    /** {@inheritDoc} */
    @Override
    protected void onInit() {
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
    }
}
