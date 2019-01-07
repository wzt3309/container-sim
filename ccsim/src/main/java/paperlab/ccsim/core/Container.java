package paperlab.ccsim.core;

import org.cloudbus.cloudsim.VmStateHistoryEntry;
import paperlab.ccsim.scheduler.ContainerCloudletScheduler;

import java.util.LinkedList;
import java.util.List;

/**
 * 在CloudSim中{@link org.cloudbus.cloudsim.container.core.Container}是作为虚拟机中的
 * 虚拟机，需要预先定义分配的资源空间，比如对一个mips为1000的单Pe虚拟机来说只能分配5个200mips的单Pe的容器，
 * 这显然与真实情况不符合的，容器对资源的占用应该是基于运行状态动态分配的
 *
 * 此处抽象container为资源隔离的空间，先将cloudlet分配给相应的container，使每个container会产生负载，再将
 * container分配给vm
 */
public class Container {
  // 容器id
  private int id;
  // 容器使用者的id
  private int userId;
  // 容器的uid
  private String uid;
  // 容器所在的vm
  private VM vm;
  // 容器中用来调度cloudlet运行的组件
  private ContainerCloudletScheduler containerCloudletScheduler;

  //------------------------------ container 状态 ------------------------------
  // 容器占用的文件系统大小
  private long size;
  // 容器总的mips数量，这里是为转换容器的实际cpu使用率到mips
  // 在实际测量中，我们只能获取容器的cpu使用率，为了标定容器当前mips使用量
  // 我们需要先设定当容器使用率为100%时，它的mips量为MIPS，这样我们就可以
  // 估计出当cpu使用率为u的时候mips量为u*MIPS
  private double mips;
  // 容器实际工作中的mips量：u*MIPS，其中u为测量到的cpu使用量
  // 其中cpu使用率是由cloudlet决定的
  private double workloadMips;
  // 容器工作中需要的处理器数量
  private int numberOfPer;
  // 容器需要的内存量
  private long ram;
  // 容器需要的带宽量
  private long bw;
  // 容器调度延迟
  private double schedulingInterval;
  // 容器是否处于迁移状态
  private boolean isInMigration;
  // 容器是否处于实例化过程中
  private boolean isInInstantiation;
  private long currentAllocatedSize;
  // 当前容器分配到的内存
  private float currentAllocatedRam;
  // 当前容器分配到的带宽
  private long currentAllocatedBw;

  //------------------------------ 历史记录 ------------------------------
  private final List<VmStateHistoryEntry> vmStateHistoryEntryList = new LinkedList<>();
  private static final int HISTORY_LENGTH = 30;

  public Container(int id,
                   int userId,
                   long size,
                   double mips,
                   int numberOfPer,
                   long ram,
                   long bw,
                   ContainerCloudletScheduler containerCloudletScheduler,
                   double schedulingInterval) {
    this.id = id;
    this.userId = userId;
    this.uid = getUid(userId, id);
    this.size = size;
    this.mips = mips;
    this.numberOfPer = numberOfPer;
    this.ram = ram;
    this.bw = bw;
    this.containerCloudletScheduler = containerCloudletScheduler;
    this.schedulingInterval = schedulingInterval;
  }

  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  public static String getUid(int userId, int containerId) {
    return userId + "-" + containerId;
  }

  public float getCurrentAllocatedRam() {
    return currentAllocatedRam;
  }

  public void setCurrentAllocatedRam(float currentAllocatedRam) {
    this.currentAllocatedRam = currentAllocatedRam;
  }

  public long getCurrentAllocatedBw() {
    return currentAllocatedBw;
  }

  public void setCurrentAllocatedBw(long currentAllocatedBw) {
    this.currentAllocatedBw = currentAllocatedBw;
  }
}
