package paperlab.ccsim.provisioners;

import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmPeProvisioner;
import org.cloudbus.cloudsim.container.core.ContainerVm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 主机分配Pe给VM的方案，当VM需求的mips大于可提供的availableMips时，会根据各VM总需求的占比对分配方案进行压缩
 *
 * 如：设置主机总mips = 600， vm1 = [100, 100] vm2 = [200, 200] available = 600 - (100 * 2 + 200 * 2) = 0
 * 当vm2又需要200时，则进行压缩
 * totalRequestedMips = 100 * 2 + 200 * 3 = mips - available + 200 = 800
 * zipped_mips_vm1 = (100 * 2) / 800 * 600 =>  new vm1 = [100 / 200 , 100 / 200] * zipped_mips_vm1
 * zipped_mips_vm2 = (200 * 3) / 800 * 600 =>  new vm2 = [200 / 600, 200 / 600, 200 / 600] * zipped_mips_vm2
 *
 */
public class ContainerVmPeProvisionerZipped extends ContainerVmPeProvisioner {
    // key为vm的uid, value为vm的每个pe获得的mips
    private Map<String, List<Double>> peMap;

    public ContainerVmPeProvisionerZipped(double mips) {
        super(mips);
        peMap = new HashMap<>();
    }

    @Override
    public boolean allocateMipsForContainerVm(ContainerVm containerVm, double mips) {
        return allocateMipsForContainerVm(containerVm.getUid(), mips);
    }

    @Override
    public boolean allocateMipsForContainerVm(String s, double mips) {
        if (mips < 0) {
            throw new IllegalArgumentException("mips is negative");
        }
        if (s == null || "".equals(s)) {
            return false;
        }
        // 先将mips添加到分配列表中
        List<Double> allocatedMips = peMap.computeIfAbsent(s, key -> new ArrayList<>());
        allocatedMips.add(mips);

        // 如果mips大于可供分配的mips，则进行全局压缩
        if (getAvailableMips() < mips) {
            double oldTotalAllocatedMips = getTotalAllocatedMips();
            double newTotalAllocatedMips = 0.0;
            // 压缩所有pe的mips
            for (String key: peMap.keySet()) {
                double totalRequestedMips = oldTotalAllocatedMips + mips;
                newTotalAllocatedMips += zipMips(key, totalRequestedMips);
            }
            setAvailableMips(getMips() - newTotalAllocatedMips);
        } else {
            setAvailableMips(getAvailableMips() - mips);
            peMap.put(s, allocatedMips);
        }
        return true;
    }

    @Override
    public boolean allocateMipsForContainerVm(ContainerVm containerVm, List<Double> mipsList) {
        if (mipsList == null || mipsList.isEmpty()) {
            throw new IllegalArgumentException("mips is null or empty");
        }
        String uid = containerVm.getUid();
        double oldAllocatedMips = getTotalAllocatedMipsForContainerVm(uid);
        double nowAllocatedMips = mipsList.stream().mapToDouble(Double::valueOf).sum();
        // 更新该虚拟机的pe分配列表
        peMap.put(uid, new ArrayList<>(mipsList));
        if (getAvailableMips() + oldAllocatedMips < nowAllocatedMips) {
            double oldTotalAllocatedMips = getTotalAllocatedMips();
            double newTotalAllocatedMips = 0.0;
            for (String key: peMap.keySet()) {
                double totalRequestedMips = oldTotalAllocatedMips - oldAllocatedMips + nowAllocatedMips;
                newTotalAllocatedMips += zipMips(key, totalRequestedMips);
            }
            setAvailableMips(getMips() - newTotalAllocatedMips);
        }else {
            setAvailableMips(getAvailableMips() + oldAllocatedMips - nowAllocatedMips);
        }
        return true;
    }

    private double zipMips(String key, double totalRequestedMips) {
        List<Double> value = peMap.get(key);
        double totalMips = getTotalAllocatedMipsForContainerVm(key);
        double factor = totalMips / totalRequestedMips;
        value = value.stream().map(old -> (getMips() * factor) * (old / totalMips)).collect(Collectors.toList());
        peMap.put(key, value);
        return getTotalAllocatedMipsForContainerVm(key);
    }

    @Override
    public List<Double> getAllocatedMipsForContainerVm(ContainerVm containerVm) {
        return peMap.computeIfPresent(containerVm.getUid(), (k, oldVal) -> oldVal);
    }

    @Override
    public double getTotalAllocatedMipsForContainerVm(ContainerVm containerVm) {
        return getTotalAllocatedMipsForContainerVm(containerVm.getUid());
    }

    private double getTotalAllocatedMipsForContainerVm(String uid) {
        List<Double> allocated = peMap.computeIfAbsent(uid, k -> new ArrayList<>());
        return allocated.stream().mapToDouble(Double::valueOf).sum();
    }

    @Override
    public double getAllocatedMipsForContainerVmByVirtualPeId(ContainerVm containerVm, int i) {
        if (i < 0) {
            throw new IllegalArgumentException("i is less than 0");
        }
        List<Double> allocated = getAllocatedMipsForContainerVm(containerVm);
        return allocated != null ? allocated.get(i) : 0;
    }

    @Override
    public void deallocateMipsForContainerVm(ContainerVm containerVm) {
        double allocatedMips = getTotalAllocatedMipsForContainerVm(containerVm);
        setAvailableMips(getAvailableMips() + allocatedMips);
        peMap.remove(containerVm.getUid());
    }

    public void deallocateMipsForAllContainerVms() {
        super.deallocateMipsForAllContainerVms();
        peMap.clear();
    }
}
