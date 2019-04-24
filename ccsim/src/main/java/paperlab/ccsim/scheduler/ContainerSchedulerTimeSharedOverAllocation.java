package paperlab.ccsim.scheduler;

import org.cloudbus.cloudsim.container.containerProvisioners.ContainerPe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static paperlab.ccsim.util.ContainerPes.getTotalMips;

public class ContainerSchedulerTimeSharedOverAllocation extends ContainerSchedulerTimeShared {
  public ContainerSchedulerTimeSharedOverAllocation(List<? extends ContainerPe> peList) {
    super(peList);
  }

  /**
   * 根据容器uid来分配mips，部分策略允许超量分配
   * 如果策略不允许超量分配，那么当容器请求的总的Mips大于available mips则分配失败
   *
   * @param containerUid  容器uid
   * @param requestedMips 容器自身请求的mips
   * @return true 分配成功，否则false
   */
  @Override
  protected boolean allocatePesForContainer(String containerUid, List<Double> requestedMips) {
    double totalRequiredMips = 0;

    List<Double> requestedMipsTruncated = new ArrayList<>();
    double peCapacity = getPeCapacity();
    for (double mips: requestedMips) {
      if (mips > peCapacity) {
        mips = peCapacity;
      }
      requestedMipsTruncated.add(mips);
      totalRequiredMips += mips;
    }

    List<String> containersMigratingIn = getContainersMigratingIn();
    // 容器迁移是不需要时间的，因此正在迁移进来的容器其实还在源主机上运行
    if (containersMigratingIn.contains(containerUid)) {
      totalRequiredMips = 0.0;
    }

    getRequestedMipsMap().put(containerUid, requestedMips);

    if (getAvailableMips() >= totalRequiredMips) {
      List<Double> allocatedMips = new ArrayList<>();
      for (double mips: requestedMipsTruncated) {
        // 容器迁移是不需要时间的，因此正在迁移进来的容器其实还在源主机上运行
        if (containersMigratingIn.contains(containerUid)) {
          mips = 0.0;
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
    } else {
      allocatePesForContainerDueToOverAllocation();
    }

    return true;
  }

  protected void allocatePesForContainerDueToOverAllocation() {
    double totalRequiredMipsByAllContainers = 0;
    double peCapacity = getPeCapacity();
    List<String> containersMigratingIn = getContainersMigratingIn();
    Map<String, List<Double>> requestedMipsTruncatedMap = new HashMap<>();

    for (Map.Entry<String, List<Double>> entry: getRequestedMipsMap().entrySet()) {
      double totalRequiredMips = 0;
      List<Double> requestedMipsTruncated = new ArrayList<>();
      String containerUid = entry.getKey();

      for (double mips: entry.getValue()) {
        if (mips > peCapacity) {
          mips = peCapacity;
        }
        requestedMipsTruncated.add(mips);
        totalRequiredMips += mips;
      }

      requestedMipsTruncatedMap.put(containerUid, requestedMipsTruncated);
      // 容器迁移是不需要时间的，因此正在迁移进来的容器其实还在源主机上运行
      if (containersMigratingIn.contains(containerUid)) {
        totalRequiredMips = 0.0;
      }
      totalRequiredMipsByAllContainers += totalRequiredMips;
    }

    // pe的总容量
    double totalAvailableMips = getTotalMips(getPeList());
    double scalingFactor = totalAvailableMips / totalRequiredMipsByAllContainers;

    // 清空之前的mips分配
    getMipsMap().clear();

    for (Map.Entry<String, List<Double>> entry: requestedMipsTruncatedMap.entrySet()) {
      String containerUid = entry.getKey();
      List<Double> requestedMipsTruncated = entry.getValue();
      List<Double> updateMipsAllocation = new ArrayList<>();

      for (double mips: requestedMipsTruncated) {
        // 容器迁移是不需要时间的，因此正在迁移进来的容器其实还在源主机上运行
        if (containersMigratingIn.contains(containerUid)) {
          mips = 0.0;
        } else {
          mips *= scalingFactor;
        }
        updateMipsAllocation.add(Math.floor(mips));
      }

      getMipsMap().put(containerUid, updateMipsAllocation);
    }

    // 已经超量分配了，available mips 为0
    setAvailableMips(0);
  }
}
