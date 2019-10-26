package j2ll;

import classparser.ClassHelper;
import compiler.MyCompiler;
import org.objectweb.asm.ClassReader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Main {


    public static void main(String[] args) throws IOException {
        String srcPath = "./app/java/";
        String classesPath = "./app/out/classes/";
        String llvmPath = "./app/c/";


        if (args.length < 3) {
            System.out.println("Posix :");
            System.out.println("Compile java file:");
            System.out.println("java -cp ./class2ir/dist/class2ir.jar:./class2ir/lib/asm-7.2.jar:./class2ir/lib/classparser.jar j2ll.Main ./app/java ./app/out/classes ./app/c/");
        } else {
            srcPath = args[0] + "/";
            classesPath = args[1] + "/";
            llvmPath = args[2] + "/";
        }

        System.out.println("java source *.java path      : " + srcPath);
        System.out.println("classes *.class output path  : " + classesPath);
        System.out.println("llvm *.ll output path        : " + llvmPath);

        javaSrc2class(srcPath, classesPath);

//        class2ll(classesPath, llvmPath);

        conv("java.lang.Object", classesPath, llvmPath);
        conv("java.io.PrintStream", classesPath, llvmPath);
        conv("java.lang.System", classesPath, llvmPath);
        conv("java.lang.Throwable", classesPath, llvmPath);
        conv("java.lang.NullPointerException", classesPath, llvmPath);
        conv("java.lang.String", classesPath, llvmPath);
        conv("java.lang.StringBuilder", classesPath, llvmPath);
        conv("test.Test", classesPath, llvmPath);
        conv("test.TestParent", classesPath, llvmPath);

        //gen clinit call
        AssistLLVM.genClinits(llvmPath);
    }

    static void javaSrc2class(String srcPath, String classesPath) throws IOException {

        File f = new File(classesPath);
        if (!f.exists()) {
            f.mkdirs();
        }
        compiler.MyCompiler.compile(srcPath, classesPath);
        List<String> files = new ArrayList<>();
        MyCompiler.find(classesPath, files, null, ".class");

        Util.helper = new ClassHelper("", files.toArray(new String[files.size()]));
        Util.helper.openClasses();

    }

    static void class2ll(String classesPath, String llvmPath) throws IOException {
        List<String> files = new ArrayList<>();
        MyCompiler.find(classesPath, files, null, ".class");
        String classesAbsPath = new File(classesPath).getAbsolutePath();
        for (String cp : files) {
            String className = cp.substring(classesAbsPath.length() + 1);
            className = className.replaceAll("[\\\\/]{1,}", ".");
            className = className.replace(".class", "");
            conv(className, classesPath, llvmPath);
        }
    }

    static void conv(String className, String classesPath, String llvmPath) throws IOException {

        String outFileName = className + ".ll";
        PrintStream ps = new PrintStream(new File(llvmPath, outFileName));

        Statistics statistics = new Statistics();
        StatisticsCollector sc = new StatisticsCollector(statistics);
        CV cv = new CV(ps, statistics);

        // read class
        String fn = classesPath + className.replace('.', '/') + ".class";
        System.out.println("class convert to llvm ir:" + fn);
        InputStream is = new FileInputStream(fn);
        ClassReader cr = new ClassReader(is);
        cr.accept(sc, 0);


        cr.accept(cv, 0);
        ps.flush();
        is.close();


    }


}


