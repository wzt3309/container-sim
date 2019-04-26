package paperlab.ccsim.main;

import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G4Xeon3040;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G5Xeon3075;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerIbmX3550XeonX5670;

public final class Settings {

    /**
     * 仿真过程整设置
     */
    public static final boolean ENABLE_OUTPUT = true;
    public static final boolean OUTPUT_CSV = false;
    public static final double SCHEDULING_INTERVAL = 300.0D;
    public static final double SIMULATION_LIMIT = 87400.0D;

    /**
     * 容器和虚拟机启动时间设置
     */
    public static final double CONTAINER_STARTTUP_DELAY = 0.4;//in seconds
    public static final double VM_STARTTUP_DELAY = 100;//in seconds

    /**
     * Cloudlet设置
     */
    public static final int CLOUDLET_LENGTH = 30;
    public static final int CLOUDLET_PES = 1;

    /**
     * 容器配置
     */
    public static final int CONTAINER_TYPES = 3;
    public static final int[] CONTAINER_MIPS = new int[]{4658, 9320, 18636};
    public static final int[] CONTAINER_PES = new int[]{1, 1, 1};
    public static final int[] CONTAINER_RAM = new int[]{128, 256, 512};
    public static final int CONTAINER_BW = 2500;

    /**
     * 虚拟机配置
     */
    public static final int VM_TYPES = 4;
    public static final double[] VM_MIPS = new double[]{37274/ 2, 37274 / 2, 37274 / 2, 37274 / 2};
    public static final int[] VM_PES = new int[]{2, 4, 1, 8};
    public static final float[] VM_RAM = new float[] {(float)1024, (float) 2048, (float) 4096, (float) 8192};//**MB*
    public static final int VM_BW = 100000;
    public static final int VM_SIZE = 2500;


    /**
     * 物理机的配置
     */
    public static final int HOST_TYPES = 3;
    public static final int[] HOST_MIPS = new int[]{37274, 37274, 37274};
    public static final int[] HOST_PES = new int[]{4, 8, 16};
    public static final int[] HOST_RAM = new int[]{65536, 131072, 262144};
    public static final int HOST_BW = 1000000;
    public static final int HOST_STORAGE = 1000000;
    public static final PowerModel[] HOST_POWER = new PowerModel[]{new PowerModelSpecPowerHpProLiantMl110G4Xeon3040(),
            new PowerModelSpecPowerHpProLiantMl110G5Xeon3075(), new PowerModelSpecPowerIbmX3550XeonX5670()};

    public static final int NUMBER_HOSTS = 20;
    public static final int NUMBER_VMS = 25;
    public static final int NUMBER_CLOUDLETS = 50;
}
