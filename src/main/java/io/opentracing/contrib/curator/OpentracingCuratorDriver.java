package io.opentracing.contrib.curator;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.apache.curator.drivers.AdvancedTracerDriver;
import org.apache.curator.drivers.EventTrace;
import org.apache.curator.drivers.OperationTrace;
import org.apache.zookeeper.KeeperException;

public class OpentracingCuratorDriver extends AdvancedTracerDriver {
  private final String peerService;

  public OpentracingCuratorDriver() {
    this("");
  }

  public OpentracingCuratorDriver(String peerService) {
    Objects.requireNonNull(peerService, "Expecting non null peerService");
    this.peerService = peerService;
  }

  @Override public void addTrace(OperationTrace trace) {
    final Tracer tracer = GlobalTracer.get();
    if (tracer == null) return;
    reportOperation(tracer, trace);
  }

  @Override public void addEvent(EventTrace trace) {
    System.out.println("Event :" + trace.getName());
  }

  private void reportOperation(Tracer tracer, OperationTrace operationTrace) {
    final long timeMicro = TimeUnit.MICROSECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS);
    final long latencyMicro = TimeUnit.MICROSECONDS.convert(operationTrace.getLatencyMs(), TimeUnit.MILLISECONDS);
    final long startMicro = timeMicro - latencyMicro;

    final Span activeSpan = tracer.activeSpan();

    final Tracer.SpanBuilder spanBuilder = tracer
        .buildSpan(operationTrace.getName())
        .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
        .withStartTimestamp(startMicro);
    if (activeSpan != null) {
      spanBuilder.asChildOf(activeSpan);
    }
    final Span span = spanBuilder.start();
    decorate(span, operationTrace);
    span.finish(startMicro + latencyMicro);
  }

  private void decorate(Span span, OperationTrace operationTrace) {
    final KeeperException.Code code = KeeperException.Code.get(operationTrace.getReturnCode());
    Tags.COMPONENT.set(span, "java-curator");
    span.setTag("session.id", operationTrace.getSessionId());
    if(operationTrace.getPath() != null && !operationTrace.getPath().isEmpty()) {
      span.setTag("path", operationTrace.getPath());
    }
    span.setTag("curator.return.code", code.toString());
    if(code != KeeperException.Code.OK) {
      Tags.ERROR.set(span, true);
    } else {
      Tags.ERROR.set(span, false);
    }

    if(!peerService.isEmpty()) {
      Tags.PEER_SERVICE.set(span, peerService);
    }
  }
}
