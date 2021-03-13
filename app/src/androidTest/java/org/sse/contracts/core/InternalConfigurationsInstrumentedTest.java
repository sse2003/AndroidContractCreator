package org.sse.contracts.core;

import android.support.test.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.sse.contracts.Utils;
import org.sse.contracts.core.conf.InternalConfigurations;

import static org.junit.Assert.assertEquals;

/**
 * Created by Administrator on 08.08.2016.
 */

public class InternalConfigurationsInstrumentedTest
{
    @Before
    public void setUp() throws Exception
    {
        Utils.setWorkingContext(InstrumentationRegistry.getTargetContext());
    }

    @Test
    public void testGetApplicationFirstStartTime() throws Exception
    {
        long time1 = InternalConfigurations.getInstance().getApplicationFirstStartTime();
        Thread.sleep(250);
        assertEquals(time1, InternalConfigurations.getInstance().getApplicationFirstStartTime());
    }

}
