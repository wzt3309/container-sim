package paperlab.ccsim.scheduler;

import paperlab.ccsim.core.VM;
import paperlab.ccsim.core.VMPe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static paperlab.ccsim.util.VMPes.getTotalMips;

/**
 * 在主机中vmm使用的用来调度虚拟机运行的策略
 */
public abstract class VMScheduler {

  // 主机上拥有的pe列表
  private List<? extends VMPe> peList;
  // 虚拟机对应分配的pe
  private Map<String, List<VMPe>> peMap;
  // 需要分配给虚拟机的mips
  private Map<String, List<Double>> mipsMap;
  // 剩余的总共可分配的mips
  private double availableMips;
  // 迁移入的vm
  private List<String> vmsMigratingIn;
  // 迁移出的vm
  private List<String> vmsMigratingOut;

  public VMScheduler(List<? extends VMPe> peList) {
    if (peList == null || peList.isEmpty()) {
      throw new IllegalArgumentException("PeList cannot be null or empty");
    }
    this.peList = peList;
    peMap = new HashMap<>();
    mipsMap =new HashMap<>();
    availableMips = getTotalMips(peList);
    vmsMigratingIn = new ArrayList<>();
    vmsMigratingOut = new ArrayList<>();
  }

  /**
   * 分配Pe给虚拟机
   * @param vm 虚拟机
   * @param mipsShare 需要分配给虚拟机的mips，这是一个list，因为一个vm可能需要多个Pe且需要不同的mips
   * @return 如果分配成功，返回true 否则false
   */
  public abstract boolean allocatePesForVm(VM vm, List<Double> mipsShare);

  /**
   * 从虚拟机上回收Pe
   * @param vm 虚拟机
   */
  public abstract void deallocatePersForVm(VM vm);

  /**
   * 回收所有分配给vm的Pe
   */
  public void deallocatePesForAllContainerVms() {
    mipsMap.clear();
    peMap.clear();
    availableMips = getTotalMips(peList);
    for (VMPe pe: peList) {
      pe.getVmPeProvisioner().deallocatedMipsForAllContainerVms();
    }
  }

  /**
   * 获得分配给虚拟机的Pe列表
   * @param vm 虚拟机
   * @return Pe列表
   */
  public List<? extends VMPe> getPesAllocatedForContainerVm(VM vm) {
    return peMap.get(vm.getUid());
  }

  /**
   * 获得分配给虚拟机的mips
   * @param vm 虚拟机
   * @return 返回分配给虚拟机的每个虚拟pe的mips
   * 比如主机虚拟化出4个pe -> VMPe
   * 虚拟机vm1获得了 pe1 pe3 但是不是获得pe1 pe3的全部mips而是部分mips
   * 这是一种time share的方式，mips相当于一个时间片内处理器被划分的小格
   */
  public List<Double> getAllocatedMipsForContainerVm(VM vm) {
    return getAllocatedMipsForContainerVm(vm.getUid());
  }

  public List<Double> getAllocatedMipsForContainerVm(String vmUid) {
    return mipsMap.get(vmUid);
  }

  /**
   * 获得分配给虚拟机得mips总量
   * @param vm 虚拟机
   * @return 分配给虚拟机的mips总量
   */
  public double getTotalAllocatedMipsForContainerVm(VM vm) {
    return getTotalAllocatedMipsForContainerVm(vm.getUid());
  }

  public double getTotalAllocatedMipsForContainerVm(String vmUid) {
    double totalAllocated = 0;
    List<Double> allocatedMips = getAllocatedMipsForContainerVm(vmUid);
    if (allocatedMips != null) {
      totalAllocated = allocatedMips.stream().mapToDouble(d -> d).sum();
    }
    return totalAllocated;
  }

  /**
   * 获得pe中最多的剩余未分配mips
   * @return max mips
   */
  public double getMaxAvailableMips() {
    double max = 0;
    for (VMPe pe: peList) {
      double tmp = pe.getVmPeProvisioner().getAvailableMips();
      if (tmp > max) {
        max = tmp;
      }
    }
    return max;
  }

  /**
   * 获取单个pe的mips总量，这里假设一个host的每个pe的mips都是一样大小
   * @return 单个mips的总量
   */
  public double getPeCapacity() {
    return peList.get(0).getMips();
  }

  @SuppressWarnings("unchecked")
  public <T extends VMPe> List<T> getPeList() {
    return (List<T>) peList;
  }

  protected <T extends VMPe> void setPeList(List<T> peList) {
    this.peList = peList;
  }


  /**
   * 虚拟机对应分配的pe
   * @return  peMap Map
   */
  public Map<String, List<VMPe>> getPeMap() {
    return peMap;
  }

  protected void setPeMap(Map<String, List<VMPe>> peMap) {
    this.peMap = peMap;
  }

  /**
   * 需要分配给虚拟机的mips量，或者说是虚拟机实际需要用到的mips量
   * @return Map
   */
  protected Map<String, List<Double>> getMipsMap() {
    return mipsMap;
  }

  protected void setMipsMap(Map<String, List<Double>> mipsMap) {
    this.mipsMap = mipsMap;
  }

  /**
   * 剩余的总共可分配的mips
   * @return mips
   */
  public double getAvailableMips() {
    return availableMips;
  }

  protected void setAvailableMips(double availableMips) {
    this.availableMips = availableMips;
  }

  public List<String> getVmsMigratingIn() {
    return vmsMigratingIn;
  }

  protected void setVmsMigratingIn(List<String> vmsMigratingIn) {
    this.vmsMigratingIn = vmsMigratingIn;
  }

  public List<String> getVmsMigratingOut() {
    return vmsMigratingOut;
  }

  protected void setVmsMigratingOut(List<String> vmsMigratingOut) {
    this.vmsMigratingOut = vmsMigratingOut;
  }
}
