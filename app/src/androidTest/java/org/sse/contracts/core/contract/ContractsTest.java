package org.sse.contracts.core.contract;

import android.support.test.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.sse.contracts.Utils;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ContractsTest {
    @Before
    public void setUp() throws Exception {
        Utils.setWorkingContext(InstrumentationRegistry.getTargetContext());
    }

    @Test
    public void testLocalAssetsContract() throws Exception {
        LocalAssetContract c1 = new LocalAssetContract("1", "file.html");
        LocalAssetContract c2 = new LocalAssetContract("2", "file");
        LocalAssetContract c3 = new LocalAssetContract("1", "file2");


        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());

        assertNotEquals(c1, c3);
        assertNotEquals(c1.hashCode(), c3.hashCode());
    }

    @Test
    public void testLocalSavedContract() throws Exception {
        LocalSavedContract c1 = new LocalSavedContract("file.html");
        LocalSavedContract c2 = new LocalSavedContract("file");
        LocalSavedContract c3 = new LocalSavedContract("file2");


        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());

        assertNotEquals(c1, c3);
        assertNotEquals(c1.hashCode(), c3.hashCode());
    }

    @Test
    public void testRemoteContract() throws Exception {
        RemoteContract c1 = new RemoteContract("1", "file.html");
        RemoteContract c2 = new RemoteContract("2", "file");
        RemoteContract c3 = new RemoteContract("1", "file2");


        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());

        assertNotEquals(c1, c3);
        assertNotEquals(c1.hashCode(), c3.hashCode());
    }

    @Test
    public void testDifferenceContracts() {
        Set<AbstractContract> set = new HashSet<>();

        LocalAssetContract c1 = new LocalAssetContract("1", "file.html");
        LocalSavedContract c2 = new LocalSavedContract("file.html");
        LocalSavedContract c3 = new LocalSavedContract("file");

        set.add(c1);

        assertTrue(set.contains(c1));
        assertTrue(set.contains(c2));
        assertTrue(set.contains(c3));
    }

}