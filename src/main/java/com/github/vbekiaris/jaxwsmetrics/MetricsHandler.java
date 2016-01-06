package com.github.vbekiaris.jaxwsmetrics;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

/**
 * JAX-WS metrics handler maintains duration & throughput information per operation.
 *
 * The generated metrics are named after the qualified name of the WSDL operation; when
 * processing faults, the operation name is followed by the "-fault" qualifier to separate
 * fault from normal message processing metrics.
 *
 * This class depends on obtaining a MetricRegistry instance; this can be supplied either
 * via  {@link #MetricsHandler(MetricRegistry)} constructor or using the setter method
 * {@link #setMetricRegistry(MetricRegistry)}.
 */
public class MetricsHandler implements LogicalHandler<LogicalMessageContext>
{
    private static final String INVALID_OPERATION_NAME = "OPERATION_NOT_AVAILABLE";
    private static final String TIMER_CONTEXT_PROPERTY = "METRICS_TIMER_CONTEXT";

    private MetricRegistry metricRegistry;

    public MetricsHandler()
    {
    }

    public MetricsHandler(MetricRegistry metricRegistry)
    {
        this.metricRegistry = metricRegistry;
    }

    public void close(MessageContext context)
    {
    }

    public boolean handleMessage(LogicalMessageContext context)
    {
        return handleAnything(context, null);
    }

    public boolean handleFault(LogicalMessageContext context)
    {
        return handleAnything(context, "fault");
    }

    private boolean handleAnything(LogicalMessageContext context, String qualifier) {
        String metricName;
        QName operationQName = (javax.xml.namespace.QName) context.get(MessageContext.WSDL_OPERATION);
        if (operationQName != null) {
            metricName = operationQName.toString();
        }
        else {
            metricName = INVALID_OPERATION_NAME;
        }

        if (qualifier != null && !qualifier.isEmpty()) {
            metricName = metricName + "-" + qualifier;
        }

        Boolean outboundProperty = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        Timer.Context timerContext = (Timer.Context) context.get(TIMER_CONTEXT_PROPERTY);

        if (outboundProperty && timerContext != null) {
            timerContext.stop();
        }
        else {
            timerContext = metricRegistry.timer(metricName).time();
            context.put(TIMER_CONTEXT_PROPERTY, timerContext);
            context.setScope(TIMER_CONTEXT_PROPERTY, MessageContext.Scope.HANDLER);
        }
        return true;
    }

    public void setMetricRegistry(MetricRegistry metricRegistry)
    {
        this.metricRegistry = metricRegistry;
    }
}
