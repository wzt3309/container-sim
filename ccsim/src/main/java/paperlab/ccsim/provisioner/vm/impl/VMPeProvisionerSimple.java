package paperlab.ccsim.provisioner.vm.impl;

import paperlab.ccsim.core.VM;
import paperlab.ccsim.provisioner.vm.VMPeProvisioner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VMPeProvisionerSimple extends VMPeProvisioner {
  // Map的key代表虚拟机的uid，value的list依次保存了分配给该虚拟机每个pe的mips值
  private Map<String, List<Double>> mipsTable;

  public VMPeProvisionerSimple(double mips) {
    super(mips);
    mipsTable = new HashMap<>();
  }

  /**
   * 给containerVm分配 {@param mips}
   *
   * @param vm 被分配mips的虚拟机
   * @param mips        虚拟机需要的mips量
   * @return true 如果分配成功，否则返回false
   */
  @Override
  public boolean allocateMipsForContainerVm(VM vm, double mips) {
    return allocateMipsForContainerVm(vm.getUid(), mips);
  }

  /**
   * 给containerVm分配 {@param mips}
   *
   * @param containerVmUid 被分配mips的虚拟机的uid
   * @param mips           虚拟机需要的mips量
   * @return true 如果分配成功，否则返回false
   */
  @Override
  public boolean allocateMipsForContainerVm(String containerVmUid, double mips) {
    if (getAvailableMips() < mips) {
      return false;
    }
    mipsTable.computeIfAbsent(containerVmUid, uidA -> new ArrayList<>()).add(mips);
    setAvailableMips(getAvailableMips() - mips);
    return true;
  }

  /**
   * 给containerVm的Pes重新分配一组 {@param mips}
   * @param vm 被分配mips的虚拟机
   * @param mips 需要分配给虚拟机的mips量，列表中的mips[i]代表分配虚拟机的Pe(i)的mips
   * @return true 如果分配成功，否则返回false
   */
  @Override
  public boolean allocateMipsForContainerVm(VM vm, List<Double> mips) {
    double totalMipsToAllocate = mips.stream().mapToDouble(d -> d).sum();
    if (getAvailableMips() + getTotalAllocatedMipsForContainerVm(vm) < totalMipsToAllocate) {
      return false;
    }
    setAvailableMips(getAvailableMips() + getTotalAllocatedMipsForContainerVm(vm) - totalMipsToAllocate);
    mipsTable.put(vm.getUid(), mips);
    return true;
  }

  @Override
  public List<Double> getAllocatedMipsForContainerVm(VM vm) {
    return mipsTable.get(vm.getUid());
  }

  @Override
  public double getTotalAllocatedMipsForContainerVm(VM vm) {
    List<Double> mips = getAllocatedMipsForContainerVm(vm);
    double totalAllocatedMips = 0;
    if (mips != null) {
      totalAllocatedMips = mips.stream().mapToDouble(d -> d).sum();
    }
    return totalAllocatedMips;
  }

  /**
   * 获取分配给虚拟机每个pe的mips
   *
   * @param vm 被分配mips的虚拟机
   * @param peId        虚拟机上pe的id
   * @return 分配给虚拟机某pe的mips
   */
  @Override
  public double getAllocatedMipsForContainerVmByVirtualPeId(VM vm, int peId) {
    List<Double> mips = getAllocatedMipsForContainerVm(vm);
    if (mips != null && peId >= 0 && peId < mips.size()) {
      return mips.get(peId);
    }
    return 0;
  }

  @Override
  public void deallocateMipsForContainerVm(VM vm) {
    double totalAllocatedMipsForContainerVm = getTotalAllocatedMipsForContainerVm(vm);
    setAvailableMips(getAvailableMips() + totalAllocatedMipsForContainerVm);
    mipsTable.remove(vm.getUid());
  }

  @Override
  public void deallocatedMipsForAllContainerVms() {
    super.deallocatedMipsForAllContainerVms();
    mipsTable.clear();
  }

  protected Map<String, List<Double>> getMipsTable() {
    return mipsTable;
  }

  protected void setMipsTable(Map<String, List<Double>> mipsTable) {
    this.mipsTable = mipsTable;
  }
}
