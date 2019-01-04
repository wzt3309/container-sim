package paperlab.ccsim.example;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.UtilizationModelNull;
import org.cloudbus.cloudsim.container.containerProvisioners.ContainerBwProvisionerSimple;
import org.cloudbus.cloudsim.container.containerProvisioners.ContainerPe;
import org.cloudbus.cloudsim.container.containerProvisioners.ContainerRamProvisionerSimple;
import org.cloudbus.cloudsim.container.containerProvisioners.CotainerPeProvisionerSimple;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmBwProvisionerSimple;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmPe;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmPeProvisionerSimple;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmRamProvisionerSimple;
import org.cloudbus.cloudsim.container.core.*;
import org.cloudbus.cloudsim.container.hostSelectionPolicies.HostSelectionPolicy;
import org.cloudbus.cloudsim.container.hostSelectionPolicies.HostSelectionPolicyFirstFit;
import org.cloudbus.cloudsim.container.resourceAllocatorMigrationEnabled.PowerContainerVmAllocationPolicyMigrationAbstractHostSelection;
import org.cloudbus.cloudsim.container.resourceAllocators.ContainerAllocationPolicy;
import org.cloudbus.cloudsim.container.resourceAllocators.ContainerVmAllocationPolicy;
import org.cloudbus.cloudsim.container.resourceAllocators.PowerContainerAllocationPolicySimple;
import org.cloudbus.cloudsim.container.schedulers.ContainerCloudletSchedulerDynamicWorkload;
import org.cloudbus.cloudsim.container.schedulers.ContainerSchedulerTimeSharedOverSubscription;
import org.cloudbus.cloudsim.container.schedulers.ContainerVmSchedulerTimeSharedOverSubscription;
import org.cloudbus.cloudsim.container.utils.IDs;
import org.cloudbus.cloudsim.container.vmSelectionPolicies.PowerContainerVmSelectionPolicy;
import org.cloudbus.cloudsim.container.vmSelectionPolicies.PowerContainerVmSelectionPolicyMaximumUsage;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.container.UtilizationModelPlanetLabInMemoryExtended;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

import static paperlab.ccsim.example.Constants.*;

class ContainerCloudSimExample01 {
  private static final int DEFAULT_NUMBER_OF_USER = 1;

  private List<ContainerCloudlet> cloudletList;
  private List<ContainerVm> vmList;
  private List<Container> containerList;
  private List<ContainerHost> hostList;

  ContainerCloudSimExample01(int numberOfUser, Calendar calendar, boolean traceFlag) {
    if (numberOfUser <= 0) {
      numberOfUser = DEFAULT_NUMBER_OF_USER;
    }

    if (calendar == null) {
      calendar = Calendar.getInstance();
    }
    CloudSim.init(numberOfUser, calendar, traceFlag);

    cloudletList = new ArrayList<>();
    vmList = new ArrayList<>();
    containerList = new ArrayList<>();
    hostList = new ArrayList<>();
  }

  public static void main(String[] args) {
    try {
      new ContainerCloudSimExample01(1, Calendar.getInstance(), false).run();
    } catch (Exception e) {
      e.printStackTrace();
      Log.printLine("Unwanted errors happen");
    }
  }

  void run() throws Exception {
    // container分配给vm的策略
    ContainerAllocationPolicy containerAllocationPolicy = new PowerContainerAllocationPolicySimple();
    // 当host已经over-loaded时，选择需要迁移的vm
    PowerContainerVmSelectionPolicy vmSelectionPolicy = new PowerContainerVmSelectionPolicyMaximumUsage();
    // 选择迁移的目的地host
    HostSelectionPolicy hostSelectionPolicy = new HostSelectionPolicyFirstFit();

    // 定义host over-load和under-load的阈值
    double overUtilizationThreshold = 0.8;
    double underUtilizationThreshold = 0.7;

    initHostList(NUMBER_HOST);
    ContainerVmAllocationPolicy vmAllocationPolicy =
        new PowerContainerVmAllocationPolicyMigrationAbstractHostSelection(
            hostList,
            vmSelectionPolicy,
            hostSelectionPolicy,
            overUtilizationThreshold,
            underUtilizationThreshold
        );

    int overBookingFactor = 80;
    ContainerDatacenterBroker broker = createBroker(overBookingFactor);
    int brokerId = broker.getId();
    initCloudletList(brokerId, NUMBER_CLOUDLET);
    initContainerList(brokerId, NUMBER_CLOUDLET);
    initVmList(brokerId, NUMBER_VM);

    String logAddress = "D:\\tmp\\Results";
    PowerContainerDatacenter e = (PowerContainerDatacenter) createDatacenter("datacenter",
        vmAllocationPolicy, containerAllocationPolicy,
        getExperName("ContainerCloudSimExample01", String.valueOf(overBookingFactor)),
        SCHEDULING_INTERVAL,
        logAddress,
        VM_STARTUP_DELAY,
        CONTAINER_STARTUP_DELAY);

    broker.submitCloudletList(cloudletList.subList(0, containerList.size()));
    broker.submitContainerList(containerList);
    broker.submitVmList(vmList);

    CloudSim.terminateSimulation(SCHEDULING_LIMIT);
    CloudSim.startSimulation();
    CloudSim.stopSimulation();
    List<ContainerCloudlet> newList = broker.getCloudletReceivedList();
    printCloudletList(newList);
  }

  private void initHostList(int numberOfHost) {
    for (int i = 0; i < numberOfHost; i++) {
      int hostType = i % HOST_TYPES;
      List<ContainerVmPe> peList = new ArrayList<>();
      for (int j = 0; j < HOST_PES[hostType]; j++) {
        peList.add(new ContainerVmPe(j, new ContainerVmPeProvisionerSimple(HOST_MIPS[hostType])));
      }

      hostList.add(new PowerContainerHostUtilizationHistory(IDs.pollId(ContainerHost.class),
          new ContainerVmRamProvisionerSimple(HOST_RAM[hostType]),
          new ContainerVmBwProvisionerSimple(HOST_BW),
          HOST_STORAGE,
          peList,
          new ContainerVmSchedulerTimeSharedOverSubscription(peList),
          HOST_POWER[hostType]));
    }
  }

  private ContainerDatacenterBroker createBroker(int overBookingFactor) {
    ContainerDatacenterBroker broker = null;
    try {
      broker = new ContainerDatacenterBroker("Broker", overBookingFactor);
    } catch (Exception var1) {
      var1.printStackTrace();
      System.exit(1);
    }

    return broker;
  }

  private void initCloudletList(int brokerId, int numberOfCloudlet) throws IOException {
    String dataFolderName = "D:\\data\\cloudsim\\workload\\planetlab";
    long fileSize = 300L;
    long outputSize = 300L;
    UtilizationModelNull utilizationModelNull = new UtilizationModelNull();

    File dataFolder = new File(dataFolderName);
    File[] subDataFolders = dataFolder.listFiles();
    int createdCloudlets = 0;

    assert subDataFolders != null;
    for (File subDataFolder : subDataFolders) {
      if (subDataFolder.isDirectory()) {
        File[] dataFiles = subDataFolder.listFiles();
        assert dataFiles != null;

        for (File dataFile : dataFiles) {
          if (createdCloudlets >= numberOfCloudlet) {
            break;
          }
          ContainerCloudlet cloudlet =
              new ContainerCloudlet(IDs.pollId(ContainerCloudlet.class),
                  CLOUDLET_LENGTH, 1,
                  fileSize, outputSize,
                  new UtilizationModelPlanetLabInMemoryExtended(
                      dataFile.getAbsolutePath(), SCHEDULING_INTERVAL),
                  utilizationModelNull, utilizationModelNull);
          cloudlet.setUserId(brokerId);
          cloudletList.add(cloudlet);
          createdCloudlets++;
        }
      }
    }
  }

  private void initContainerList(int brokerId, int numberOfContainer) {
    for (int i = 0; i < numberOfContainer; i++) {
      int type = i % CONTAINER_TYPES;
      containerList.add(new PowerContainer(IDs.pollId(Container.class), brokerId,
          CONTAINER_MIPS[type], CONTAINER_PES[type], CONTAINER_RAM[type], CONTAINER_BW,
          0L, "Xen",
          new ContainerCloudletSchedulerDynamicWorkload(CONTAINER_MIPS[type], CONTAINER_PES[type]),
          SCHEDULING_INTERVAL));
    }
  }

  private void initVmList(int brokerId, int numberOfVm) {
    for (int i = 0; i < numberOfVm; i++) {
      int type = i % VM_TYPES;
      List<ContainerPe> peList = new ArrayList<>();
      for (int j = 0; j < VM_PES[type]; j++) {
        peList.add(new ContainerPe(j,
            new CotainerPeProvisionerSimple(VM_MIPS[type])));
      }
      vmList.add(new PowerContainerVm(IDs.pollId(ContainerVm.class), brokerId,
          VM_MIPS[type], VM_RAM[type], VM_BW, VM_SIZE, "Xen",
          new ContainerSchedulerTimeSharedOverSubscription(peList),
          new ContainerRamProvisionerSimple(VM_RAM[type]),
          new ContainerBwProvisionerSimple(VM_BW),
          peList, SCHEDULING_INTERVAL));
    }
  }

  private ContainerDatacenter createDatacenter(String name,
                                               ContainerVmAllocationPolicy vmAllocationPolicy,
                                               ContainerAllocationPolicy containerAllocationPolicy,
                                               String experName, double schedulingInterval, String logAddress,
                                               double vmStartupDelay, double containerStartupDelay) throws Exception {
    String arch = "x86";
    String os = "Linux";
    String vmm = "Xen";
    double timeZone = 8.0D;
    double cost = 3.0D;
    double costPerMem = 0.05D;
    double costPerStorage = 0.001D;
    double costPerBw = 0.0D;
    ContainerDatacenterCharacteristics characteristics =
        new ContainerDatacenterCharacteristics(arch, os, vmm, hostList, timeZone,
            cost, costPerMem, costPerStorage, costPerBw);
    ContainerDatacenter datacenter = new PowerContainerDatacenterCM(name, characteristics, vmAllocationPolicy,
        containerAllocationPolicy, new LinkedList<>(), schedulingInterval, experName,
        logAddress, vmStartupDelay, containerStartupDelay);

    return datacenter;
  }

  private String getExperName(String... args) {
    StringBuilder sbd = new StringBuilder();
    for (int i = 0; i < args.length; i++) {
      if (!args[i].isEmpty()) {
        if (i != 0) {
          sbd.append("_");
        }
        sbd.append(args[i]);
      }
    }
    return sbd.toString();
  }

  private void printCloudletList(List<ContainerCloudlet> list) {
    Cloudlet cloudlet;

    String indent = "    ";
    Log.printLine();
    Log.printLine("========== OUTPUT ==========");
    Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
        + "Data center ID" + indent + "VM ID" + indent + "Time" + indent
        + "Start Time" + indent + "Finish Time");

    DecimalFormat dft = new DecimalFormat("###.##");
    for (ContainerCloudlet cloudlet1 : list) {
      cloudlet = cloudlet1;
      Log.print(indent + cloudlet.getCloudletId() + indent + indent);

      if (cloudlet.getCloudletStatusString().equals("Success")) {
        Log.print("SUCCESS");

        Log.printLine(indent + indent + cloudlet.getResourceId()
            + indent + indent + indent + cloudlet.getVmId()
            + indent + indent
            + dft.format(cloudlet.getActualCPUTime()) + indent
            + indent + dft.format(cloudlet.getExecStartTime())
            + indent + indent
            + dft.format(cloudlet.getFinishTime()));
      }
    }
  }
}
