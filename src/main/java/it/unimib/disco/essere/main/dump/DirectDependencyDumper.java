package it.unimib.disco.essere.main.dump;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.io.CharSink;

import it.unimib.disco.essere.main.graphmanager.InstructionIdentifier;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilderByUrl;
import it.unimib.disco.essere.main.terminal.ExistFile;
import it.unimib.disco.essere.main.terminal.FileConverter;

public class DirectDependencyDumper {

	private static final Logger logger = LogManager.getLogger(DirectDependencyDumper.class);

	public static void main(String[] args) {
		DirectDependencyDumper dd = new DirectDependencyDumper();
		JCommander jc = new JCommander(dd, args);
		logger.info("Starting...");
		Stopwatch sw = Stopwatch.createStarted();
		dd.dump();
		logger.info("Done in " + sw);
	}

	@Parameter(names = { "-projectFolder",
			"-p" }, description = "Project folder (folder of class files)", converter = FileConverter.class, validateWith = ExistFile.class, required = true)
	File _projectFolder;

	@Parameter(names = { "-output",
			"-o" }, description = "Output file name. Use '-' for standard output.", converter = CharSinkConverter.class, required = true)
	CharSink output = null;

	@Parameter(names = "--help", help = true)
	private boolean _help;

	public void dump() {
		logger.info("Collecting classes...");
		SystemBuilderByUrl _sys = new SystemBuilderByUrl(_projectFolder.toString());
		Joiner j = Joiner.on(";");
		_sys.readClass();
		Stream<String> exts = _sys.getClasses().stream()
				.map(jcl -> j.join(jcl.getClassName(), "extends", jcl.getSuperclassName()));

		Stream<String> impls = _sys.getClasses().stream().flatMap(
				jcl -> Stream.of(jcl.getInterfaceNames()).map(in -> j.join(jcl.getClassName(), "implements", in)));

		Stream<String> uses = _sys.getClasses().stream().flatMap(jcl -> computeDependences(jcl).stream()
				.map(dep -> parseClassName(dep)[0]).map(dep -> j.join(jcl.getClassName(), "uses", dep)));
		List<String> lines = Stream.concat(Stream.concat(exts, impls), uses).collect(Collectors.toList());
		try {
			// Do not like this.
			// There must be a way to write a stream to file "in streaming"
			// without rewriting an util from scratch
			output.writeLines(lines);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Parse a class signature.
	 * 
	 * @param fullClassName
	 * @return an array containing the name of the class and its package.
	 */
	public static String[] parseClassName(String fullClassName) {
		String[] classInfo = new String[2];
		String className = "";
		String packageName = "";
		if (fullClassName.contains("/")) {
			int slashIndex = fullClassName.lastIndexOf('/');
			// logger.debug("nome classe " + fullClassName);
			packageName = "";
			if (slashIndex >= 0) {
				packageName = fullClassName.substring(1, slashIndex);
				packageName = packageName.replace('/', '.');
			} else {
				packageName = DEFAULT_PACKAGE;
			}

			className = fullClassName.substring(1, fullClassName.length() - 1);
			className = className.replace('/', '.');

		} else {
			packageName = getPackageName(fullClassName);
			className = fullClassName;
		}
		classInfo[0] = className;
		classInfo[1] = packageName;

		return classInfo;

	}

	public static final String DEFAULT_PACKAGE = "(default package)";

	/**
	 * @param fullClassName
	 * @return the name of the package of the given class.
	 */
	public static String getPackageName(String fullClassName) {
		String packageName = DEFAULT_PACKAGE;
		int pointIndex = fullClassName.lastIndexOf('.');
		if (pointIndex >= 0) {
			packageName = fullClassName.substring(0, pointIndex);
		}
		return packageName;
	}

	/**
	 * Compute the list of the class referenced by javaClass.
	 * 
	 * @param javaClass
	 * @return List of referenced classes names
	 */
	public static List<String> computeDependences(JavaClass javaClass) {
		List<String> dependences = new ArrayList<>();
		ConstantPool cp = javaClass.getConstantPool();
		ConstantPoolGen cpg = new ConstantPoolGen(cp);
		for (Method m : javaClass.getMethods()) {
			String className = javaClass.getClassName();
			MethodGen mg = new MethodGen(m, className, cpg);
			InstructionList instructions = mg.getInstructionList();
			if (instructions != null) {
				InstructionHandle[] ihs = instructions.getInstructionHandles();
				for (int i = 0; i < ihs.length; i++) {
					InstructionHandle ih = ihs[i];
					Instruction instruction = ih.getInstruction();
					dependences = InstructionIdentifier.identify(dependences, cpg, instruction);
				}
			}
		}

		dependences.addAll(InstructionIdentifier.findAnnotations(javaClass));
		return dependences;
	}

}
