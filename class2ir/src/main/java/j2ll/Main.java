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
        String srcPath = "../app/java/";
        String distPath = "../app/out/classes/";
        File f = new File(distPath);
        if (!f.exists()) {
            f.mkdirs();
        }
        compiler.MyCompiler.compile(srcPath, distPath);
        List<String> files = new ArrayList<>();
        MyCompiler.find(distPath, files, null, ".class");

        Util.helper = new ClassHelper("", files.toArray(new String[files.size()]));
        Util.helper.openClasses();

        conv("java.lang.Object", distPath);
        conv("java.io.PrintStream", distPath);
        conv("java.lang.System", distPath);
        conv("java.lang.Throwable", distPath);
        conv("java.lang.NullPointerException", distPath);
        conv("java.lang.String", distPath);
        conv("java.lang.StringBuilder", distPath);
        conv("test.Test", distPath);
        conv("test.TestParent", distPath);

    }


    static void conv(String className, String home) throws IOException {

        String out = className + ".ll";
        PrintStream ps = new PrintStream(new File("../app/c/", out));

        Statistics statistics = new Statistics();
        StatisticsCollector sc = new StatisticsCollector(statistics);
        CV cv = new CV(ps, statistics);

        // read class
        String prefix = home;
        String fn = prefix + className.replace('.', '/') + ".class";
        System.out.println("class convert to llvm ir:" + fn);
        InputStream is = new FileInputStream(fn);
        ClassReader cr = new ClassReader(is);
//        ClassReader cr = new ClassReader(className);
        cr.accept(sc, 0);


        cr.accept(cv, 0);
        ps.flush();
        is.close();
    }


}


