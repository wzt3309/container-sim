package paperlab.ccsim;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.text.DecimalFormat;
import java.util.*;

/**
 * An example of one datacenter with one host, one vm and one cloudlet
 */
public class ExampleOne {

  public static void main(String[] args) {
    // First: init cloudsim package
    int num_user = 1;   // num of cloud user
    Calendar cal = Calendar.getInstance();
    boolean trace_flag = false;
    CloudSim.init(num_user, cal, trace_flag);

    // Second: create a datacenter
    Datacenter datacenter0 = createDatacenter("Datacenter_0");

    // Third: create a datacenter broker
    DatacenterBroker broker = createDatacenterBroker("broker"); // acting as user to manager VMs

    // Fourth: create one vm
    int vmId = 0;
    int brokerId = broker.getId();
    List<Vm> vmList = new ArrayList<>();
    Vm vm0 = createVm(vmId, brokerId);
    vmList.add(vm0);
    //// submit vm list to broker
    broker.submitVmList(vmList);

    // Fifth: create one cloudlet
    List<Cloudlet> cloudletList = new ArrayList<>();
    int cloudletId = 0;
    Cloudlet cloudlet0 = createCloudlet(cloudletId);
    cloudlet0.setUserId(brokerId);
    cloudlet0.setVmId(vmId);
    cloudletList.add(cloudlet0);
    //// submit cloudlet list
    broker.submitCloudletList(cloudletList);

    // Sixth: starts the simulation
    CloudSim.startSimulation();

    CloudSim.stopSimulation();

    // Final: print results when simulation is over
    List<Cloudlet> newCloudletList = broker.getCloudletReceivedList();
    printCloudletList(newCloudletList);

    Log.printLine("ExampleOne finished!");
  }

  private static Datacenter createDatacenter(String name) {
    // 1. create a list to store machine
    List<Host> hostList = new ArrayList<>();

    // 2. one machine has one or more PEs(CPUs/Cores) have same MIPS
    int mips = 1000;
    List<Pe> peList = new ArrayList<>();
    //// `PeProvisionerSimple` is the provisioning policy to allocate its PEs' mips to virtual machine
    peList.add(new Pe(0, new PeProvisionerSimple(mips)));

    // 3. create host with its id and a list of pes
    int hostId = 0;
    int ram = 2048; // host memory (MB)
    int bw = 10000; // bandwidth (Mbps)
    int storage = 1_000_000; // host storage (MB)
    hostList.add(
        new Host(
            hostId,
            new RamProvisionerSimple(ram),
            new BwProvisionerSimple(bw),
            storage,
            peList,
            //// `VmScheduler` allocate PEs to vm
            //// and then the `PeProvisioner` allocate PEs' mips to vm
            new VmSchedulerTimeShared(peList)
            )
    );  // that's our machine

    String arch = "x86";
    String os = "linux";
    String vmm = "Xen";
    double time_zone = getDefaultTimeZone();  // time zone of the local date-time from UTC/Greenwich
    double cost_per_sec = 3.0; // the cost per sec of CPU use - resource pricing
    double cost_per_mem = 0.05; // the cost of using Memory - resource pricing
    double cost_per_storage = 0.001; // the cost of using Storage - resource pricing
    double cost_per_bw = 0.0; // the cost of using bandwidth - resource pricing
    List<Storage> storageList = new LinkedList<>(); // storage device in datacenter like DAS, NAS, SAN
    DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
        arch, os, vmm, hostList, time_zone, cost_per_sec, cost_per_mem, cost_per_storage, cost_per_bw
    );

    Datacenter datacenter = null;
    try {
      datacenter = new Datacenter(name, characteristics,
          new VmAllocationPolicySimple(hostList), storageList,
          /* the delay of schedule for processing datacenter received event*/0.0);
    } catch (Exception e) {
      Log.formatLine("Create an instance of datacenter failed: ", e.getMessage());
    }

    return datacenter;
  }

  private static double getDefaultTimeZone() {
   TimeZone tz = TimeZone.getDefault();
   double res = tz.getOffset(Calendar.ZONE_OFFSET);
   int hour_per_mills = 1000 * 3600;
   return  res / hour_per_mills;
  }

  private static DatacenterBroker createDatacenterBroker(String name) {
    DatacenterBroker broker = null;
    try {
      broker = new DatacenterBroker(name);
    } catch (Exception e) {
      Log.formatLine("Create an instance of datacenter broker failed: ", e.getMessage());
    }
    return broker;
  }

  private static Vm createVm(int vmId, int brokerId) {
    double mips = 1000.0;
    long size = 10000; // image size (MB)
    int ram = 512; // vm memory (MB)
    int bw = 1000; // bandwidth (Mbps)
    int numberOfPes = 1; // number of PEs (vCPU)
    String vmm = "Xen"; // VMM name

    return new Vm(vmId, brokerId, mips, numberOfPes, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
  }

  private static Cloudlet createCloudlet(int cloudletId) {
    int length = 400000; // MI size of this cloudlet
    int numberOfPes = 1;
    int fileSize = 300; // the file size (in byte) of program and data
    long outputSize = 300; // the file size (in byte) of cloudlet after execution
    UtilizationModel utilizationModel = new UtilizationModelFull();
    return new Cloudlet(cloudletId, length, numberOfPes, fileSize, outputSize,
        utilizationModel, utilizationModel, utilizationModel);
  }

  private static void printCloudletList(List<Cloudlet> list) {
    String indent = "    ";
    Log.printLine();
    Log.printLine("========== OUTPUT ==========");
    Log.formatLine("%-15s\t%-15s\t%-15s\t%-15s\t%-15s\t%-15s\t%-15s",
        "CLOUDLET ID", "STATUS", "DATACENTER ID", "VM ID", "TIME", "START TIME", "FINISH TIME");

    DecimalFormat dft = new DecimalFormat("###.##");
    for (Cloudlet cloudlet : list) {
      Log.format("%-15d", cloudlet.getCloudletId());

      if (cloudlet.getStatus() == Cloudlet.SUCCESS) {
        Log.format("%-15s", "SUCCESS");

        Log.formatLine("\t%-15d\t%-15d\t%-15s\t%-15s\t%-15s",
            cloudlet.getResourceId(), cloudlet.getVmId(), dft.format(cloudlet.getActualCPUTime()),
            dft.format(cloudlet.getExecStartTime()), dft.format(cloudlet.getFinishTime()));
      }
    }
  }

}
