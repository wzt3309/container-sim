package paperlab.ccsim.example;

import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G4Xeon3040;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G5Xeon3075;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerIbmX3550XeonX5670;

final class Constants {
  private Constants() {}

  static final double SCHEDULING_INTERVAL = 300.0D;
  static final double SCHEDULING_LIMIT = 87400.0D;

  static final int CLOUDLET_LENGTH = 3000;

  static final double CONTAINER_STARTUP_DELAY = 0.4; // in sec
  static final double VM_STARTUP_DELAY = 100; // in sec

  static final int HOST_TYPES = 3;
  static final int[] HOST_MIPS = {37274, 37274, 37274};
  static final int[] HOST_PES = {2, 4, 8};
  static final int[] HOST_RAM = {65536, 131072, 262144};
  static final int HOST_BW = 1_000_000;
  static final long HOST_STORAGE = 1_000_000;
  static final PowerModel[] HOST_POWER = {
      new PowerModelSpecPowerHpProLiantMl110G4Xeon3040(),
      new PowerModelSpecPowerHpProLiantMl110G5Xeon3075(),
      new PowerModelSpecPowerIbmX3550XeonX5670(),
  };

  static final int VM_TYPES = 4;
  static final double[] VM_MIPS = {3727, 3727.0, 3787.0, 5080.0};
  static final int[] VM_PES = {1, 1, 1, 1};
  static final float[] VM_RAM = {1024F, 2048F, 4096F, 8192F};
  static final int VM_BW = 100_000;
  static final int VM_SIZE = 2500;

  static final int CONTAINER_TYPES = 3;
  static final int[] CONTAINER_MIPS = {800, 900, 700};
  static final int[] CONTAINER_PES = {1, 1, 1};
  static final int[] CONTAINER_RAM = {1, 2, 10};
  static final int CONTAINER_BW = 1;

  static final int NUMBER_HOST = 3;
  static final int NUMBER_VM = 10;
  static final int NUMBER_CLOUDLET = 100;

}
