package idx3d.pipeline;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.tools.*;

/**
 * A class for online compilation. Source:
 * https://blog.frankel.ch/compilation-java-code-on-the-fly/
 * 
 * @author aless
 *
 */
public class EvilExecutor {

	private static final String UTF_8 = "UTF-8";

	/**
	 * Reads the source code from an arbitrary file on the file system, and returns
	 * it as a string. An alternative implementation would get the source from
	 * across the network.
	 * 
	 * @param sourcePath
	 * @return
	 * @throws IOException
	 */
	private String readCodeAbroad(String sourcePath) throws IOException {
		//InputStream stream = new FileInputStream(sourcePath);
		InputStream stream = new URL(sourcePath).openStream();		
		String separator = System.getProperty("line.separator");
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
			return reader.lines().collect(Collectors.joining(separator));
		}
	}

	/**
	 * Get name of classes in baseClass class. 
	 * @param baseClass - a Class
	 * @return List of classes within
	 */
	public static List<String> getClassNames(Class<?> baseClass) {
	    List<String> classNames = new ArrayList<String>();
	    classNames.add(baseClass.getSimpleName());
	    for (Class<?> subclass: baseClass.getClasses()) {
	        classNames.addAll(getClassNames(subclass));
	    }
	    return classNames;
	}
	
	
	/**
	 * Saves the source code file  in a read-enabled directory. <br>
	 * The file name is the className parameter.
	 * create a file named according to the class name it contains.
	 * 
	 * @param source - the source code
	 * @param tempDir - where to write files. Can be null.
	 * @param className - the name of the class. Can be null, but not advised.
	 * @return the Path were the source was saved.
	 * @throws IOException
	 */
	protected Path saveSource(String source, String tempDir, String className) throws IOException {
		//String tmpProperty = System.getProperty("java.io.tmpdir");
		if(tempDir==null) {
			tempDir = System.getProperty("java.io.tmpdir");
		}
		// Path sourcePath = Paths.get(tmpProperty, "Harmless.java");
		Path sourcePath = Paths.get(tempDir, className);
		source = stripComments(source);
		Files.write(sourcePath, source.getBytes(UTF_8));
		return sourcePath;
	}

	/**
	 * Remove all comments.
	 * @TODO implement it
	 * 
	 * @param source - The source code to clean up.
	 * @return
	 */
	protected String stripComments(String source) {
		return source;
	}
	
	
	/**
	 * Compiles the class file out of the java file.
	 * 
	 * @param path2classFolder - Path to folder where the class is.
	 * @param className - the name of compiled class
	 * 
	 * @return 
	 */
	private Path compileSource(Path path2classFolder, String className) {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		compiler.run(null, null, null, path2classFolder.toFile().getAbsolutePath());
		// return javaFile.getParent().resolve("Harmless.class");
		return path2classFolder.getParent().resolve(className);
	}

	/**
	 * Loads the compiled class and instantiates a new object. To be independent
	 * from any cast, the to-be-executed code should be set in the constructor of
	 * the external source code class.
	 * 
	 * @param javaClassPath - Path to Java Class
	 * @param className - class name 
	 * 
	 * @throws MalformedURLException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private void runClass(Path javaClassPath, String className)
			throws MalformedURLException, 
			ClassNotFoundException, 
			IllegalAccessException, 
			InstantiationException 
	{
		URL classUrl = javaClassPath.getParent().toFile().toURI().toURL();
		URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { classUrl });
		//Class<?> clazz = Class.forName("Harmless", true, classLoader);
		if(className.endsWith(".class"))
			className = className.replace(".class", "");
		
		Class<?> clazz = Class.forName(className, true, classLoader);
		clazz.newInstance();
	}
	
	/**
	 * Extract the class name, using Regex.
	 * 
	 * @param source - the class source code.
	 * @return the class name. Or null if was unable to retrieve it
	 */
	public String extractFileName(String source) {
		String src = stripComments(source);
		
		
		
		return null;
	}
	
	/**
	 * Find contend between words
	 * @param src
	 * @param start
	 * @param end
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	 public static List<String> regexBetween(String src, String start, String end, int fromIndex, int toIndex) {
		 
		 /*
		 int p2 = -1;		 
		 int p1 = src.indexOf(start, fromIndex);
		 if(p1>=0) {
			 p2 = src.indexOf(end, p1);
		 }		 
		 if(toIndex!=null)
			 toIndex[0] = p2;
		 
		 	*/	 
		List<String> results = new ArrayList<>(); // For storing results
		
		String exp = ""+start + "(.*?)" + end;
		
		Pattern p = Pattern.compile(exp, Pattern.CASE_INSENSITIVE|Pattern.MULTILINE|Pattern.UNICODE_CASE);
		System.out.println("Regex: " + exp);
		int endIndex = (toIndex<0 || toIndex<=fromIndex || toIndex>src.length()) ? src.length() : toIndex;
				
		String subSrc = src.substring(fromIndex, endIndex);
		Matcher m = p.matcher(subSrc);
		while (m.find()) { // Loop through all matches
			results.add(m.group()); // Get value and store in collection.
		}
		return results;
	}

	/**
	 * Example of use
	 * @param sourcePath
	 * @throws Exception
	 */
	public void doEvil(String sourcePath) throws Exception {
		String source = readCodeAbroad(sourcePath);
		
		String className = "";
		String tempDir = null;
		
		Path javaFile = saveSource(source, tempDir, className);
		Path classFile = compileSource(javaFile, className);
		runClass(classFile, className);
	}

	public static void main(String... args) throws Exception {
		//new EvilExecutor().doEvil(args[0]);
		
		String src = "public class Teste { ABC";
		List<String> list = regexBetween(src, "class ","\\{", 0, -1);
		
		for (String s : list) {
			System.out.println(s);
		}
		
		
		
	}
}
