package paperlab.ccsim.example;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarReadTest {

  @Test
  public void test() {
    URL testClassURL = getClass().getClassLoader().getResource("");
    try {
      URL jarURL = new URL(testClassURL, "../classes/lib/cloudsim-examples-4.0.jar");
      JarFile jar = new JarFile(URLDecoder.decode(jarURL.getPath(), "UTF-8"));
      Enumeration<JarEntry> entries = jar.entries();
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        String name = entry.getName();
        if (name.startsWith("workload/planetlab") && !entry.isDirectory()) {
          InputStream in = jar.getInputStream(entry);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
