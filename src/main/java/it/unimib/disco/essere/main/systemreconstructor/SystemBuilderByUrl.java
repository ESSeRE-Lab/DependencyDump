package it.unimib.disco.essere.main.systemreconstructor;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SystemBuilderByUrl extends SystemBuilder {
    private static final Logger logger = LogManager.getLogger(SystemBuilderByUrl.class);

    private Path systemPath;

    public SystemBuilderByUrl(String url) {
        super();
        systemPath = Paths.get(url);
       
    }
    
    @Override
    public void readClass(){

        Stream<Path> stream;
        try {
            stream = Files.walk(this.systemPath);


        stream.forEach(filePath -> {
            if (Files.isRegularFile(filePath)) {
                try {
                   
                    if ("class".equals(FilenameUtils.getExtension(filePath.toString()))) {
                        // JavaClass clazz =
                        // Repository.lookupClass(FilenameUtils.removeExtension(filePath.toFile().getName()));
                        //ClassParser cParser = null;
                        //FileInputStream inputStream = null;
                        try (FileInputStream inputStream = new FileInputStream(filePath.toFile());){
                            
                            // System.out.println("creato input stream");
                        	ClassParser cParser = new ClassParser(inputStream, filePath.getFileName().toString());
                            
                            try {
                                JavaClass clazz = cParser.parse();                               
                                Repository.addClass(clazz);
                                this.getClasses().add(clazz);
                                this.getPackages().add(clazz.getPackageName());                               
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        stream.close();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }

    public Path getSystemPath() {
        return systemPath;
    }

}
