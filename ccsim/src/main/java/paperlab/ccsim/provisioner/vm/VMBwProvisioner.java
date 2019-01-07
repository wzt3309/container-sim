package paperlab.ccsim.provisioner.vm;

import paperlab.ccsim.core.VM;

/**
 * 虚拟机带宽分配
 */
public abstract class VMBwProvisioner {
  private long bw;
  private long availableBw;

  public VMBwProvisioner(long bw) {
    this.bw = bw;
    this.availableBw = bw;
  }

  /**
   * 向虚拟机分配带宽
   * @param vm 虚拟机
   * @param bw 待分配的带宽
   * @return true 分配成功,否则 false
   */
  public abstract boolean allocateBwForContainerVm(VM vm, long bw);

  /**
   * 获取向虚拟机分配的带宽
   * @param vm 虚拟机
   * @return 分配的带宽
   */
  public abstract long getAllocatedBwForContainerVm(VM vm);

  /**
   * 回收虚拟机的带宽
   * @param vm 虚拟机
   */
  public abstract void deallocateBwForContainerVm(VM vm);

  /**
   * 回收所有虚拟机的带宽
   */
  public void deallocatedBwForContainerVms() {
    availableBw = bw;
  }

  /**
   * 是否有足够的带宽供虚拟机使用
   * @param vm 虚拟机
   * @param bw 待分配带宽
   * @return true 如果足够，否则 false
   */
  public abstract boolean isSuitableForContainerVm(VM vm, long bw);

  public long getUsedBw() {
    return bw - availableBw;
  }

  public long getBw() {
    return bw;
  }

  protected void setBw(long bw) {
    this.bw = bw;
  }

  public long getAvailableBw() {
    return availableBw;
  }

  protected void setAvailableBw(long availableBw) {
    this.availableBw = availableBw;
  }
}
