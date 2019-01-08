package paperlab.ccsim.scheduler;

import org.cloudbus.cloudsim.Log;
import paperlab.ccsim.core.Container;
import paperlab.ccsim.core.ContainerPe;

import java.util.*;

import static paperlab.ccsim.util.ContainerPes.getTotalMips;

public class ContainerSchedulerTimeShared extends ContainerScheduler {
  private Map<String, List<Double>> requestedMipsMap;

  public ContainerSchedulerTimeShared(List<? extends ContainerPe> peList) {
    super(peList);
    requestedMipsMap = new HashMap<>();
  }

  /**
   * 分配pe给容器的，该方法可能根据分配策略来调整实际分配给容器的Pe和mips
   * 有些策略可能通过压缩每个容器的mips量来允许超量分配
   *
   * @param container     容器
   * @param requestedMips 容器需要的mips，一个容器可能需要多核来执行
   * @return true 分配成功，否则false
   */
  @Override
  public boolean allocatePesForContainer(Container container, List<Double> requestedMips) {
    boolean result = allocatePesForContainer(container.getUid(), requestedMips);
    updatePeProvisioning();
    return result;
  }

  /**
   * 根据{@link #allocatePesForContainer(String, List)}分配的mips分配列表来更新Pe
   */
  protected void updatePeProvisioning() {
    // 清空pe分配列表
    getPeMap().clear();
    // 重置pe分配
    for (ContainerPe pe: getPeList()) {
      pe.deallocatedMipsForAllContainers();
    }

    Iterator<ContainerPe> peItor = getPeList().iterator();
    ContainerPe pe = peItor.next();
    double peAvailableMips = pe.getAvailableMips();

    for (Map.Entry<String, List<Double>> entry: getMipsMap().entrySet()) {
      String containerUid = entry.getKey();
      getPeMap().put(containerUid, new LinkedList<>());

      for (double mips: entry.getValue()) {
        while (mips >= 0.1) {
          if (peAvailableMips >= mips) {
            pe.allocateMipsForContainer(containerUid, mips);
            getPeMap().get(containerUid).add(pe);
            peAvailableMips -= mips;
          } else {
            pe.allocateMipsForContainer(containerUid, peAvailableMips);
            if (peAvailableMips != 0) {
              getPeMap().get(containerUid).add(pe);
            }
            mips -= peAvailableMips;
            if (mips <= 0.1) {
              break;
            }
            if (!peItor.hasNext()) {
              Log.printConcatLine("There is no enough MIPS (", mips, ") to allocate to container ", containerUid);
            }
            pe = peItor.next();
            peAvailableMips = pe.getAvailableMips();
          }
        }
      }
    }
  }

  /**
   * 根据容器uid来分配mips，部分策略允许超量分配
   * 如果策略不允许超量分配，那么当容器请求的总的Mips大于available mips则分配失败
   * @param containerUid 容器uid
   * @param requestedMips 容器自身请求的mips
   * @return true 分配成功，否则false
   */
  protected boolean allocatePesForContainer(String containerUid, List<Double> requestedMips) {
    double totalRequiredMips = 0;
    double peCapacity = getPeCapacity();
    for (double mips: requestedMips) {
      if (mips > peCapacity) {
        return false;
      }
      totalRequiredMips += mips;
    }

    List<String> containersMigratingIn = getContainersMigratingIn();
    if (containersMigratingIn.contains(containerUid)) {
      totalRequiredMips = 0;
    }

    if (getAvailableMips() < totalRequiredMips) {
      return false;
    }

    requestedMipsMap.put(containerUid, requestedMips);

    List<Double> allocatedMips = new ArrayList<>();
    for (double mips: requestedMips) {
      if (containersMigratingIn.contains(containerUid)) {
        mips = 0;
      }
      allocatedMips.add(mips);
    }

    Map<String, List<Double>> mipsMap = getMipsMap();
    double totalAllocatedMips = 0;
    if (mipsMap.containsKey(containerUid)) {
      totalAllocatedMips = getTotalAllocatedMipsForContainer(containerUid);
    }
    mipsMap.put(containerUid, allocatedMips);
    setAvailableMips(getAvailableMips() + totalAllocatedMips - totalRequiredMips);

    return true;
  }

  /**
   * 回收分配给容器的Pe和mips
   *
   * @param container 容器
   */
  @Override
  public void deallocatePesForContainer(Container container) {
    String containerUid = container.getUid();
    // 将该容器从请求分配mips的列表中移除
    requestedMipsMap.remove(containerUid);

    // 清理mips分配列表，重置pe
    getMipsMap().clear();
    setAvailableMips(getTotalMips(getPeList()));
    for (ContainerPe pe: getPeList()) {
      pe.deallocatedMipsForAllContainers();
    }

    // 重新分配划分给容器的mips
    for (Map.Entry<String, List<Double>> entry: requestedMipsMap.entrySet()) {
      allocatePesForContainer(entry.getKey(), entry.getValue());
    }

    // 根据分配给容器的mips更新Pe
    updatePeProvisioning();
  }

  /**
   * 回收所有的mips资源
   */
  @Override
  public void deallocatePesForAllContainers() {
    super.deallocatePesForAllContainers();
    requestedMipsMap.clear();
  }

  /**
   * Time share情况中，最大available mips 就是总共 available mips
   *
   * @return available mips
   */
  @Override
  public double getMaxAvailableMips() {
    return getAvailableMips();
  }

  /**
   * 容器原始请求的mips map
   * @return mips
   */
  protected Map<String, List<Double>> getRequestedMipsMap() {
    return requestedMipsMap;
  }

  protected void setRequestedMipsMap(Map<String, List<Double>> requestedMipsMap) {
    this.requestedMipsMap = requestedMipsMap;
  }
}
