package paperlab.ccsim.scheduler;

import org.cloudbus.cloudsim.Log;
import paperlab.ccsim.core.VM;
import paperlab.ccsim.core.VMPe;
import paperlab.ccsim.provisioner.vm.VMPeProvisioner;

import java.util.*;

import static paperlab.ccsim.util.VMPes.getTotalMips;

/**
 * 分配时，先通过{@link #allocatePesForVm(String, List)}来确定能否分配mips给vm以及分配的数量，
 * 然后执行{@link #updatePeProvisioning()}来由具体的Pe来向vm提供mips
 */
public class VMSchedulerTimeShared extends VMScheduler {
  // 原生vm请求的mips，实际消耗的mips与此不同，正在迁出和迁入机器的只占用主机90%和10%的mips
  // 真实请求mips保存在mipsMap中
  private Map<String, List<Double>> requestedMipsMap;

  public VMSchedulerTimeShared(List<? extends VMPe> peList) {
    super(peList);
    requestedMipsMap = new HashMap<>();
  }

  /**
   * 分配Pe给虚拟机
   *
   * @param vm        虚拟机
   * @param requestedMips 需要分配给虚拟机的mips，这是一个list，因为一个vm可能需要多个Pe且需要不同的mips
   * @return 如果分配成功，返回true 否则false
   */
  @Override
  public boolean allocatePesForVm(VM vm, List<Double> requestedMips) {
    String uid = vm.getUid();
    List<String> vmsMigratingIn = getVmsMigratingIn();
    List<String> vmsMigratingOut = getVmsMigratingOut();

    if (vm.isInMigration()) {
      if (!vmsMigratingIn.contains(uid) && !vmsMigratingOut.contains(uid)) {
        vmsMigratingOut.add(uid);
      }
    } else {
      // 如果vm不在迁移态，需要检查其是否存在于迁出列表中，若不在迁出列表中，以下调用不生效
      vmsMigratingOut.remove(uid);
    }

    boolean result = allocatePesForVm(uid, requestedMips);
    updatePeProvisioning();
    return result;
  }

  /**
   * 更新主机向vm分配Pe的过程，就是更新每个Pe的VMPeProvisioner
   */
  protected void updatePeProvisioning() {
    // 将之前pe分配索引map清空，并回收pe将pe分配数据归0
    getPeMap().clear();
    for (VMPe pe: getPeList()) {
      pe.getVmPeProvisioner().deallocatedMipsForAllContainerVms();
    }

    Iterator<VMPe> vmPeItor = getPeList().iterator();
    VMPe vmPe = vmPeItor.next();
    VMPeProvisioner vmPeProvisioner = vmPe.getVmPeProvisioner();
    double vmPeAvailableMips = vmPeProvisioner.getAvailableMips();

    // 循环寻找pe来提供需要分配的mips
    for (Map.Entry<String, List<Double>> entry: getMipsMap().entrySet()) {
      String vmUid = entry.getKey();
      //  初始化分配给vm的pe列表
      getPeMap().put(vmUid, new LinkedList<>());

      for (double mips: entry.getValue()) {
        while (mips >= 0.1) {
          if (vmPeAvailableMips >= mips) {
            vmPeProvisioner.allocateMipsForContainerVm(vmUid, mips);
            // 分配完成 更新pe available mips
            vmPeAvailableMips = vmPeProvisioner.getAvailableMips();
            getPeMap().get(vmUid).add(vmPe);
            break;
          } else {
            // 将pe剩余mips全分配给该vm
            vmPeProvisioner.allocateMipsForContainerVm(vmUid, vmPeAvailableMips);
            if (vmPeAvailableMips != 0) {
              getPeMap().get(vmUid).add(vmPe);
            }

            // 虚拟机剩余需要的mips
            mips -= vmPeAvailableMips;
            if (mips <= 0.1) {
              break;
            }

            // 此处会存在分配不足的情况
            if (!vmPeItor.hasNext()) {
              Log.printConcatLine("There is no enough MIPS (", mips, ") to allocate to VM ", vmUid);
            }

            vmPe = vmPeItor.next();
            vmPeProvisioner = vmPe.getVmPeProvisioner();
            vmPeAvailableMips = vmPeProvisioner.getAvailableMips();
          }
        }
      }
    }
  }

  /**
   * 检查虚拟机请求的mips是否小于主机可提供的mips，如果不足，则分配不成功
   * 正在迁入和迁出的虚拟机需求的mips与常规不同，迁出主机只使用0.9*mips，迁入主机只使用0.1*mips
   * @param vmUid 虚拟机uid
   * @param requestedMips 虚拟机请求的mips
   * @return true 分配成功，否则false
   */
  protected boolean allocatePesForVm(String vmUid, List<Double> requestedMips) {
    double totalRequestedMips = 0;
    // pe capacity in mips
    double peCapacity = getPeCapacity();
    for (double mips: requestedMips) {
      if (mips > peCapacity) {
        // 对单个pe请求的mips不能超过该Pe的容量
        return false;
      }
      totalRequestedMips += mips;
    }

    // 放入原始请求的mips
    requestedMipsMap.put(vmUid, requestedMips);

    List<String> vmsMigratingIn = getVmsMigratingIn();
    List<String> vmsMigratingOut = getVmsMigratingOut();

    if (vmsMigratingIn.contains(vmUid)) {
      // 作为迁入的目标机，正在迁移过程的vm只占用目标机的10% mips
      totalRequestedMips *= 0.1;
    } else if (vmsMigratingOut.contains(vmUid)) {
      // 作为迁出的源头机，正在迁移过程的vm只占用源头机的90% mips
      totalRequestedMips *= 0.9;
    }

    // 请求的mips超过了剩余可供分配的mips总量, not allow over-allocation
    if (getAvailableMips() < totalRequestedMips) {
      return false;
    }

    List<Double> _requestedMips = new ArrayList<>();
    for (double mips: requestedMips) {
      if (vmsMigratingIn.contains(vmUid)) {
        mips *= 0.1;
      } else if (vmsMigratingOut.contains(vmUid)) {
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
    // 如果vm已经存在，那么属于重新分配，因此需要加上之前已经分配的mips
    setAvailableMips(getAvailableMips() + allocatedMipsForVM - totalRequestedMips);

    return true;
  }

  /**
   * 从虚拟机上回收Pe
   *
   * @param vm 虚拟机
   */
  @Override
  public void deallocatePersForVm(VM vm) {
    String vmUid = vm.getUid();
    // 从原始请求列表中移除vm
    requestedMipsMap.remove(vmUid);

    // 重置分配列表和available mips
    getMipsMap().clear();
    setAvailableMips(getTotalMips(getPeList()));
    for (VMPe pe: getPeList()) {
      pe.getVmPeProvisioner().deallocatedMipsForAllContainerVms();
    }

    for (Map.Entry<String, List<Double>> entry: requestedMipsMap.entrySet()) {
      allocatePesForVm(entry.getKey(), entry.getValue());
    }

    updatePeProvisioning();
  }

  /**
   * 回收所有分配给vm的Pe
   */
  @Override
  public void deallocatePesForAllContainerVms() {
    super.deallocatePesForAllContainerVms();
    requestedMipsMap.clear();
  }

  /**
   * 对于time share 策略来说 最大可供分配的Mips就是剩余可分配的Mips
   *
   * @return max mips
   */
  @Override
  public double getMaxAvailableMips() {
    return getAvailableMips();
  }

  public Map<String, List<Double>> getRequestedMipsMap() {
    return requestedMipsMap;
  }

  public void setRequestedMipsMap(Map<String, List<Double>> requestedMipsMap) {
    this.requestedMipsMap = requestedMipsMap;
  }
}
