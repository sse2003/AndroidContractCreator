package org.sse.contracts.core.classes;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ClassesFactoryTest
{
    @Test
    public void testLoadClasses() throws Exception
    {
        assertNull(ExpressionClassesFactory.create("нет такого класса"));
        assertNotNull(ExpressionClassesFactory.create("ФИО"));
        assertNotNull(ExpressionClassesFactory.create("фио"));
    }
}
