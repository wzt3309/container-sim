package paperlab.ccsim.power.random;

public class Dvfs {
  public static void main(String[] args) {
    boolean enableOutput = true;
    boolean outputToFile = true;
    String inputFolder = "";
    String outputFolder = "D:\\tmp\\cloudSim";
    String workload = "random";
    String vmAllocationPolicy = "dvfs";
    String vmSelectionPolicy = "";
    String parameter = "";

    new RandomRunner(
        enableOutput,
        outputToFile,
        inputFolder,
        outputFolder,
        workload,
        vmAllocationPolicy,
        vmSelectionPolicy,
        parameter);
  }
}
