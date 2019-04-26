package paperlab.ccsim.provisioners;

import org.cloudbus.cloudsim.container.containerProvisioners.ContainerPeProvisioner;
import org.cloudbus.cloudsim.container.core.Container;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * VM分配Pe给container的方案，当container需求的mips大于可提供的availableMips时，会根据各container总需求的占比对分配方案进行压缩
 *
 * 如：设置VM总mips = 600， co1 = [100, 100] co2 = [200, 200] available = 600 - (100 * 2 + 200 * 2) = 0
 * 当co2又需要200时，则进行压缩
 * totalRequestedMips = 100 * 2 + 200 * 3 = mips - available + 200 = 800
 * zipped_mips_co1 = (100 * 2) / 800 * 600 =>  new co1 = [100 / 200 , 100 / 200] * zipped_mips_co1
 * zipped_mips_co2 = (200 * 3) / 800 * 600 =>  new co2 = [200 / 600, 200 / 600, 200 / 600] * zipped_mips_co2
 *
 */
public class ContainerPeProvisionerZipped extends ContainerPeProvisioner {
    private Map<String, List<Double>> peMap;

    public ContainerPeProvisionerZipped(double mips) {
        super(mips);
        this.peMap = new HashMap<>();
    }

    @Override
    public boolean allocateMipsForContainer(Container container, double mips) {
        return allocateMipsForContainer(container.getUid(), mips);
    }

    @Override
    public boolean allocateMipsForContainer(String s, double mips) {
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
    public boolean allocateMipsForContainer(Container container, List<Double> mipsList) {
        if (mipsList == null || mipsList.isEmpty()) {
            throw new IllegalArgumentException("mips is null or empty");
        }
        String uid = container.getUid();
        double oldAllocatedMips = getTotalAllocatedMipsForContainerVm(uid);
        double nowAllocatedMips = mipsList.stream().mapToDouble(Double::valueOf).sum();
        // 更新container的pe分配列表
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
    public List<Double> getAllocatedMipsForContainer(Container container) {
        return peMap.computeIfPresent(container.getUid(), (k, oldVal) -> oldVal);
    }

    @Override
    public double getTotalAllocatedMipsForContainer(Container container) {
        return getTotalAllocatedMipsForContainerVm(container.getUid());
    }

    private double getTotalAllocatedMipsForContainerVm(String uid) {
        List<Double> allocated = peMap.computeIfAbsent(uid, k -> new ArrayList<>());
        return allocated.stream().mapToDouble(Double::valueOf).sum();
    }

    @Override
    public double getAllocatedMipsForContainerByVirtualPeId(Container container, int i) {
        if (i < 0) {
            throw new IllegalArgumentException("i is less than 0");
        }
        List<Double> allocated = getAllocatedMipsForContainer(container);
        return allocated != null ? allocated.get(i) : 0;
    }

    @Override
    public void deallocateMipsForContainer(Container container) {
        super.deallocateMipsForAllContainers();
        peMap.clear();
    }
}
