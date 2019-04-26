package paperlab.ccsim.main;

import lombok.Data;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.container.containerPlacementPolicies.*;
import org.cloudbus.cloudsim.container.containerSelectionPolicies.PowerContainerSelectionPolicy;
import org.cloudbus.cloudsim.container.containerSelectionPolicies.PowerContainerSelectionPolicyCor;
import org.cloudbus.cloudsim.container.containerSelectionPolicies.PowerContainerSelectionPolicyMaximumUsage;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmPe;
import org.cloudbus.cloudsim.container.core.*;
import org.cloudbus.cloudsim.container.hostSelectionPolicies.*;
import org.cloudbus.cloudsim.container.resourceAllocatorMigrationEnabled.PowerContainerVmAllocationPolicyMigrationAbstractHostSelection;
import org.cloudbus.cloudsim.container.resourceAllocatorMigrationEnabled.PowerContainerVmAllocationPolicyMigrationStaticThresholdMC;
import org.cloudbus.cloudsim.container.resourceAllocatorMigrationEnabled.PowerContainerVmAllocationPolicyMigrationStaticThresholdMCUnderUtilized;
import org.cloudbus.cloudsim.container.resourceAllocators.ContainerAllocationPolicy;
import org.cloudbus.cloudsim.container.resourceAllocators.ContainerAllocationPolicyRS;
import org.cloudbus.cloudsim.container.resourceAllocators.ContainerVmAllocationPolicy;
import org.cloudbus.cloudsim.container.resourceAllocators.PowerContainerAllocationPolicySimple;
import org.cloudbus.cloudsim.container.vmSelectionPolicies.PowerContainerVmSelectionPolicy;
import org.cloudbus.cloudsim.container.vmSelectionPolicies.PowerContainerVmSelectionPolicyMaximumCorrelation;
import org.cloudbus.cloudsim.container.vmSelectionPolicies.PowerContainerVmSelectionPolicyMaximumUsage;
import org.cloudbus.cloudsim.core.CloudSim;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * 进行多种策略模拟的抽象类
 */
@Data
public abstract class AbstractContainerSimulation {
    /**
     * 物理机
     */
    protected List<ContainerHost> hostList;

    /**
     * 运行容器的虚拟机
     */
    protected List<ContainerVm> vmList;

    /**
     * 容器
     */
    protected List<Container> containerList;

    /**
     * 需要运行在容器中的任务
     */
    protected List<ContainerCloudlet> cloudletList;

    /**
     * 作为用户的代理，与ContainerDatacenter进行交互，模拟用户提交任务请求等工作
     */
    protected ContainerDatacenterBroker broker;

    /**
     * 超量预定因子，用于设定容器请求的最大cpu利用率
     */
    private double overBookingFactor;

    // 实验命名
    private String experimentName;
    // 是否输出日志
    private boolean enableOutput;
    // 日志输出地址
    private String logAddress;
    // 进行重复实验时，实验的标号
    private String runtime;


    public AbstractContainerSimulation(boolean enableOutput, boolean outputToFile,
                                       String inputFolder, String outputFolder,
                                       String vmAllocationPolicy, String containerAllocationPolicy,
                                       String vmSelectionPolicy, String containerSelectionPolicy,
                                       String hostSelectionPolicy, double overBookingFactor,
                                       String runtime, String logAddress) {
        setOverBookingFactor(overBookingFactor);
        setRuntime(runtime);
        setLogAddress(logAddress);
        setExperimentName(this.getExperimentName(hostSelectionPolicy, vmAllocationPolicy,
                vmSelectionPolicy, containerSelectionPolicy,
                containerAllocationPolicy, String.valueOf(getOverBookingFactor()), runtime));

        try {
            this.initLogOutput(enableOutput, outputToFile, outputFolder, vmAllocationPolicy, vmSelectionPolicy, containerSelectionPolicy, hostSelectionPolicy);
        } catch (Exception var10) {
            var10.printStackTrace();
            System.exit(0);
        }
        this.init(inputFolder + "/", getOverBookingFactor());
        this.start(getExperimentName(), outputFolder, this.getVmAllocationPolicy(vmAllocationPolicy, vmSelectionPolicy, containerSelectionPolicy, hostSelectionPolicy), getContainerAllocationPolicy(containerAllocationPolicy));
    }

    protected void initLogOutput(boolean enableOutput, boolean outputToFile,
                                 String outputFolder, String vmAllocationPolicy,
                                 String vmSelectionPolicy, String containerSelectionPolicy,
                                 String hostSelectionPolicy) throws IOException {
        this.setEnableOutput(enableOutput);
        Log.setDisabled(!this.isEnableOutput());
        if (this.isEnableOutput() && outputToFile) {
            int index = getExperimentName().lastIndexOf("_");

            mkdir(outputFolder);
            mkdir(outputFolder + "/log/" + getExperimentName().substring(0, index));
            mkdir(outputFolder + "/ContainerMigration/" + getExperimentName().substring(0, index));
            mkdir(outputFolder + "/NewlyCreatedVms/" + getExperimentName().substring(0, index));
            mkdir(outputFolder + "/EnergyConsumption/" + getExperimentName().substring(0, index));

            File file = new File(outputFolder + "/log/" + getExperimentName().substring(0, index)
                    + "/" + this.getExperimentName(hostSelectionPolicy, vmAllocationPolicy,
                    vmSelectionPolicy, containerSelectionPolicy,
                    String.valueOf(getOverBookingFactor()), getRuntime()) + ".txt");
            if (!file.createNewFile()) {
                throw new RuntimeException("Can't create file");
            }
            Log.setOutput(new FileOutputStream(file));
        }

    }

    private void mkdir(String dirName) {
        File folder = new File(dirName);
        File parent = folder.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Couldn't create dir: " + parent);
        }
        if (!folder.exists()) {
            if (!folder.mkdir()) {
                throw new RuntimeException("Can't create folder");
            }
        }
    }


    private List<ContainerHost> createHostList(final int hostNumber) {
        ArrayList<ContainerHost> hostList = new ArrayList<ContainerHost>();
        IntStream.range(0, hostNumber).forEach(i -> {
            int type = i % hostNumber;
            // 物理机中的Pe，由 ContainerVmPeProvisioner 控制向虚拟机分配mips
            List<ContainerVmPe> peList = new ArrayList<>();

        });
        return hostList;
    }

    protected String getExperimentName(String... args) {
        StringBuilder experimentName = new StringBuilder();

        for (int i = 0; i < args.length; ++i) {
            if (!args[i].isEmpty()) {
                if (i != 0) {
                    experimentName.append("_");
                }

                experimentName.append(args[i]);
            }
        }

        return experimentName.toString();
    }

    public String getExperimentName() {
        return experimentName;
    }

    protected abstract void init(String var1, double overBookingFactor);

    protected void start(String experimentName, String outputFolder,
                         ContainerVmAllocationPolicy vmAllocationPolicy,
                         ContainerAllocationPolicy containerAllocationPolicy) {
        System.out.println("Starting " + experimentName);

        try {
            PowerContainerDatacenter e = (PowerContainerDatacenter) HelperEx.createDatacenter("datacenter",
                    PowerContainerDatacenterCM.class, hostList, vmAllocationPolicy, containerAllocationPolicy,
                    getExperimentName(), Settings.SCHEDULING_INTERVAL, getLogAddress(),
                    Settings.VM_STARTTUP_DELAY, Settings.CONTAINER_STARTTUP_DELAY);
            vmAllocationPolicy.setDatacenter(e);
            e.setDisableVmMigrations(false);
            broker.submitVmList(vmList);
            broker.submitContainerList(containerList);
            broker.submitCloudletList(cloudletList.subList(0, containerList.size()));

            CloudSim.terminateSimulation(86400.0D);
            double lastClock = CloudSim.startSimulation();
            List newList = broker.getCloudletReceivedList();
            Log.printLine("Received " + newList.size() + " cloudlets");
            CloudSim.stopSimulation();

            HelperEx.printResultsNew(e, broker, lastClock, experimentName, true, outputFolder);
        } catch (Exception var8) {
            var8.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
            System.exit(0);
        }

        Log.printLine("Finished " + experimentName);
    }

    protected ContainerVmAllocationPolicy getVmAllocationPolicy(String vmAllocationPolicyName, String vmSelectionPolicyName, String containerSelectionPolicyName, String hostSelectionPolicyName) {
        ContainerVmAllocationPolicy vmAllocationPolicy = null;
        PowerContainerVmSelectionPolicy vmSelectionPolicy = null;
        PowerContainerSelectionPolicy containerSelectionPolicy = null;
        HostSelectionPolicy hostSelectionPolicy = null;
        if (!vmSelectionPolicyName.isEmpty() && !containerSelectionPolicyName.isEmpty() && !hostSelectionPolicyName.isEmpty()) {
            vmSelectionPolicy = this.getVmSelectionPolicy(vmSelectionPolicyName);
            containerSelectionPolicy = this.getContainerSelectionPolicy(containerSelectionPolicyName);
            hostSelectionPolicy = this.getHostSelectionPolicy(hostSelectionPolicyName);
        }


        if (vmAllocationPolicyName.startsWith("MSThreshold-Over_")) {
            double overUtilizationThreshold = Double.parseDouble(vmAllocationPolicyName.substring(18));
            vmAllocationPolicy = new PowerContainerVmAllocationPolicyMigrationStaticThresholdMC(hostList, vmSelectionPolicy,
                    containerSelectionPolicy, hostSelectionPolicy, overUtilizationThreshold,
                    Settings.VM_TYPES, Settings.VM_PES, Settings.VM_RAM, Settings.VM_BW,
                    Settings.VM_SIZE, Settings.VM_MIPS);
        } else if (vmAllocationPolicyName.startsWith("MSThreshold-Under_")) {
            double overUtilizationThreshold = Double.parseDouble(vmAllocationPolicyName.substring(18, 22));
            double underUtilizationThreshold = Double.parseDouble(vmAllocationPolicyName.substring(24));
            vmAllocationPolicy = new PowerContainerVmAllocationPolicyMigrationStaticThresholdMCUnderUtilized(hostList,
                    vmSelectionPolicy, containerSelectionPolicy, hostSelectionPolicy, overUtilizationThreshold,
                    underUtilizationThreshold, Settings.VM_TYPES, Settings.VM_PES, Settings.VM_RAM, Settings.VM_BW,
                    Settings.VM_SIZE, Settings.VM_MIPS);

        } else if (vmAllocationPolicyName.startsWith("VMThreshold-Under_")) {

            double overUtilizationThreshold = Double.parseDouble(vmAllocationPolicyName.substring(18, 22));
            double underUtilizationThreshold = Double.parseDouble(vmAllocationPolicyName.substring(24));
            vmAllocationPolicy = new PowerContainerVmAllocationPolicyMigrationAbstractHostSelection(hostList, vmSelectionPolicy, hostSelectionPolicy, overUtilizationThreshold, underUtilizationThreshold);


        } else {
            System.out.println("Unknown VM allocation policy: " + vmAllocationPolicyName);
            System.exit(0);
        }
        return vmAllocationPolicy;
    }

    protected ContainerAllocationPolicy getContainerAllocationPolicy(String containerAllocationPolicyName) {
        ContainerAllocationPolicy containerAllocationPolicy;
        if (containerAllocationPolicyName == "Simple") {

            containerAllocationPolicy = new PowerContainerAllocationPolicySimple(); // DVFS policy without VM migrations
        } else {

            ContainerPlacementPolicy placementPolicy = getContainerPlacementPolicy(containerAllocationPolicyName);
            containerAllocationPolicy = new ContainerAllocationPolicyRS(placementPolicy); // DVFS policy without VM migrations
        }

        return containerAllocationPolicy;
    }

    protected ContainerPlacementPolicy getContainerPlacementPolicy(String name) {
        ContainerPlacementPolicy placementPolicy;
        switch (name) {
            case "LeastFull":
                placementPolicy = new ContainerPlacementPolicyLeastFull();
                break;
            case "MostFull":
                placementPolicy = new ContainerPlacementPolicyMostFull();
                break;

            case "FirstFit":
                placementPolicy = new ContainerPlacementPolicyFirstFit();
                break;
            case "Random":
                placementPolicy = new ContainerPlacementPolicyRandomSelection();
                break;
            default:
                placementPolicy = null;
                System.out.println("The container placement policy is not defined");
                break;
        }
        return placementPolicy;
    }

    protected HostSelectionPolicy getHostSelectionPolicy(String hostSelectionPolicyName) {
        HostSelectionPolicy hostSelectionPolicy = null;
        if (hostSelectionPolicyName.equals("FirstFit")) {

            hostSelectionPolicy = new HostSelectionPolicyFirstFit();


        } else if (hostSelectionPolicyName.equals("LeastFull")) {

            hostSelectionPolicy = new HostSelectionPolicyLeastFull();


        } else if (hostSelectionPolicyName.equals("MostFull")) {

            hostSelectionPolicy = new HostSelectionPolicyMostFull();


        } else if (hostSelectionPolicyName.equals("RandomSelection")) {

            hostSelectionPolicy = new HostSelectionPolicyRandomSelection();
        } else {
            System.out.println("Unknown Host selection policy: " + hostSelectionPolicyName);
            System.exit(0);
        }

        return hostSelectionPolicy;
    }

    protected PowerContainerSelectionPolicy getContainerSelectionPolicy(String containerSelectionPolicyName) {
        PowerContainerSelectionPolicy containerSelectionPolicy = null;
        if (containerSelectionPolicyName.equals("Cor")) {
            containerSelectionPolicy = new PowerContainerSelectionPolicyCor(new PowerContainerSelectionPolicyMaximumUsage());
        } else if (containerSelectionPolicyName.equals("MaxUsage")) {
            containerSelectionPolicy = new PowerContainerSelectionPolicyMaximumUsage();


        } else {
            System.out.println("Unknown Container selection policy: " + containerSelectionPolicyName);
            System.exit(0);
        }

        return containerSelectionPolicy;
    }

    protected PowerContainerVmSelectionPolicy getVmSelectionPolicy(String vmSelectionPolicyName) {
        PowerContainerVmSelectionPolicy vmSelectionPolicy = null;
        if (vmSelectionPolicyName.equals("VmMaxC")) {
            vmSelectionPolicy = new PowerContainerVmSelectionPolicyMaximumCorrelation(new PowerContainerVmSelectionPolicyMaximumUsage());
        } else if (vmSelectionPolicyName.equals("VmMaxU")) {
            vmSelectionPolicy = new PowerContainerVmSelectionPolicyMaximumUsage();
        } else {
            System.out.println("Unknown VM selection policy: " + vmSelectionPolicyName);
            System.exit(0);
        }

        return vmSelectionPolicy;
    }

}
