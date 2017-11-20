package test;

/**
 * Created by slimon on 20-11-17.
 */
public class MiscTester {

    public static void main(String[] args) {
        /*short value = 0;
        System.out.println(shortToUInt(value));
        value = 1000;
        System.out.println(shortToUInt(value));
        value = -1;
        System.out.println(shortToUInt(value));
        value = Short.MAX_VALUE;
        System.out.println(shortToUInt(value));
        value = Short.MIN_VALUE;
        System.out.println(shortToUInt(value));*/
        short s = -1;
        int ii = -1;
        char c = (char) ii;
        int i = c;
        System.out.println(c);
        System.out.println(i);
        for(s = Short.MIN_VALUE; s < Short.MAX_VALUE; s += Math.min(4096, Short.MAX_VALUE - s)) {
            System.out.println(s + "    " + shortToUInt(s));
        }
        s = Short.MAX_VALUE;
        System.out.println(s + "    " + shortToUInt(s));
        s = 1;
        System.out.println(s + "    " + shortToUInt(s));
        s = -1;
        System.out.println(s + "    " + shortToUInt(s));
        System.out.println((Short.MIN_VALUE & 0xFFFF) << 1);
        System.out.println(1 << 16);

        System.out.println(uIntToShort(0));
        System.out.println(uIntToShort(65535));
    }

    private static int shortToUInt(short value) {
        return (int)value + 32768;
    }

    private static short uIntToShort(int value) {
        return (short) (value - 32768);
    }
}
