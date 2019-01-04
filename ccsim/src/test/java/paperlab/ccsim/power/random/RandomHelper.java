package paperlab.ccsim.power.random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelNull;
import org.cloudbus.cloudsim.UtilizationModelStochastic;

import java.util.ArrayList;
import java.util.List;

import static paperlab.ccsim.power.Constants.CLOUDLET_LENGTH;
import static paperlab.ccsim.power.Constants.CLOUDLET_PES;
import static paperlab.ccsim.power.random.RandomConstants.CLOUDLET_UTILIZATION_SEED;

public final class RandomHelper {
  private RandomHelper() {}

  public static List<Cloudlet> createCloudletList(int brokerId, int numberOfCloudlet) {
    List<Cloudlet> cloudletList = new ArrayList<>();

    long fileSize = 300;
    long outputSize = 300;
    UtilizationModel utilizationModelNull = new UtilizationModelNull();

    for (int i = 0; i < numberOfCloudlet; i++) {
      Cloudlet cloudlet = new Cloudlet(
          i,
          CLOUDLET_LENGTH,
          CLOUDLET_PES,
          fileSize,
          outputSize,
          new UtilizationModelStochastic(CLOUDLET_UTILIZATION_SEED * i),
          utilizationModelNull,
          utilizationModelNull);
      cloudlet.setUserId(brokerId);
      cloudlet.setVmId(i);
      cloudletList.add(cloudlet);
    }

    return cloudletList;
  }
}
