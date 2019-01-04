package paperlab.ccsim.power;

import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G4Xeon3040;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G5Xeon3075;

public final class Constants {
  private Constants() {}

  public static final double SCHEDULING_INTERVAL = 300;
  public static final double SIMULATION_LIMIT = 24 * 60 * 60;

  public final static int CLOUDLET_LENGTH	= 2500 * (int) SIMULATION_LIMIT;
  public final static int CLOUDLET_PES	= 1;

  public final static int VM_TYPES = 4;
  public final static int[] VM_MIPS = { 2500, 2000, 1000, 500 };
  public final static int[] VM_PES = { 1, 1, 1, 1 };
  public final static int[] VM_RAM = { 870, 1740, 1740, 613 };
  public final static int VM_BW = 100_000; // 100Mbps
  public final static int VM_SIZE = 2500; // 2.5GB

  public final static int HOST_TYPES = 2;
  public final static int[] HOST_MIPS = { 1860, 2660 };
  public final static int[] HOST_PES = { 2, 2 };
  public final static int[] HOST_RAM = { 4096, 4096 };
  public final static int HOST_BW = 1_000_000; // 1Gbps
  public final static int HOST_STORAGE = 1_000_000; //
  public final static PowerModel[] HOST_POWER = {
      new PowerModelSpecPowerHpProLiantMl110G4Xeon3040(),
      new PowerModelSpecPowerHpProLiantMl110G5Xeon3075(),
  };
}
