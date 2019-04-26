package paperlab.ccsim.main;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.Calendar;

/**
 * 进行多种策略模拟的实现类
 */
public class ContainerSimulationImpl extends AbstractContainerSimulation {

    public ContainerSimulationImpl (
            boolean enableOutput,
            boolean outputToFile,
            String inputFolder,
            String outputFolder,
            String vmAllocationPolicy,
            String containerAllocationPolicy,
            String vmSelectionPolicy,
            String containerSelectionPolicy,
            String hostSelectionPolicy,
            double overBookingFactor, String runTime, String logAddress) {


        super(enableOutput,
                outputToFile,
                inputFolder,
                outputFolder,
                vmAllocationPolicy,
                containerAllocationPolicy,
                vmSelectionPolicy,
                containerSelectionPolicy,
                hostSelectionPolicy,
                overBookingFactor, runTime, logAddress);

    }

    @Override
    protected void init(String inputFolder, double overBookingFactor) {
        try {
            CloudSim.init(1, Calendar.getInstance(), false);
//            setOverBookingFactor(overBookingFactor);
            broker = HelperEx.createBroker(overBookingFactor);
            int brokerId = broker.getId();
            cloudletList = HelperEx.createContainerCloudletList(brokerId, inputFolder, Settings.NUMBER_CLOUDLETS);
            containerList = HelperEx.createContainerList(brokerId, Settings.NUMBER_CLOUDLETS);
            vmList = HelperEx.createVmList(brokerId, Settings.NUMBER_VMS);
            hostList = HelperEx.createHostList(Settings.NUMBER_HOSTS);

        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
            System.exit(0);
        }
    }
}
