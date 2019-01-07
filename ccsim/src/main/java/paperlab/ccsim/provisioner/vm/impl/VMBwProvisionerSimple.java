package paperlab.ccsim.provisioner.vm.impl;

import paperlab.ccsim.core.VM;
import paperlab.ccsim.provisioner.vm.VMBwProvisioner;

import java.util.HashMap;
import java.util.Map;

public class VMBwProvisionerSimple extends VMBwProvisioner {
  // key为虚拟机的uid，value为分配的带宽
  private Map<String, Long> bwTable;

  public VMBwProvisionerSimple(long bw) {
    super(bw);
    bwTable = new HashMap<>();
  }

  /**
   * 向虚拟机分配带宽
   * 在{@link org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmBwProvisionerSimple}中
   * 此方法会先回收分配给虚拟机的带宽，如果再次分配{@param bw}不成功，那么虚拟机当前被分配的带宽就变成了0
   * 此处修改为如果分配不成功，则依旧保持原有的分配大小
   *
   * @param vm 虚拟机
   * @param bw          待分配的带宽
   * @return true 分配成功,否则 false
   */
  @Override
  public boolean allocateBwForContainerVm(VM vm, long bw) {
    long availableBw = getAvailableBw();
    long allocatedBwForContainerVm = getAllocatedBwForContainerVm(vm);
    if (isSuitableForContainerVm(vm, bw)) {
      availableBw -= (bw - allocatedBwForContainerVm);
      setAvailableBw(availableBw);
      bwTable.put(vm.getUid(), bw);
      vm.setCurrentAllocatedBw(bw);
      return true;
    }
    return false;
  }

  /**
   * 获取向虚拟机分配的带宽
   *
   * @param vm 虚拟机
   * @return 分配的带宽
   */
  @Override
  public long getAllocatedBwForContainerVm(VM vm) {
    long allocatedBw = 0;
    String uid = vm.getUid();
    if (bwTable.containsKey(uid)) {
      allocatedBw = bwTable.get(uid);
    }
    return allocatedBw;
  }

  /**
   * 回收虚拟机的带宽
   *
   * @param vm 虚拟机
   */
  @Override
  public void deallocateBwForContainerVm(VM vm) {
    long allocatedBw = getAllocatedBwForContainerVm(vm);
    setAvailableBw(getAvailableBw() + allocatedBw);
    bwTable.remove(vm.getUid());
    vm.setCurrentAllocatedBw(0);
  }

  /**
   * 回收所有虚拟机的带宽
   */
  @Override
  public void deallocatedBwForContainerVms() {
    super.deallocatedBwForContainerVms();
    bwTable.clear();
  }

  /**
   * 是否有足够的带宽供虚拟机使用
   *
   * @param vm 虚拟机
   * @param bw          待分配带宽
   * @return true 如果足够，否则 false
   */
  @Override
  public boolean isSuitableForContainerVm(VM vm, long bw) {
   return getAvailableBw() + getAllocatedBwForContainerVm(vm) >= bw;
  }

  protected Map<String, Long> getBwTable() {
    return bwTable;
  }

  protected void setBwTable(Map<String, Long> bwTable) {
    this.bwTable = bwTable;
  }
}
