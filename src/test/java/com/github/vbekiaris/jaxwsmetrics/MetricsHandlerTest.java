package com.github.vbekiaris.jaxwsmetrics;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.SOAPFaultException;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 *
 */
public class MetricsHandlerTest
{

    private String address;
    private URL wsdlURL;
    private Endpoint endpoint;
    private MetricRegistry metricRegistry;

    @org.testng.annotations.BeforeMethod
    public void setUp() throws Exception {
        // setup web service endpoint
        address = "http://localhost:9999/services/echo";
        wsdlURL = new URL(address + "?wsdl");
        endpoint = Endpoint.create(new EchoServiceImpl());
        // initialize metrics registry & setup handler
        metricRegistry = new MetricRegistry();
        MetricsHandler metricsHandler = new MetricsHandler(metricRegistry);
        List<Handler> handlers = new ArrayList<Handler>();
        handlers.add(metricsHandler);
        endpoint.getBinding().setHandlerChain(handlers);
        endpoint.publish(address);
    }

    @org.testng.annotations.AfterMethod
    public void tearDown() throws Exception {
        endpoint.stop();
    }

    @org.testng.annotations.Test
    public void testHandleMessage() throws Exception {
        sendSoapMessage("echoservice-soap-message.xml");
        SortedMap<String, Timer> timers = metricRegistry.getTimers();
        assertTrue(timers != null && timers.containsKey("{http://jaxwsmetrics.vbekiaris.github.com/}echo"));
    }

    @org.testng.annotations.Test
    public void testHandleFault() throws Exception {
        try
        {
            sendSoapMessage("echoservice-soap-message-fault.xml");
        }
        catch (SOAPFaultException e) {
            // expected exception
        }
        SortedMap<String, Timer> timers = metricRegistry.getTimers();
        assertTrue(timers != null && timers.containsKey("{http://jaxwsmetrics.vbekiaris.github.com/}echoWithFault"));
    }

    @org.testng.annotations.Test
    public void testInvalidOperation() throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost post = new HttpPost(address);
        post.setHeader("Content-type", "text/xml");
        InputStream is = getClass().getClassLoader().getResourceAsStream("echoservice-soap-message-invalid-op.xml");
        post.setEntity(new InputStreamEntity(is));

        CloseableHttpResponse response = httpClient.execute(post);

        SortedMap<String, Timer> timers = metricRegistry.getTimers();
        assertEquals(response.getStatusLine().getStatusCode(), 500);
        assertTrue(timers != null && timers.containsKey("OPERATION_NOT_AVAILABLE"));

        response.close();
        httpClient.close();
    }

    private SOAPMessage sendSoapMessage(String resourcePath) throws SOAPException, IOException {
        QName serviceName = new QName("http://github.com/vbekiaris/jaxwsmetrics/test", "echoService");
        QName portName = new QName("http://github.com/vbekiaris/jaxwsmetrics/test", "echoPort");
        Service jaxwsService = Service.create(wsdlURL, serviceName);
        Dispatch<SOAPMessage> disp = jaxwsService.createDispatch(portName,
                                                                 SOAPMessage.class,
                                                                 Service.Mode.MESSAGE);
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
        SOAPMessage reqMsg = MessageFactory.newInstance().createMessage(null, is);
        assertNotNull(reqMsg);
        SOAPMessage response = disp.invoke(reqMsg);
        return response;
    }
}
