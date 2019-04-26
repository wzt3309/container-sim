package paperlab.ccsim.core;

import org.cloudbus.cloudsim.container.containerProvisioners.ContainerBwProvisioner;
import org.cloudbus.cloudsim.container.containerProvisioners.ContainerPe;
import org.cloudbus.cloudsim.container.containerProvisioners.ContainerRamProvisioner;
import org.cloudbus.cloudsim.container.core.Container;
import org.cloudbus.cloudsim.container.core.PowerContainerVm;
import org.cloudbus.cloudsim.container.schedulers.ContainerScheduler;

import java.util.List;

/**
 * 模拟运行容器的虚拟机
 */
public class PowerContainerVmZipped extends PowerContainerVm {
    private boolean isZipped = false;

    public PowerContainerVmZipped(int id, int userId, double mips, float ram, long bw, long size, String vmm, ContainerScheduler containerScheduler, ContainerRamProvisioner containerRamProvisioner, ContainerBwProvisioner containerBwProvisioner, List<? extends ContainerPe> peList, double schedulingInterval) {
        super(id, userId, mips, ram, bw, size, vmm, containerScheduler, containerRamProvisioner, containerBwProvisioner, peList, schedulingInterval);
    }

    @Override
    public boolean isSuitableForContainer(Container container) {
        boolean isSuitable = true;
        if (getContainerRamProvisioner().isSuitableForContainer(container, container.getCurrentRequestedRam()) && getContainerBwProvisioner()
                .isSuitableForContainer(container, container.getCurrentRequestedBw())) {
            // pe被压缩过，不适合继续创建容器
            if (isZipped) {
                isSuitable = false;
            }
            // pe没有被压缩过，如果需要压缩，则置isZipped = true，允许进行一次压缩
            if (getContainerScheduler().getPeCapacity() < container.getCurrentRequestedMaxMips()
                    || getContainerScheduler().getAvailableMips() < container.getWorkloadTotalMips()) {
                isZipped = true;
            }
        } else {
            isSuitable = false;
        }
        return isSuitable;
    }
}
