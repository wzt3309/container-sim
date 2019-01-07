package paperlab.ccsim.provisioner.vm.impl;

import paperlab.ccsim.core.VM;
import paperlab.ccsim.provisioner.vm.VmRamProvisioner;

import java.util.HashMap;
import java.util.Map;

public class VmRamProvisionerSimple extends VmRamProvisioner {
  // key为虚拟机uid，value为向虚拟机分配的内存
  private Map<String, Long> ramTable;

  public VmRamProvisionerSimple(long ram) {
    super(ram);
    ramTable = new HashMap<>();
  }

  /**
   * 分配内存给虚拟机
   *
   * @param vm 虚拟机
   * @param ram         内存
   * @return true 如果分配成功，否则false
   */
  @Override
  public boolean allocateRamForContainerVm(VM vm, long ram) {
    long availableRam = getAvailableRam();
    long allocatedRamForContainerVm = getAllocatedRamForContainerVm(vm);
    if (isSuitableForContainerVm(vm, ram)) {
      availableRam -= (ram - allocatedRamForContainerVm);
      setAvailableRam(availableRam);
      ramTable.put(vm.getUid(), ram);
      vm.setCurrentAllocatedRam(ram);
      return true;
    }
    return false;
  }

  /**
   * 获得分配给虚拟机的内存
   *
   * @param vm 虚拟机
   * @return 分配的ram
   */
  @Override
  public long getAllocatedRamForContainerVm(VM vm) {
    long allocatedRam = 0;
    String uid = vm.getUid();
    if (ramTable.containsKey(uid)) {
      allocatedRam = ramTable.get(uid);
    }
    return allocatedRam;
  }

  /**
   * 回收分配给虚拟机的内存
   *
   * @param vm 虚拟机
   */
  @Override
  public void deallocateRamForContainerVm(VM vm) {
    long allocatedRam = getAllocatedRamForContainerVm(vm);
    setAvailableRam(getAvailableRam() + allocatedRam);
    ramTable.remove(vm.getUid());
    vm.setCurrentAllocatedRam(0);
  }

  /**
   * 回收所有虚拟机的内存
   */
  @Override
  public void deallocatedRamForContainerVms() {
    super.deallocatedRamForContainerVms();
    ramTable.clear();
  }

  /**
   * 检查是否具有足够的内存给虚拟机使用
   *
   * @param vm 虚拟机
   * @return true 如果足够，否则false
   */
  @Override
  public boolean isSuitableForContainerVm(VM vm, long ram) {
    return getAvailableRam() + getAllocatedRamForContainerVm(vm) >= ram;
  }

  protected Map<String, Long> getRamTable() {
    return ramTable;
  }

  protected void setRamTable(Map<String, Long> ramTable) {
    this.ramTable = ramTable;
  }
}
