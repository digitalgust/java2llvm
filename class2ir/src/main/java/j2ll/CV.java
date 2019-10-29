package j2ll;

import org.objectweb.asm.*;

import java.io.PrintStream;
import java.lang.reflect.Modifier;
import java.util.*;

public class CV extends ClassVisitor {

    // out
    private PrintStream ps;
    private Statistics statistics;

    // state
    String className;
    String superName;
    private Set<JField> staticFields = new HashSet<JField>();
    private List<JField> fields = new ArrayList<JField>();

    private List<MV> methods = new ArrayList<MV>();
    // shared states
    Set<String> declares = new HashSet<>();


    Map<String, String> staticStrs = new HashMap();
    Set<String> assistFunc = new HashSet<>();

    public CV(PrintStream ps, Statistics statistics) {
        super(Opcodes.ASM5);
        this.ps = ps;
        this.statistics = statistics;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
        this.superName = superName;
    }

    public void visitSource(String source, String debug) {
//        this.ps.println("src " + source);
    }

    public void visitOuterClass(String owner, String name, String desc) {
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return null;
    }

    public void visitAttribute(Attribute attr) {
        this.ps.println(" attr " + attr);
    }

    public void visitInnerClass(String name, String outerName, String innerName, int access) {
    }

    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if ((access & Opcodes.ACC_STATIC) != 0) {
            Util.javaSignature2irType(statistics.getResolver(), desc);
            this.staticFields.add(new JField(name, desc));
        } else {
            this.fields.add(new JField(name, desc));
//            this.ps.println("  f  " + desc + " " + name + " " + signature + " " + value);
        }
        return null;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MV mv = new MV(access, name, desc, this);
        this.methods.add(mv);
        return mv;
    }

    public void visitEnd() {
        this.ps.println("; CLASS: " + this.className + " extends " + this.superName);
        this.ps.println();
        this.ps.println(AssistLLVM.FUNC_DECLARES);
        this.ps.println();

        // declares
        for (String mdeclare : declares) {
            this.ps.println("declare " + mdeclare);
        }
        this.ps.println();

        // use classes
        this.ps.println("; first generation");
        Resolver next = statistics.getResolver();
        for (String name : next.getClasses()) {
            Util.class2struct(next, name);
        }
        for (String name : next.getClasses()) {
            this.ps.println(Util.class2struct(next, name) + " ; use " + name);
        }

        this.ps.println(AssistLLVM.PRE_DEF_TYPE);

        // out fields
        this.ps.println("; globals");
        for (JField field : staticFields) {
            if (field.className == null) {
                String ir = Util.javaSignature2irType(statistics.getResolver(), field.javaSignature);
                this.ps.println(Util.static2str(this.className, field.name) + " = internal global " + ir + " zeroinitializer");
            } else {
                String ir = Util.javaSignature2irType(statistics.getResolver(), field.javaSignature);
                this.ps.println(Util.static2str(field.className, field.name) + " = internal global " + ir + " zeroinitializer");
            }
        }
        this.ps.println(AssistLLVM.GLOBAL_VAR_DECLARE);
        this.ps.println();

        for (String s : staticStrs.values()) {
            this.ps.println(s);
        }
        this.ps.println();

        // out methods
        for (MV method : this.methods) {
            if ((method.access & Modifier.NATIVE) == 0) {
                method.out(this.ps);
            }
        }

        for (String s : assistFunc) {
            this.ps.println(s);
        }
        this.ps.println();

    }

    public Set<JField> getStaticFields() {
        return staticFields;
    }
}