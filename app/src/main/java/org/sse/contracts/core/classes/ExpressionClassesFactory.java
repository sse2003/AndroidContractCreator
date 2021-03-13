package org.sse.contracts.core.classes;


public class ExpressionClassesFactory {

    public static AbstractClass create(String className) {
        String packageName = ExpressionClassesFactory.class.getPackage().getName();
        String name = packageName + ".Class_" + className.toLowerCase();
        try {
            Class cl = Class.forName(name);
            return (AbstractClass) cl.newInstance();
        } catch (Exception e) {
            return null;
        }
    }
}
