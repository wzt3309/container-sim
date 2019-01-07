package paperlab.ccsim.provisioner.vm;

import paperlab.ccsim.core.VM;

import java.util.List;

/**
 * ContainerVm内用来分配处理器pe的方法
 */
public abstract class VMPeProvisioner {

  // mips 总量
  private double mips;
  // mips 剩余量
  private double availableMips;

  public VMPeProvisioner(double mips) {
    this.mips = mips;
    this.availableMips = mips;
  }

  /**
   * 给containerVm分配 {@param mips}
   * @param vm 被分配mips的虚拟机
   * @param mips 虚拟机需要的mips量
   * @return true 如果分配成功，否则返回false
   */
  public abstract boolean allocateMipsForContainerVm(VM vm, double mips);

  /**
   * 给containerVm分配 {@param mips}
   * @param containerVmUid 被分配mips的虚拟机的uid
   * @param mips 虚拟机需要的mips量
   * @return true 如果分配成功，否则返回false
   */
  public abstract boolean allocateMipsForContainerVm(String containerVmUid,double mips);

  /**
   * 给containerVm的Pes重新分配一组 {@param mips}
   * @param vm 被分配mips的虚拟机
   * @param mips 需要分配给虚拟机的mips量，列表中的mips[i]代表分配虚拟机的Pe(i)的mips
   * @return true 如果分配成功，否则返回false
   */
  public abstract boolean allocateMipsForContainerVm(VM vm, List<Double> mips);

  public abstract List<Double> getAllocatedMipsForContainerVm(VM vm);

  public abstract double getTotalAllocatedMipsForContainerVm(VM vm);

  /**
   * 获取分配给虚拟机每个pe的mips
   * @param vm 被分配mips的虚拟机
   * @param peId 虚拟机上pe的id
   * @return 分配给虚拟机某pe的mips
   */
  public abstract double getAllocatedMipsForContainerVmByVirtualPeId(VM vm, int peId);

  public abstract void deallocateMipsForContainerVm(VM vm);

  public void deallocatedMipsForAllContainerVms() {
    availableMips = mips;
  }

  public double getTotalAllocatedMips() {
    double totalAllocatedMips = mips - availableMips;
    return totalAllocatedMips > 0 ? totalAllocatedMips : 0;
  }

  public double getUtilization() {
    return getTotalAllocatedMips() / mips;
  }

  public double getMips() {
    return mips;
  }

  public void setMips(double mips) {
    this.mips = mips;
  }

  public double getAvailableMips() {
    return availableMips;
  }

  public void setAvailableMips(double availableMips) {
    this.availableMips = availableMips;
  }
}
