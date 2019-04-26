package paperlab.ccsim.core;

import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmBwProvisioner;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmPe;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmRamProvisioner;
import org.cloudbus.cloudsim.container.core.ContainerVm;
import org.cloudbus.cloudsim.container.core.PowerContainerHost;
import org.cloudbus.cloudsim.container.schedulers.ContainerVmScheduler;
import org.cloudbus.cloudsim.power.models.PowerModel;

import java.util.List;

/**
 * 模拟运行虚拟机的主机
 */
public class PowerContainerHostZipped extends PowerContainerHost {
    private boolean isZipped = false;

    public PowerContainerHostZipped(int id, ContainerVmRamProvisioner ramProvisioner, ContainerVmBwProvisioner bwProvisioner, long storage, List<? extends ContainerVmPe> peList, ContainerVmScheduler vmScheduler, PowerModel powerModel) {
        super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler, powerModel);
    }

    @Override
    public boolean isSuitableForContainerVm(ContainerVm vm) {
        boolean isSuitable = true;
        if (getContainerVmRamProvisioner().isSuitableForContainerVm(vm, vm.getCurrentRequestedRam())
                && getContainerVmBwProvisioner().isSuitableForContainerVm(vm, vm.getCurrentRequestedBw())) {
            // pe被压缩过，不适合继续创建容器
            if (isZipped) {
                isSuitable = false;
            }
            // pe没有被压缩过，如果需要压缩，则置isZipped = true，允许进行一次压缩
            if (getContainerVmScheduler().getPeCapacity() < vm.getCurrentRequestedMaxMips()
                    || getContainerVmScheduler().getAvailableMips() < vm.getCurrentRequestedTotalMips()) {
                isZipped = true;
            }
        } else {
            isSuitable = false;
        }
        return isSuitable;
    }
}
