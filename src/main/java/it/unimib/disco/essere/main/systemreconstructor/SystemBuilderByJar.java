package it.unimib.disco.essere.main.systemreconstructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SystemBuilderByJar extends SystemBuilder {
    private static final Logger logger = LogManager.getLogger(SystemBuilderByJar.class);

    private JarFile jarFile;

    public SystemBuilderByJar(String jarUrl) {
        super();
        try {
            this.jarFile = new JarFile(jarUrl);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void readClass() {
        ClassParser cParser = null;
        InputStream inputStream = null;

        Enumeration<JarEntry> jarEnum = jarFile.entries();
        
        
        while(jarEnum.hasMoreElements()) {
            JarEntry entry = jarEnum.nextElement();
            if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                try {
                    inputStream = jarFile.getInputStream(entry);
                    cParser = new ClassParser(inputStream, entry.getName());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (cParser != null) {
                    try {
                        JavaClass clazz = cParser.parse();
                        Repository.addClass(clazz);
                        this.getClasses().add(clazz);
                        this.getPackages().add(clazz.getPackageName());
                        

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }
}
