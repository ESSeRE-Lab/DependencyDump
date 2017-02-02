# A simple Java dependency dumper

Instructions:

1. compile: `mvn clean dependency:copy-dependencies package`
2. run: `java -cp "target/DependencyDump-0.0.1.jar:target/dependency/*" it.unimib.disco.essere.main.dump.DirectDependencyDumper -projectFolder /project/to/analyze -output dependencies.csv`

> Note: the program analyzes java binaries, and collects all `.class` files found in the specified folder. 