package paperlab.ccsim.provisioner.vm;

import paperlab.ccsim.core.VM;

/**
 * 主机中给vm分配内存的方法
 */
public abstract class VmRamProvisioner {
  private long ram;
  private long availableRam;

  public VmRamProvisioner(long ram) {
    this.ram = ram;
    this.availableRam = ram;
  }

  /**
   * 分配内存给虚拟机
   * @param vm 虚拟机
   * @param ram 内存
   * @return true 如果分配成功，否则false
   */
  public abstract boolean allocateRamForContainerVm(VM vm, long ram);

  /**
   * 获得分配给虚拟机的内存
   * @param vm 虚拟机
   * @return 分配的ram
   */
  public abstract long getAllocatedRamForContainerVm(VM vm);

  /**
   * 回收分配给虚拟机的内存
   * @param vm 虚拟机
   */
  public abstract void deallocateRamForContainerVm(VM vm);

  /**
   * 回收所有虚拟机的内存
   */
  public void deallocatedRamForContainerVms() {
    availableRam = ram;
  }

  /**
   * 检查是否具有足够的内存给虚拟机使用
   * @param vm 虚拟机
   * @return true 如果足够，否则false
   */
  public abstract boolean isSuitableForContainerVm(VM vm, long ram);

  public double getUsedVmRam() {
    return ram - availableRam;
  }

  public long getRam() {
    return ram;
  }

  protected void setRam(long ram) {
    this.ram = ram;
  }

  public long getAvailableRam() {
    return availableRam;
  }

  protected void setAvailableRam(long availableRam) {
    this.availableRam = availableRam;
  }
}
