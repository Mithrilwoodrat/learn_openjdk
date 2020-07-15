package Runtime;

import java.io.Serializable;

public class TestClass implements Serializable {
    private int m;
    private final int n = 1;
    private static long l = 2l;
    private String s = "test";
    public int inc () {
        return m = m +1;
    }
}