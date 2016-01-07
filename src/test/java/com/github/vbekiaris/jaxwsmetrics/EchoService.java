package com.github.vbekiaris.jaxwsmetrics;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.soap.SOAPException;

/**
 * Echo web service interface, used for testing {@link MetricsHandler}.
 */
@WebService
public interface EchoService
{
    @WebMethod(operationName = "echo")
    String echo(@WebParam(name = "input") @XmlElement(required = true, nillable = false) String input);

    @WebMethod(operationName = "echoWithFault")
    String echoWithFault(@WebParam(name = "input") String input) throws SOAPException;

}
