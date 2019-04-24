package paperlab.ccsim.core;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.VmStateHistoryEntry;
import org.cloudbus.cloudsim.container.containerProvisioners.ContainerBwProvisioner;
import org.cloudbus.cloudsim.container.containerProvisioners.ContainerRamProvisioner;
import org.cloudbus.cloudsim.container.core.Container;
import paperlab.ccsim.scheduler.ContainerScheduler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 代表运行容器的虚拟机
 */
public class VM {
  //------------------------------ vm 容量 ------------------------------
  // 虚拟机大小，创建虚拟机需要占用文件系统空间
  private long size;
  // 虚拟机每个处理器Pe(vCpu)需要的处理能力
  private double mips;
  // 虚拟机中处理器Pe数量(就是分配的vCpu数量)
  private int numberOfPe;
  // 虚拟机内存
  private long ram;
  // 虚拟机带宽
  private long bw;

  // the id
  private int id;
  // 创建此虚拟机的用户的id
  private int userId;
  // userId与id结合
  private String uid;
  // 虚拟机所在的host
  private Host host;

  //------------------------------ vm 状态 ------------------------------
  // 虚拟机是否处于 migration 状态
  private boolean isInMigration;
  // 虚拟机是否处于 waiting 状态， waiting 表示虚拟机正在等待container到来
  private boolean isInWaiting;
  // 虚拟机是否处于实例化过程中
  private boolean isInInstantiation;
  // 虚拟机是否处于启动/运行失败状态
  private boolean isFailed;
  // 虚拟机当前分配的文件系统空间
  private long currentAllocatedSize;
  // 虚拟机当前分配的内存大小
  private long currentAllocatedRam;
  // 虚拟机当前分配的带宽大小
  private long currentAllocatedBw;
  // 虚拟机中当前分配的mips的
  // todo 此处要做下处理，不能像cloudsim原来设计的一样，只采用减法来分配cpu能力，cpu属于可压缩资源
  private List<Double> currentAllocatedMips;

  //------------------------------ 容器相关 ------------------------------
  // 虚拟机中容器
  private final List<? extends Container> containerList = new ArrayList<>();
  // 虚拟机中迁移入的容器
  private final List<? extends Container> migratingInContainerList = new ArrayList<>();
  // 虚拟机如何分配处理器能力给容器，因为有多个处理器，因此使用ContainerPe来封装ContainerPeProvisioner
  private List<? extends ContainerPe> peList;
  // 虚拟机如何分配内存给容器
  private ContainerRamProvisioner containerRamProvisioner;
  // 虚拟机中如何分配带宽给容器
  private ContainerBwProvisioner containerBwProvisioner;
  // 虚拟机中如何对运行中的容器进行调度
  private ContainerScheduler containerScheduler;

  //------------------------------ vm 历史 ------------------------------
  private final List<VmStateHistoryEntry> vmStateHistoryEntryList = new LinkedList<>();

  public VM(int id,
            int userId,
            double mips,
            long ram,
            long bw,
            long size,
            ContainerScheduler containerScheduler,
            ContainerRamProvisioner containerRamProvisioner,
            ContainerBwProvisioner containerBwProvisioner,
            List<? extends ContainerPe> peList) {
    this.id = id;
    this.userId = userId;
    this.uid = getUid(userId, id);
    this.mips = mips;
    this.numberOfPe = peList.size();
    this.ram = ram;
    this.bw = bw;
    this.size = size;
    this.containerScheduler = containerScheduler;
    this.containerRamProvisioner = containerRamProvisioner;
    this.containerBwProvisioner = containerBwProvisioner;
    this.peList = peList;

    this.isInInstantiation = true;
  }

  public String getUid() {
    return uid;
  }

  /**
   * Generate unique string identificator of the Container.
   * @param userId
   * @param vmId
   * @return
   */
  public static String getUid(int userId, int vmId) {
    return userId + "_" + vmId;
  }

  public boolean isInMigration() {
    return isInMigration;
  }

  public void setInMigration(boolean inMigration) {
    isInMigration = inMigration;
  }

  public boolean isInWaiting() {
    return isInWaiting;
  }

  public void setInWaiting(boolean inWaiting) {
    isInWaiting = inWaiting;
  }

  public boolean isInInstantiation() {
    return isInInstantiation;
  }

  public void setInInstantiation(boolean inInstantiation) {
    isInInstantiation = inInstantiation;
  }

  public boolean isFailed() {
    return isFailed;
  }

  public void setFailed(boolean failed) {
    isFailed = failed;
  }

  public long getCurrentAllocatedRam() {
    return currentAllocatedRam;
  }

  public void setCurrentAllocatedRam(long currentAllocatedRam) {
    this.currentAllocatedRam = currentAllocatedRam;
  }

  public long getCurrentAllocatedBw() {
    return currentAllocatedBw;
  }

  public void setCurrentAllocatedBw(long currentAllocatedBw) {
    this.currentAllocatedBw = currentAllocatedBw;
  }
}
