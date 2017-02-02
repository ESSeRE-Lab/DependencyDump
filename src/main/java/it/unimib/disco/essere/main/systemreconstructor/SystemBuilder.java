package it.unimib.disco.essere.main.systemreconstructor;

import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.classfile.JavaClass;

public abstract class SystemBuilder {
    private List<JavaClass> classes;
    private List<String> packages;

    public List<JavaClass> getClasses() {
        return classes;
    }

    public List<String> getPackages() {
        return packages;
    }

    protected SystemBuilder() {
        classes = new ArrayList<>();
        packages = new ArrayList<>();
    }

    /**
     * Reads all classes in the provided classpath and returns the list of
     * classes and the list of packages of the analyzed system
     */
    public abstract void readClass();

}
