package java.lang;

public class Throwable {
    String detailMessage;

    public Throwable(){
        detailMessage="";
    }

    public Throwable(String s){
        detailMessage=s;
    }

    public String getMessage() {
        return detailMessage;
    }
}
