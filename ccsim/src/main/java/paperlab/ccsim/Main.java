package paperlab.ccsim;

import org.cloudbus.cloudsim.util.MathUtil;

public class Main {
    public static void main(String[] args) {
        double median = MathUtil.median(new double[]{1.1, 2.3, 4.5});
        System.out.println(median);
    }
}
