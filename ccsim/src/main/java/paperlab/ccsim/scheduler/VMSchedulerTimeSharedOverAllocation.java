package paperlab.ccsim.scheduler;

import paperlab.ccsim.core.VMPe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static paperlab.ccsim.util.VMPes.getTotalMips;

/**
 * 允许超量分配Pe的mips，如果已经开始进行超量分配了，那么主机available mips就为0
 */
public class VMSchedulerTimeSharedOverAllocation extends VMSchedulerTimeShared {
  public VMSchedulerTimeSharedOverAllocation(List<? extends VMPe> peList) {
    super(peList);
  }

  /**
   * 超量分配时，会先计算一个收缩因子，然后将每个虚拟机请求的mips根据收缩因子进行收缩
   * 从而保证请求的mips之和小于等于主机的mips总量
   *
   * @param vmUid           虚拟机uid
   * @param requestedMips 虚拟机请求的mips
   * @return true 分配成功，否则false
   */
  @Override
  protected boolean allocatePesForVm(String vmUid, List<Double> requestedMips) {
    double totalRequestedMips = 0;

    // 如果虚拟机请求的单个mips大于Pe的容量，那么我们就将其请求截断为Pe的容量
    List<Double> requestedMipsTruncated = new ArrayList<>();
    double peCapacity = getPeCapacity();
    for (double mips: requestedMips) {
      if (mips > peCapacity) {
        requestedMipsTruncated.add(peCapacity);
        totalRequestedMips += peCapacity;
      } else {
        requestedMipsTruncated.add(mips);
        totalRequestedMips += mips;
      }
    }

    getRequestedMipsMap().put(vmUid, requestedMips);

    List<String> vmsMigratingIn = getVmsMigratingIn();
    List<String> vmsMigratingOut = getVmsMigratingOut();
    if (vmsMigratingIn.contains(vmUid)) {
      totalRequestedMips *= 0.1;
    }
    if (vmsMigratingOut.contains(vmUid)) {
      totalRequestedMips *= 0.9;
    }

    if (getAvailableMips() >= totalRequestedMips) {
      List<Double> _requestedMips = new ArrayList<>();
      for (double mips: requestedMipsTruncated) {
        if (vmsMigratingIn.contains(vmUid)) {
          mips *= 0.1;
        }
        if (vmsMigratingOut.contains(vmUid)) {
          mips *= 0.9;
        }
        _requestedMips.add(mips);
      }

      Map<String, List<Double>> mipsMap = getMipsMap();
      double allocatedMipsForVM = 0;
      if (mipsMap.containsKey(vmUid)) {
        allocatedMipsForVM = getTotalAllocatedMipsForContainerVm(vmUid);
      }
      mipsMap.put(vmUid, _requestedMips);
      setAvailableMips(getAvailableMips() + allocatedMipsForVM - totalRequestedMips);
    } else {
      allocatePesForVMDueToOverAllocation();
    }
    return true;
  }

  /**
   * 根据总的mips容量和全部虚拟机请求的mips来对产生压缩因子，对虚拟机请求的mips进行压缩，
   * 从而来允许mips超量
   */
  private void allocatePesForVMDueToOverAllocation() {
    double totalRequiredMipsByAllVMs = 0;
    double peCapacity = getPeCapacity();
    Map<String, List<Double>> requestedMipsTruncatedMap = new HashMap<>();
    List<String> vmsMigratingIn = getVmsMigratingIn();
    List<String> vmsMigratingOut = getVmsMigratingOut();

    for (Map.Entry<String, List<Double>> entry: getRequestedMipsMap().entrySet()) {
      double totalRequiredMips = 0;
      String vmUid = entry.getKey();
      List<Double> requestedMips = entry.getValue();
      List<Double> requestedMipsTruncated = new ArrayList<>();

      for (double mips: requestedMips) {
        if (mips > peCapacity) {
          requestedMipsTruncated.add(peCapacity);
          totalRequiredMips += peCapacity;
        } else {
          requestedMipsTruncated.add(mips);
          totalRequiredMips += mips;
        }
      }
      requestedMipsTruncatedMap.put(vmUid, requestedMipsTruncated);

      if (vmsMigratingIn.contains(vmUid)) {
        totalRequiredMips *= 0.1;
      } else if (vmsMigratingOut.contains(vmUid)) {
        totalRequiredMips *= 0.9;
      }
      totalRequiredMipsByAllVMs += totalRequiredMips;
    }

    double totalAvailableMips = getTotalMips(getPeList());
    double scalingFactor = totalAvailableMips / totalRequiredMipsByAllVMs;

    // 需要对之前分配的mips进行修改，要乘上mips压缩率 scalingFactor
    getMipsMap().clear();

    for (Map.Entry<String, List<Double>> entry: requestedMipsTruncatedMap.entrySet()) {
      String vmUid = entry.getKey();
      List<Double> requestedMipsTruncated = entry.getValue();
      List<Double> updateMipsAllocation = new ArrayList<>();

      for (double mips: requestedMipsTruncated) {
        if (vmsMigratingIn.contains(vmUid)) {
          mips *= scalingFactor;
          mips *= 0.1;
        } else if (vmsMigratingOut.contains(vmUid)) {
          mips *= scalingFactor;
          mips *= 0.9;
        } else {
          mips *= scalingFactor;
        }
        updateMipsAllocation.add(Math.floor(mips));
      }

      // 更新mips分配列表
      getMipsMap().put(vmUid, updateMipsAllocation);
    }

    // 因为已经是超量分配了，因此available为0
    setAvailableMips(0);
  }
}
