package paperlab.ccsim.scheduler;

import paperlab.ccsim.core.ContainerPe;

import java.util.*;

import static paperlab.ccsim.util.ContainerPes.getTotalMips;

/**
 * 在虚拟机中调度容器运行的策略，主要是调度Pe的使用
 */
public abstract class ContainerScheduler {
  // 可供容器使用的Pe列表，docker中如果不设定--cupsets来绑定特定的核
  // 每个容器都是使用所有虚拟机的vCpu(Pes)
  private List<? extends ContainerPe> peList;

  // 一个调度周期内，分配给每个容器具体使用Pe
  private Map<String, List<ContainerPe>> peMap;

  // 一个调度周期内，分配给每个容器使用的Pe的mips量
  private Map<String, List<Double>> mipsMap;

  // 一个调度周期内，剩余还未分配的mips总量
  private double availableMips;

  // 正在迁移入的容器
  private List<String> containersMigratingIn;

  // 正在迁移出的容器
  private List<String> containersMigratingOut;

  public ContainerScheduler(List<? extends ContainerPe> peList) {
    if (peList == null || peList.isEmpty()) {
      throw new IllegalArgumentException("PeList is null or empty");
    }
    this.peList = peList;
    peMap = new HashMap<>();
    mipsMap = new HashMap<>();
    availableMips = getTotalMips(peList);
    containersMigratingIn = new ArrayList<>();
    containersMigratingOut = new ArrayList<>();
  }

  /**
   * 分配pe给容器的，该方法可能根据分配策略来调整实际分配给容器的Pe和mips
   * 有些策略可能通过压缩每个容器的mips量来允许超量分配
   * @param container 容器
   * @param requestedMips 容器需要的mips，一个容器可能需要多核来执行
   * @return true 分配成功，否则false
   */
  public abstract boolean allocatePesForContainer(Container container, List<Double> requestedMips);

  /**
   * 回收分配给容器的Pe和mips
   * @param container 容器
   */
  public abstract void deallocatePesForContainer(Container container);

  /**
   * 回收所有的mips资源
   */
  public void deallocatePesForAllContainers() {
    mipsMap.clear();
    peMap.clear();
    availableMips = getTotalMips(peList);
    for (ContainerPe pe: peList) {
      pe.deallocatedMipsForAllContainers();
    }
  }

  public List<ContainerPe> getPesAllocatedForContainer(Container container) {
    return getPesAllocatedForContainer(container.getUid());
  }

  public List<ContainerPe> getPesAllocatedForContainer(String containerUid) {
    return peMap.get(containerUid);
  }

  public double getTotalAllocatedMipsForContainer(Container container) {
    return getTotalAllocatedMipsForContainer(container.getUid());
  }

  public double getTotalAllocatedMipsForContainer(String containerUid) {
    List<Double> mipsList = mipsMap.get(containerUid);
    double totalMips = 0;
    if (mipsList != null && !mipsList.isEmpty()) {
      totalMips = mipsList.stream().mapToDouble(Double::doubleValue).sum();
    }
    return totalMips;
  }

  /**
   * 获得所有Pe中剩余mips最多的Pe的available mips
   * @return max available pe among all Pes
   */
  public double getMaxAvailableMips() {
    ContainerPe max = peList.stream()
        .max(Comparator.comparingDouble(ContainerPe::getAvailableMips))
        .orElse(null);
    return max != null ? max.getAvailableMips() : 0;
  }

  /**
   * 获得Pe的容量，假设每个pe的容量是一至的
   * @return mips
   */
  public double getPeCapacity() {
    // peList it not null
    return peList.get(0).getMips();
  }

  @SuppressWarnings("unchecked")
  public <T extends ContainerPe> List<T> getPeList() {
    return (List<T>) peList;
  }

  protected  <T extends ContainerPe> void setPeList(List<T> peList) {
    this.peList = peList;
  }

  public Map<String, List<Double>> getMipsMap() {
    return mipsMap;
  }

  public void setMipsMap(Map<String, List<Double>> mipsMap) {
    this.mipsMap = mipsMap;
  }

  public Map<String, List<ContainerPe>> getPeMap() {
    return peMap;
  }

  protected void setPeMap(Map<String, List<ContainerPe>> peMap) {
    this.peMap = peMap;
  }

  public double getAvailableMips() {
    return availableMips;
  }

  protected void setAvailableMips(double availableMips) {
    this.availableMips = availableMips;
  }

  public List<String> getContainersMigratingIn() {
    return containersMigratingIn;
  }

  protected void setContainersMigratingIn(List<String> containersMigratingIn) {
    this.containersMigratingIn = containersMigratingIn;
  }

  public List<String> getContainersMigratingOut() {
    return containersMigratingOut;
  }

  protected void setContainersMigratingOut(List<String> containersMigratingOut) {
    this.containersMigratingOut = containersMigratingOut;
  }
}
