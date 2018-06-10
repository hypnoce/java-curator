package io.opentracing.contrib.curator;

import com.google.common.truth.Truth;
import io.opentracing.Scope;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.util.GlobalTracer;
import io.opentracing.util.ThreadLocalScopeManager;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class OpentracingCuratorDriverTest {

  private static final MockTracer mockTracer = new MockTracer(new ThreadLocalScopeManager(),
      MockTracer.Propagator.TEXT_MAP);

  private TestingServer testingServer;

  @BeforeClass
  public static void init() {
    GlobalTracer.register(mockTracer);
  }

  @Before
  public void before() throws Exception {
    mockTracer.reset();
    testingServer = new TestingServer();
    testingServer.start();
  }

  @After
  public void after() throws IOException {
    testingServer.close();
  }

  @Test
  public void curator_spans_should_be_child_of_current_active_span() throws Exception {
    try (Scope activeSpan = mockTracer.buildSpan("parent").startActive(true);
         CuratorFramework client = CuratorFrameworkFactory.newClient(testingServer.getConnectString(), new RetryOneTime(1))) {
      client.start();
      client.getZookeeperClient().setTracerDriver(new OpentracingCuratorDriver());
      client.createContainers("/path");
      client.setData().forPath("/path", "data".getBytes(StandardCharsets.UTF_8));
    }
  }
}