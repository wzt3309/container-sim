package paperlab.ccsim.schedulers;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmPe;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmPeProvisioner;
import org.cloudbus.cloudsim.container.schedulers.ContainerVmSchedulerTimeSharedOverSubscription;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 主机内部调度供VM使用的Pe，负责分配Pe和更新Pe状态，允许VM超量预定Pe
 */
public class ContainerVmSchedulerTimeSharedOverSubscriptionZipped extends ContainerVmSchedulerTimeSharedOverSubscription {

    public ContainerVmSchedulerTimeSharedOverSubscriptionZipped(List<? extends ContainerVmPe> pelist) {
        super(pelist);
    }

    @Override
    protected void updatePeProvisioning() {
        Log.printLine("VmSchedulerTimeShared: update the pe provisioning......");
        getPeMap().clear();
        for (ContainerVmPe pe : getPeList()) {
            pe.getContainerVmPeProvisioner().deallocateMipsForAllContainerVms();
        }

        Iterator<ContainerVmPe> peIterator = getPeList().iterator();
        ContainerVmPe pe = peIterator.next();
        ContainerVmPeProvisioner containerVmPeProvisioner = pe.getContainerVmPeProvisioner();
        double availableMips = containerVmPeProvisioner.getAvailableMips();
        int idx = 0, peSize = getPeList().size();

        for (Map.Entry<String, List<Double>> entry : getMipsMap().entrySet()) {
            String vmUid = entry.getKey();
            getPeMap().put(vmUid, new LinkedList<>());

            for (double mips : entry.getValue()) {
                while (mips >= 0.1) {
                    if (availableMips >= mips) {
                        containerVmPeProvisioner.allocateMipsForContainerVm(vmUid, mips);
                        getPeMap().get(vmUid).add(pe);
                        availableMips -= mips;
                        break;
                    } else {
                        containerVmPeProvisioner.allocateMipsForContainerVm(vmUid, availableMips);
                        if(availableMips != 0){
                            getPeMap().get(vmUid).add(pe);
                        }
                        mips -= availableMips;
                        if (mips <= 0.1) {
                            break;
                        }
                        if (!peIterator.hasNext()) {
                            ContainerVmPe zippedPe = getPeList().get(idx++ % peSize);
                            ContainerVmPeProvisioner zippedPeProvisioner = zippedPe.getContainerVmPeProvisioner();
                            zippedPeProvisioner.allocateMipsForContainerVm(vmUid, mips);
                            getPeMap().get(vmUid).add(pe);
                            continue;
                        }
                        pe = peIterator.next();
                        containerVmPeProvisioner = pe.getContainerVmPeProvisioner();
                        availableMips = containerVmPeProvisioner.getAvailableMips();
                    }
                }
            }
        }
    }
}
