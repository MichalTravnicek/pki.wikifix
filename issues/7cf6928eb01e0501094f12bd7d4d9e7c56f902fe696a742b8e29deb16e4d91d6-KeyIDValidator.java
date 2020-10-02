import java.math.BigInteger;

public class KeyIDValidator {

    public final static char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    public static void main(String[] args) {

        String inputString = args[0].toLowerCase();
        System.out.println("Key ID in NSS database : " + inputString);

        byte[] inputBytes = new byte[inputString.length()/2];

        for (int i=0; i<inputBytes.length; i++) {
            String s = inputString.substring(i*2, i*2 + 2);
            inputBytes[i] = (byte) Integer.parseInt(s, 16);
        }

        BigInteger inputKeyID = new BigInteger(inputBytes);
        String storedString = inputKeyID.toString(16);
        System.out.println("Key ID stored in CS.cfg: " + storedString);

        BigInteger outputKeyID = new BigInteger(storedString, 16);
        byte[] outputBytes = outputKeyID.toByteArray();

        StringBuilder sb = new StringBuilder();
        for (byte b : outputBytes) {
            int i = b & 0xFF;
            sb.append(HEX_CHARS[i >> 4]);
            sb.append(HEX_CHARS[i & 0x0F]);
        }

        String outputString = sb.toString();
        System.out.println("Key ID used for lookup : " + outputString);

        System.out.println();

        if (inputString.equals(outputString)) {
            System.out.println("SUCCESS");
        } else {
            System.out.println("FAILED");
        }
    }
}

