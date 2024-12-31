package namnv.consumer;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

public class BufferExample {
  public static void main(String[] args) {
    // Create a new buffer (e.g., 128 bytes)
    MutableDirectBuffer buffer = new UnsafeBuffer(new byte[128]);

    // The string to write
    String myString = "Hello, Agrona!";

    // Get the offset where we want to write
    int offset = 0;

    // Convert the string to a byte array and write it into the buffer
    byte[] stringBytes = myString.getBytes();
    buffer.putBytes(offset, stringBytes, 0, stringBytes.length);

    var stringBytes1 = new byte[128];
    buffer.getBytes(offset, stringBytes1);
    // Print out the contents from the buffer (for verification)
    System.out.println("Buffer contains: " + new String(stringBytes1));
  }
}

