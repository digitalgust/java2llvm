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

        compile(srcPath, classesPath);

        convert2ll("java.lang.Object", classesPath, llvmPath);
        convert2ll("java.io.PrintStream", classesPath, llvmPath);
        convert2ll("java.lang.System", classesPath, llvmPath);
        convert2ll("java.lang.Throwable", classesPath, llvmPath);
        convert2ll("java.lang.NullPointerException", classesPath, llvmPath);
        convert2ll("java.lang.String", classesPath, llvmPath);
        convert2ll("java.lang.StringBuilder", classesPath, llvmPath);
        convert2ll("test.Test", classesPath, llvmPath);
        convert2ll("test.TestParent", classesPath, llvmPath);

    }

    static void compile(String srcPath, String classesPath) {

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

    static void convert2ll(String className, String classesPath, String llvmPath) throws IOException {

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


