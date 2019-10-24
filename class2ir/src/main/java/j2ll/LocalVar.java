package j2ll;

/**
 * Local Variable
 */
public class LocalVar {

    int slot;
    String name;
    String signature;
    int startAt;  //start at label index
    int endAt;     //end at label index

    public LocalVar(int slot, String name, String signature) {
        this.slot = slot;
        this.name = name;
        this.signature = signature;
    }
}
