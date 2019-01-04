package paperlab.ccsim.power;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.power.*;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.util.MathUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static paperlab.ccsim.power.Constants.*;

public final class Helper {

  public static List<Vm> createVmList(int brokerId, int numberOfVm) {
    List<Vm> vmList = new ArrayList<>();
    for (int i = 0; i < numberOfVm; i++) {
      int types = i % VM_TYPES;
      vmList.add(
          new PowerVm(i, brokerId,
              VM_MIPS[types],
              VM_PES[types],
              VM_RAM[types],
              VM_BW,
              VM_SIZE,
              1,  // the priority don't use
              "Xen",
              new CloudletSchedulerDynamicWorkload(VM_MIPS[types], VM_PES[types]),
              SCHEDULING_INTERVAL));
    }
    return vmList;
  }

  public static List<PowerHost> createHostList(int numberOfHost) {
    List<PowerHost> hostList = new ArrayList<>();
    for (int i = 0; i < numberOfHost; i++) {
      int type = i % HOST_TYPES;

      List<Pe> peList = new ArrayList<>();
      for (int j = 0; j < HOST_PES[type]; j++) {
        peList.add(new Pe(j, new PeProvisionerSimple(HOST_MIPS[type])));
      }

      hostList.add(new PowerHost(i,
          new RamProvisionerSimple(HOST_RAM[type]),
          new BwProvisionerSimple(HOST_BW),
          HOST_STORAGE,
          peList,
          new VmSchedulerTimeSharedOverSubscription(peList),
          HOST_POWER[type]));
    }

    return hostList;
  }

  public static DatacenterBroker createBroker() {
    DatacenterBroker broker = null;
    try {
      broker = new PowerDatacenterBroker("Broker");
    } catch (Exception e) {
      Log.formatLine("Create an instance of datacenter broker failed: ", e.getMessage());
      System.exit(1);
    }
    return broker;
  }

  public static Datacenter createDatacenter(
      String name,
      Class<? extends Datacenter> datacenterCLass,
      List<PowerHost> hostList,
      VmAllocationPolicy vmAllocationPolicy) {
    String arch = "x86";
    String os = "Linux";
    String vmm = "Xen";
    double timezone = getTimeZone();
    double costPerSec = 3.0;
    double costPerMem = 0.05;
    double costPerStorage = 0.001;
    double costPerBw = 0.0;
    DatacenterCharacteristics datacenterCharacteristics = new DatacenterCharacteristics(
        arch,
        os,
        vmm,
        hostList,
        timezone,
        costPerSec,
        costPerMem,
        costPerStorage,
        costPerBw);

    Datacenter datacenter = null;
    try {
      datacenter = datacenterCLass.getConstructor(
          String.class,
          DatacenterCharacteristics.class,
          VmAllocationPolicy.class,
          List.class, Double.TYPE).newInstance(name,
          datacenterCharacteristics,
          vmAllocationPolicy,
          new LinkedList<Storage>(),
          SCHEDULING_INTERVAL);
    } catch (Exception e) {
      Log.formatLine("Create an instance of datacenter failed: ", e.getMessage());
    }
    return datacenter;
  }

  private static double getTimeZone() {
    TimeZone tz = TimeZone.getDefault();
    double offset = tz.getOffset(Calendar.ZONE_OFFSET);
    double HOUR_PER_MILLS = 3600.0 * 1000.0;
    return offset / HOUR_PER_MILLS;
  }

  public static void printResult(
      PowerDatacenter datacenter,
      List<Vm> vmList,
      double lastClock,
      String experimentName,
      boolean outputInCsv,
      String outputFolder) throws IOException {
    Log.enable();
    List<Host> hostList = datacenter.getHostList();

    int numberOfHost = hostList.size();
    int numberOfVm = vmList.size();

    double totalSimulationTime = lastClock;
    double energy = datacenter.getPower() / (3600 * 1000);  // kw*h
    int numberOfMigrations = datacenter.getMigrationCount();

    Map<String, Double> slaMetrics = getSlaMetrics(vmList);

    double slaOverall = slaMetrics.get("overall");
    double slaAverage = slaMetrics.get("average");
    // 因为迁移而发生的sla违反率
    double slaUnderAllocatedMigration = slaMetrics.get("underallocated_migration");

    // 每台host发生sla的时间
    double slaTimePerActiveHost = getSlaTimePerActiveHost(hostList);
    double sla = slaTimePerActiveHost * slaUnderAllocatedMigration;

    List<Double> timeBeforeHostShutdown = getTimeBeforeHostShutdown(hostList);

    int numberOfHostShutdown = timeBeforeHostShutdown.size();

    double meanTimeBeforeHostShutdown = Double.NaN;
    double stDevTimeBeforeHostShutdown = Double.NaN;
    if (!timeBeforeHostShutdown.isEmpty()) {
      meanTimeBeforeHostShutdown = MathUtil.mean(timeBeforeHostShutdown);
      stDevTimeBeforeHostShutdown = MathUtil.stDev(timeBeforeHostShutdown);
    }

    List<Double> timeBeforeVmMigration = getTimeBeforeVmMigration(vmList);
    double meanTimeBeforeVmMigration = Double.NaN;
    double stDevTimeBeforeVmMigration = Double.NaN;
    if (!timeBeforeHostShutdown.isEmpty()) {
      meanTimeBeforeHostShutdown = MathUtil.mean(timeBeforeVmMigration);
      stDevTimeBeforeVmMigration = MathUtil.stDev(timeBeforeVmMigration);
    }

    if (outputInCsv) {
      Path root = Paths.get(outputFolder);
      if (Files.notExists(root, NOFOLLOW_LINKS)) {
        Files.createDirectories(root);
      }

      Path stats = root.resolve("stats");
      if (Files.notExists(stats, NOFOLLOW_LINKS)) {
        Files.createDirectories(stats);
      }

      Path time_before_host_shutdown = root.resolve("time_before_host_shutdown");
      if (Files.notExists(time_before_host_shutdown)) {
        Files.createDirectories(time_before_host_shutdown);
      }

      Path time_before_vm_migration = root.resolve("time_before_vm_migration");
      if (Files.notExists(time_before_vm_migration)) {
        Files.createDirectories(time_before_vm_migration);
      }

      Path metrics = root.resolve("metrics");
      if (Files.notExists(metrics)) {
        Files.createDirectories(metrics);
      }

      StringBuilder data = new StringBuilder();
      String delimeter = ",";
      data.append(experimentName).append(delimeter);
      data.append(parseExperimentName(experimentName));
      data.append(String.format("%d", numberOfHost)).append(delimeter);
      data.append(String.format("%d", numberOfVm)).append(delimeter);
      data.append(String.format("%.2f", totalSimulationTime)).append(delimeter);
      data.append(String.format("%.5f", energy)).append(delimeter);
      data.append(String.format("%d", numberOfMigrations)).append(delimeter);
      data.append(String.format("%.10f", sla)).append(delimeter);
      data.append(String.format("%.10f", slaTimePerActiveHost)).append(delimeter);
      data.append(String.format("%.10f", slaUnderAllocatedMigration)).append(delimeter);
      data.append(String.format("%.10f", slaOverall)).append(delimeter);
      data.append(String.format("%.10f", slaAverage)).append(delimeter);
      data.append(String.format("%d", numberOfHostShutdown)).append(delimeter);
      data.append(String.format("%.2f", meanTimeBeforeHostShutdown)).append(delimeter);
      data.append(String.format("%.2f", stDevTimeBeforeHostShutdown)).append(delimeter);
      data.append(String.format("%.2f", meanTimeBeforeVmMigration)).append(delimeter);
      data.append(String.format("%.2f", stDevTimeBeforeVmMigration)).append(delimeter);

      if (datacenter.getVmAllocationPolicy() instanceof PowerVmAllocationPolicyMigrationAbstract) {
        PowerVmAllocationPolicyMigrationAbstract vmAllocationPolicy = (PowerVmAllocationPolicyMigrationAbstract) datacenter
            .getVmAllocationPolicy();

        double executionTimeVmSelectionMean = MathUtil.mean(vmAllocationPolicy
            .getExecutionTimeHistoryVmSelection());
        double executionTimeVmSelectionStDev = MathUtil.stDev(vmAllocationPolicy
            .getExecutionTimeHistoryVmSelection());
        double executionTimeHostSelectionMean = MathUtil.mean(vmAllocationPolicy
            .getExecutionTimeHistoryHostSelection());
        double executionTimeHostSelectionStDev = MathUtil.stDev(vmAllocationPolicy
            .getExecutionTimeHistoryHostSelection());
        double executionTimeVmReallocationMean = MathUtil.mean(vmAllocationPolicy
            .getExecutionTimeHistoryVmReallocation());
        double executionTimeVmReallocationStDev = MathUtil.stDev(vmAllocationPolicy
            .getExecutionTimeHistoryVmReallocation());
        double executionTimeTotalMean = MathUtil.mean(vmAllocationPolicy
            .getExecutionTimeHistoryTotal());
        double executionTimeTotalStDev = MathUtil.stDev(vmAllocationPolicy
            .getExecutionTimeHistoryTotal());

        data.append(String.format("%.5f", executionTimeVmSelectionMean)).append(delimeter);
        data.append(String.format("%.5f", executionTimeVmSelectionStDev)).append(delimeter);
        data.append(String.format("%.5f", executionTimeHostSelectionMean)).append(delimeter);
        data.append(String.format("%.5f", executionTimeHostSelectionStDev)).append(delimeter);
        data.append(String.format("%.5f", executionTimeVmReallocationMean)).append(delimeter);
        data.append(String.format("%.5f", executionTimeVmReallocationStDev)).append(delimeter);
        data.append(String.format("%.5f", executionTimeTotalMean)).append(delimeter);
        data.append(String.format("%.5f", executionTimeTotalStDev)).append(delimeter);

        writeMetricHistory(hostList, vmAllocationPolicy, outputFolder + "/metrics/" + experimentName
            + "_metric");
      }

      data.append("\n");

      writeDataRow(data.toString(), outputFolder + "/stats/" + experimentName + "_stats.csv");
      writeDataColumn(timeBeforeHostShutdown, outputFolder + "/time_before_host_shutdown/"
          + experimentName + "_time_before_host_shutdown.csv");
      writeDataColumn(timeBeforeVmMigration, outputFolder + "/time_before_vm_migration/"
          + experimentName + "_time_before_vm_migration.csv");
    }else {
      Log.setDisabled(false);
      Log.printLine();
      Log.printLine("Experiment name: " + experimentName);
      Log.printLine("Number of hosts: " + numberOfHost);
      Log.printLine("Number of VMs: " + numberOfVm);
      Log.printLine(String.format("Total simulation time: %.2f sec", totalSimulationTime));
      Log.printLine(String.format("Energy consumption: %.2f kWh", energy));
      Log.printLine(String.format("Number of VM migrations: %d", numberOfMigrations));
      Log.printLine(String.format("SLA: %.5f%%", sla * 100));
      Log.printLine(String.format(
          "SLA perf degradation due to migration: %.2f%%",
          slaUnderAllocatedMigration * 100));
      Log.printLine(String.format("SLA time per active host: %.2f%%", slaTimePerActiveHost * 100));
      Log.printLine(String.format("Overall SLA violation: %.2f%%", slaOverall * 100));
      Log.printLine(String.format("Average SLA violation: %.2f%%", slaAverage * 100));
      // Log.printLine(String.format("SLA time per VM with migration: %.2f%%",
      // slaTimePerVmWithMigration * 100));
      // Log.printLine(String.format("SLA time per VM without migration: %.2f%%",
      // slaTimePerVmWithoutMigration * 100));
      // Log.printLine(String.format("SLA time per host: %.2f%%", slaTimePerHost * 100));
      Log.printLine(String.format("Number of host shutdowns: %d", numberOfHostShutdown));
      Log.printLine(String.format(
          "Mean time before a host shutdown: %.2f sec",
          meanTimeBeforeHostShutdown));
      Log.printLine(String.format(
          "StDev time before a host shutdown: %.2f sec",
          stDevTimeBeforeHostShutdown));
      Log.printLine(String.format(
          "Mean time before a VM migration: %.2f sec",
          meanTimeBeforeVmMigration));
      Log.printLine(String.format(
          "StDev time before a VM migration: %.2f sec",
          stDevTimeBeforeVmMigration));

      if (datacenter.getVmAllocationPolicy() instanceof PowerVmAllocationPolicyMigrationAbstract) {
        PowerVmAllocationPolicyMigrationAbstract vmAllocationPolicy = (PowerVmAllocationPolicyMigrationAbstract) datacenter
            .getVmAllocationPolicy();

        double executionTimeVmSelectionMean = MathUtil.mean(vmAllocationPolicy
            .getExecutionTimeHistoryVmSelection());
        double executionTimeVmSelectionStDev = MathUtil.stDev(vmAllocationPolicy
            .getExecutionTimeHistoryVmSelection());
        double executionTimeHostSelectionMean = MathUtil.mean(vmAllocationPolicy
            .getExecutionTimeHistoryHostSelection());
        double executionTimeHostSelectionStDev = MathUtil.stDev(vmAllocationPolicy
            .getExecutionTimeHistoryHostSelection());
        double executionTimeVmReallocationMean = MathUtil.mean(vmAllocationPolicy
            .getExecutionTimeHistoryVmReallocation());
        double executionTimeVmReallocationStDev = MathUtil.stDev(vmAllocationPolicy
            .getExecutionTimeHistoryVmReallocation());
        double executionTimeTotalMean = MathUtil.mean(vmAllocationPolicy
            .getExecutionTimeHistoryTotal());
        double executionTimeTotalStDev = MathUtil.stDev(vmAllocationPolicy
            .getExecutionTimeHistoryTotal());

        Log.printLine(String.format(
            "Execution time - VM selection mean: %.5f sec",
            executionTimeVmSelectionMean));
        Log.printLine(String.format(
            "Execution time - VM selection stDev: %.5f sec",
            executionTimeVmSelectionStDev));
        Log.printLine(String.format(
            "Execution time - host selection mean: %.5f sec",
            executionTimeHostSelectionMean));
        Log.printLine(String.format(
            "Execution time - host selection stDev: %.5f sec",
            executionTimeHostSelectionStDev));
        Log.printLine(String.format(
            "Execution time - VM reallocation mean: %.5f sec",
            executionTimeVmReallocationMean));
        Log.printLine(String.format(
            "Execution time - VM reallocation stDev: %.5f sec",
            executionTimeVmReallocationStDev));
        Log.printLine(String.format("Execution time - total mean: %.5f sec", executionTimeTotalMean));
        Log.printLine(String
            .format("Execution time - total stDev: %.5f sec", executionTimeTotalStDev));
      }
      Log.printLine();
    }

    Log.setDisabled(true);
  }

  private static String parseExperimentName(String name) {
    Scanner scanner = new Scanner(name);
    StringBuilder csvName = new StringBuilder();
    scanner.useDelimiter("_");
    for (int i = 0; i < 4; i++) {
      if (scanner.hasNext()) {
        csvName.append(scanner.next()).append(",");
      } else {
        csvName.append(",");
      }
    }
    scanner.close();
    return csvName.toString();
  }

  private static Map<String, Double> getSlaMetrics(List<Vm> vmList) {
    Map<String, Double> metrics = new HashMap<>();
    List<Double> slaViolation = new LinkedList<>();
    double totalAllocated = 0;
    double totalRequested = 0;
    double totalUnderAllocatedDueToMigration = 0;

    for (Vm vm: vmList) {
      double vmTotalAllocated = 0;
      double vmTotalRequested = 0;
      double vmTotalUnderAllocatedDueToMigration = 0;
      double previousTime = -1;
      double previousAllocated = 0;
      double previousRequested = 0;
      boolean previousIsInMigration = false;

      for (VmStateHistoryEntry entry: vm.getStateHistory()) {
        if (previousTime != -1) {
          double timeDiff = entry.getTime() - previousTime;
          vmTotalAllocated += timeDiff * previousAllocated; // 一种积分的过程，仔这个时间段内分配的总量
          vmTotalRequested += timeDiff * previousRequested;

          if (previousRequested < previousAllocated) {
            slaViolation.add((previousRequested - previousAllocated) / previousAllocated);
            if (previousIsInMigration) {
              vmTotalUnderAllocatedDueToMigration += (previousRequested - previousAllocated)
                  * timeDiff;
            }
          }
        }

        previousAllocated = entry.getAllocatedMips();
        previousRequested = entry.getRequestedMips();
        previousIsInMigration = entry.isInMigration();
        previousTime = entry.getTime();
      }

      totalAllocated += vmTotalAllocated;
      totalRequested += vmTotalRequested;
      totalUnderAllocatedDueToMigration += vmTotalUnderAllocatedDueToMigration;
    }

    metrics.put("overall", (totalRequested - totalAllocated) / totalRequested);
    if (slaViolation.isEmpty()) {
      metrics.put("average", 0.0);
    } else {
      metrics.put("average", MathUtil.mean(slaViolation));
    }
    metrics.put("underallocated_migration", totalUnderAllocatedDueToMigration / totalRequested);

    return metrics;
  }

  private static double getSlaTimePerActiveHost(List<Host> hosts) {
    double totalTime = 0;
    double slaTime = 0;

    for (Host _host: hosts) {
      double previousRequested = 0;
      double previousAllocated = 0;
      double previousTime = -1;
      boolean isActive = false;

      HostDynamicWorkload host = (HostDynamicWorkload) _host;
      for (HostStateHistoryEntry entry: host.getStateHistory()) {
        if (previousTime != -1 && isActive) {
          totalTime += entry.getTime() - previousTime;
          if (previousAllocated < previousRequested) {
            slaTime += entry.getTime() - previousTime;
          }
        }

        previousRequested = entry.getRequestedMips();
        previousAllocated = entry.getAllocatedMips();
        previousTime = entry.getTime();
        isActive = entry.isActive();
      }
    }

    return slaTime / totalTime;
  }

  private static List<Double> getTimeBeforeHostShutdown(List<Host> hosts) {
    List<Double>  timeBeforeHostShutdown = new LinkedList<>();

    for (Host _host: hosts) {
      boolean previousIsActive = true;
      double lastActiveTime = -1;

      HostDynamicWorkload host = (HostDynamicWorkload) _host;
      for (HostStateHistoryEntry entry: host.getStateHistory()) {
        if (previousIsActive && !entry.isActive()) {
          timeBeforeHostShutdown.add(entry.getTime() - lastActiveTime);
        }
        if (!previousIsActive && entry.isActive()) {
          lastActiveTime = entry.getTime();
        }
        previousIsActive = entry.isActive();
      }
    }
    return timeBeforeHostShutdown;
  }

  private static List<Double> getTimeBeforeVmMigration(List<Vm> vmList) {
    List<Double> timeBeforeVmMigration = new LinkedList<>();
    for (Vm vm: vmList) {
      double previousMigrationTime = 0;
      boolean previousIsMigrated = false;
      for (VmStateHistoryEntry entry: vm.getStateHistory()) {
        if (previousIsMigrated && !entry.isInMigration()) {
          timeBeforeVmMigration.add(entry.getTime() - previousMigrationTime);
        }
        if (!previousIsMigrated && entry.isInMigration()) {
          previousMigrationTime = entry.getTime();
        }
        previousIsMigrated = entry.isInMigration();
      }
    }
    return timeBeforeVmMigration;
  }

  private static void writeMetricHistory(
      List<? extends Host> hosts,
      PowerVmAllocationPolicyMigrationAbstract vmAllocationPolicy,
      String outputPath) {
    // for (Host host : hosts) {
    for (int j = 0; j < 10; j++) {
      Host host = hosts.get(j);

      if (!vmAllocationPolicy.getTimeHistory().containsKey(host.getId())) {
        continue;
      }
      File file = new File(outputPath + "_" + host.getId() + ".csv");
      try {
        file.createNewFile();
      } catch (IOException e1) {
        e1.printStackTrace();
        System.exit(0);
      }
      try {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        List<Double> timeData = vmAllocationPolicy.getTimeHistory().get(host.getId());
        List<Double> utilizationData = vmAllocationPolicy.getUtilizationHistory().get(host.getId());
        List<Double> metricData = vmAllocationPolicy.getMetricHistory().get(host.getId());

        for (int i = 0; i < timeData.size(); i++) {
          writer.write(String.format(
              "%.2f,%.2f,%.2f\n",
              timeData.get(i),
              utilizationData.get(i),
              metricData.get(i)));
        }
        writer.close();
      } catch (IOException e) {
        e.printStackTrace();
        System.exit(0);
      }
    }
  }

  private static void writeDataColumn(List<? extends Number> data, String outputPath) {
    File file = new File(outputPath);
    try {
      file.createNewFile();
    } catch (IOException e1) {
      e1.printStackTrace();
      System.exit(0);
    }
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(file));
      for (Number value : data) {
        writer.write(value.toString() + "\n");
      }
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(0);
    }
  }

  /**
   * Write data row.
   *
   * @param data the data
   * @param outputPath the output path
   */
  private static void writeDataRow(String data, String outputPath) {
    File file = new File(outputPath);
    try {
      file.createNewFile();
    } catch (IOException e1) {
      e1.printStackTrace();
      System.exit(0);
    }
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(file));
      writer.write(data);
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(0);
    }
  }
}
