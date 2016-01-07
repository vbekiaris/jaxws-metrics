package com.github.vbekiaris.jaxwsmetrics;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.soap.SOAPFaultException;

/**
 * Echo web service implementation, used for testing {@link MetricsHandler}.
 */
@WebService(name = "echo",
            targetNamespace = "http://github.com/vbekiaris/jaxwsmetrics/test",
            serviceName = "echoService",
            portName = "echoPort",
            endpointInterface = "com.github.vbekiaris.jaxwsmetrics.EchoService")
public class EchoServiceImpl implements EchoService
{
    public String echo(String input) {
        return input;
    }

    public String echoWithFault(String input) throws SOAPException
    {
        SOAPMessage msg = MessageFactory.newInstance().createMessage();
        msg.getSOAPBody().addFault(new QName("http://github.com/vbekiaris/jaxwsmetrics/test", "fault"), "fault-test");
        SOAPFault fault = msg.getSOAPBody().getFault();
        throw new SOAPFaultException(fault);
    }
}
