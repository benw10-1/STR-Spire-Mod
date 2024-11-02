package str_exporter.builders;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

public class StringCompressionTest {
  // @Test
  public void testCompress() {
    // replace this with what you want to check
    String input = "This is a test stringThis is a test stringThis is a test stringThis is a test stringa";
    String compressed = StringCompression.compress(input);
    assertNotNull(compressed);
    System.out.println("Decompressed - "+input);
    System.out.println("Compressed - "+compressed);
  }
}
