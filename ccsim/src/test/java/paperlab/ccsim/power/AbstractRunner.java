package paperlab.ccsim.power;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardOpenOption.*;
import static java.nio.file.attribute.PosixFilePermissions.asFileAttribute;
import static java.nio.file.attribute.PosixFilePermissions.fromString;
import static paperlab.ccsim.power.Constants.SIMULATION_LIMIT;
import static paperlab.ccsim.power.Helper.createDatacenter;

/**
 * Test {@link org.cloudbus.cloudsim.examples.power.RunnerAbstract}
 */
public abstract class AbstractRunner {
  private boolean enableOutput;
  protected DatacenterBroker broker;
  protected List<Cloudlet> cloudletList;
  protected List<Vm> vmList;
  protected List<PowerHost> hostList;

  public AbstractRunner(boolean enableOutput,
                        boolean outputToFile,
                        String inputFolder,
                        String outputFolder,
                        String workload,
                        String vmAllocationPolicy,
                        String vmSelectionPolicy,
                        String parameter) {
    try {
      initLogOutput(
          enableOutput,
          outputToFile,
          outputFolder,
          workload,
          vmAllocationPolicy,
          vmSelectionPolicy,
          parameter);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(0);
    }

    init(inputFolder + "/" + workload);
    start(getExperimentName(workload, vmAllocationPolicy, vmSelectionPolicy, parameter),
        outputFolder,
        getVmAllocationPolicy(vmAllocationPolicy, vmSelectionPolicy, parameter));
  }

  protected void initLogOutput(
      boolean enableOutput,
      boolean outputToFile,
      String outputFolder,
      String workload,
      String vmAllocationPolicy,
      String vmSelectionPolicy,
      String parameter) throws IOException {
    setEnableOutput(enableOutput);
    Log.setDisabled(!isEnableOutput());
    if (isEnableOutput() && outputToFile) {
      Path root = Paths.get(outputFolder);
      if (!Files.exists(root, NOFOLLOW_LINKS)) {
        Files.createDirectories(root);
      }

      Path log = root.resolve("log");
      if (!Files.exists(log)) {
        Files.createDirectory(log);
      }

      Path logFile = log.resolve(getExperimentName(
          workload,
          vmAllocationPolicy,
          vmSelectionPolicy,
          parameter,
          ".txt"));
      Log.setOutput(Files.newOutputStream(logFile));
    }
  }

  protected VmAllocationPolicy getVmAllocationPolicy(
      String vmAllocationPolicyName,
      String vmSelectionPolicyName,
      String parameterName) {
    VmAllocationPolicy vmAllocationPolicy = null;
    PowerVmSelectionPolicy vmSelectionPolicy = null;
    if (!vmSelectionPolicyName.isEmpty()) {
      vmSelectionPolicy = getVmSelectionPolicy(vmSelectionPolicyName);
    }
    double parameter = 0;
    if (!parameterName.isEmpty()) {
      parameter = Double.valueOf(parameterName);
    }
    switch (vmAllocationPolicyName) {
      case "iqr": {
        PowerVmAllocationPolicyMigrationAbstract fallbackVmSelectionPolicy = new PowerVmAllocationPolicyMigrationStaticThreshold(
            hostList,
            vmSelectionPolicy,
            0.7);
        vmAllocationPolicy = new PowerVmAllocationPolicyMigrationInterQuartileRange(
            hostList,
            vmSelectionPolicy,
            parameter,
            fallbackVmSelectionPolicy);
        break;
      }
      case "mad": {
        PowerVmAllocationPolicyMigrationAbstract fallbackVmSelectionPolicy = new PowerVmAllocationPolicyMigrationStaticThreshold(
            hostList,
            vmSelectionPolicy,
            0.7);
        vmAllocationPolicy = new PowerVmAllocationPolicyMigrationMedianAbsoluteDeviation(
            hostList,
            vmSelectionPolicy,
            parameter,
            fallbackVmSelectionPolicy);
        break;
      }
      case "lr": {
        PowerVmAllocationPolicyMigrationAbstract fallbackVmSelectionPolicy = new PowerVmAllocationPolicyMigrationStaticThreshold(
            hostList,
            vmSelectionPolicy,
            0.7);
        vmAllocationPolicy = new PowerVmAllocationPolicyMigrationLocalRegression(
            hostList,
            vmSelectionPolicy,
            parameter,
            org.cloudbus.cloudsim.examples.power.Constants.SCHEDULING_INTERVAL,
            fallbackVmSelectionPolicy);
        break;
      }
      case "lrr": {
        PowerVmAllocationPolicyMigrationAbstract fallbackVmSelectionPolicy = new PowerVmAllocationPolicyMigrationStaticThreshold(
            hostList,
            vmSelectionPolicy,
            0.7);
        vmAllocationPolicy = new PowerVmAllocationPolicyMigrationLocalRegressionRobust(
            hostList,
            vmSelectionPolicy,
            parameter,
            org.cloudbus.cloudsim.examples.power.Constants.SCHEDULING_INTERVAL,
            fallbackVmSelectionPolicy);
        break;
      }
      case "thr":
        vmAllocationPolicy = new PowerVmAllocationPolicyMigrationStaticThreshold(
            hostList,
            vmSelectionPolicy,
            parameter);
        break;
      case "dvfs":
        vmAllocationPolicy = new PowerVmAllocationPolicySimple(hostList);
        break;
      default:
        System.out.println("Unknown VM allocation policy: " + vmAllocationPolicyName);
        System.exit(0);
    }
    return vmAllocationPolicy;
  }

  protected PowerVmSelectionPolicy getVmSelectionPolicy(String vmSelectionPolicyName) {
    PowerVmSelectionPolicy vmSelectionPolicy = null;
    if (vmSelectionPolicyName.equals("mc")) {
      vmSelectionPolicy = new PowerVmSelectionPolicyMaximumCorrelation(
          new PowerVmSelectionPolicyMinimumMigrationTime());
    } else if (vmSelectionPolicyName.equals("mmt")) {
      vmSelectionPolicy = new PowerVmSelectionPolicyMinimumMigrationTime();
    } else if (vmSelectionPolicyName.equals("mu")) {
      vmSelectionPolicy = new PowerVmSelectionPolicyMinimumUtilization();
    } else if (vmSelectionPolicyName.equals("rs")) {
      vmSelectionPolicy = new PowerVmSelectionPolicyRandomSelection();
    } else {
      System.out.println("Unknown VM selection policy: " + vmSelectionPolicyName);
      System.exit(0);
    }
    return vmSelectionPolicy;
  }

  protected void start(String experimentName, String outputFolder, VmAllocationPolicy vmAllocationPolicy) {
    System.out.println("Starting " + experimentName);
    PowerDatacenter datacenter = (PowerDatacenter) createDatacenter(
        "datacenter",
        PowerDatacenter.class,
        hostList,
        vmAllocationPolicy);

    datacenter.setDisableMigrations(false);

    broker.submitVmList(vmList);
    broker.submitCloudletList(cloudletList);

    CloudSim.terminateSimulation(SIMULATION_LIMIT);
    double lastClock = CloudSim.startSimulation();

    List<Cloudlet> newList = broker.getCloudletReceivedList();
    Log.printLine("Received " + newList.size() + " cloudlets");

    CloudSim.stopSimulation();

    try {
      Helper.printResult(
          datacenter,
          vmList,
          lastClock,
          experimentName,
          true,
          outputFolder);
    } catch (IOException e) {
      e.printStackTrace();
      Log.printLine("The simulation has been terminated due to an unexpected error");
      System.exit(0);
    } finally {
      Log.printLine("Finished " + experimentName);
    }
  }

  protected abstract void init(String inputFolder);

  private String getExperimentName(String... args) {
    return String.join("_", args);
  }

  private void setEnableOutput(boolean enableOutput) {
    this.enableOutput = enableOutput;
  }

  private boolean isEnableOutput() {
    return this.enableOutput;
  }
}
