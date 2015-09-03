package org.opennms.netmgt.discovery;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.opennms.netmgt.config.discovery.Specific;
import org.opennms.netmgt.discovery.actors.Discoverer;
import org.opennms.netmgt.discovery.actors.EventWriter;
import org.opennms.netmgt.discovery.actors.RangeChunker;
import org.opennms.netmgt.icmp.NullPinger;
import org.springframework.test.context.ContextConfiguration;

@RunWith( OpenNMSJUnit4ClassRunner.class )
@ContextConfiguration( locations = { "classpath:/META-INF/opennms/emptyContext.xml" } )
public class DiscoveryRoutingTest extends CamelTestSupport
{

    static public class Discoverer
    {
        public DiscoveryResults discover( DiscoveryJob job )
        {
            return new DiscoveryResults( Maps.newHashMap(), job.getForeignSource(), job.getLocation() );
        }

    }

    static public class EventWriter
    {
        private static final Logger LOG = LoggerFactory.getLogger( EventWriter.class );

        public void sendEvents( DiscoveryResults results )
        {
            results.getResponses().entrySet().forEach(
                            e -> sendNewSuspectEvent( e.getKey(), e.getValue(), results.getForeignSource() ) );
        }

        private void sendNewSuspectEvent( InetAddress address, EchoPacket response, String foreignSource )
        {
            EventBuilder eb = new EventBuilder( EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, "OpenNMS.Discovery" );
            eb.setInterface( address );
            eb.setHost( InetAddressUtils.getLocalHostName() );

            eb.addParam( "RTT", response.getReceivedTimeNanos() - response.getSentTimeNanos() );

            if ( foreignSource != null )
            {
                eb.addParam( "foreignSource", foreignSource );
            }

            try
            {
                EventIpcManagerFactory.getIpcManager().sendNow( eb.getEvent() );
                LOG.debug( "Sent event: {}", EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI );
            }
            catch ( Throwable t )
            {
                LOG.warn( "run: unexpected throwable exception caught during send to middleware", t );
            }
        }
    }

    @Override
    protected JndiRegistry createRegistry() throws Exception
    {
        JndiRegistry registry = super.createRegistry();

        registry.bind( "rangeChunker", new RangeChunker() );
        registry.bind( "discoverer", new Discoverer() );
        registry.bind( "eventWriter", new EventWriter() );

        return registry;
    }

    /**
     * Delay calling context.start() so that you can attach an {@link AdviceWithRouteBuilder} to the
     * context before it starts.
     */
    @Override
    public boolean isUseAdviceWith()
    {
        return true;
    }

    /**
     * Build the route for all of the config parsing messages.
     */
    @Override
    protected RouteBuilder createRouteBuilder() throws Exception
    {
        return new RouteBuilder() {

            @Override
            public void configure() throws Exception
            {
                // Add exception handlers
                onException( IOException.class ).handled( true ).logStackTrace( true ).stop();

                from( "direct:createDiscoveryJobs" ).to( "bean:rangeChunker" ).split( body() ).recipientList(
                                simple( "seda:Location-${body.location}" ) );
                from( "seda:Location-LOC1" ).to( "bean:discoverer" ).to( "bean:eventWriter" );
            }
        };
    }

    @Test
    public void testDiscover() throws Exception
    {
        for ( RouteDefinition route : new ArrayList<RouteDefinition>( context.getRouteDefinitions() ) )
        {
            route.adviceWith( context, new AdviceWithRouteBuilder() {
                @Override
                public void configure() throws Exception
                {
                    mockEndpoints();
                }
            } );
        }
        context.start();

        // We should get 1 call to the scheduler endpoint
        MockEndpoint endpoint = getMockEndpoint( "mock:bean:eventWriter", false );
        endpoint.setExpectedMessageCount( 1 );

        // Create the config aka job
        Specific specific = new Specific();
        specific.setContent( "4.2.2.2" );

        DiscoveryConfiguration config = new DiscoveryConfiguration();
        config.addSpecific( specific );
        config.setForeignSource( "Bogus FS" );
        config.setTimeout( 3000 );
        config.setRetries( 2 );
        config.setLocation( "LOC1" );

        // Execute the job
        template.requestBody( "direct:createDiscoveryJobs", config );

        assertMockEndpointsSatisfied();
    }
}
