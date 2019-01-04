package paperlab.ccsim.power.random;

import org.cloudbus.cloudsim.core.CloudSim;
import paperlab.ccsim.power.AbstractRunner;
import paperlab.ccsim.power.Helper;

import java.util.Calendar;

import static paperlab.ccsim.power.random.RandomConstants.NUMBER_OF_HOST;
import static paperlab.ccsim.power.random.RandomConstants.NUMBER_OF_VM;

public class RandomRunner extends AbstractRunner {

  public RandomRunner(boolean enableOutput, boolean outputToFile, String inputFolder, String outputFolder, String workload, String vmAllocationPolicy, String vmSelectionPolicy, String parameter) {
    super(enableOutput, outputToFile, inputFolder, outputFolder, workload, vmAllocationPolicy, vmSelectionPolicy, parameter);
  }

  @Override
  protected void init(String inputFolder) {
    CloudSim.init(1, Calendar.getInstance(), false);

    broker = Helper.createBroker();
    int brokerId = broker.getId();

    cloudletList = RandomHelper.createCloudletList(brokerId, NUMBER_OF_VM);
    vmList = Helper.createVmList(brokerId, cloudletList.size());
    hostList = Helper.createHostList(NUMBER_OF_HOST);
  }
}
