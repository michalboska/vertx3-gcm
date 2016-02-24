package com.github.michalboska.vertx3.gcm.internal;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Michal Boska
 **/
@RunWith(VertxUnitRunner.class)
public class GcmServiceImplTest {

    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Test(timeout = 5000)
    public void testFoo(TestContext context) {
        Vertx vertx = rule.vertx();
    }

}
