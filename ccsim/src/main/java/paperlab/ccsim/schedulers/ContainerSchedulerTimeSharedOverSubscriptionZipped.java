package paperlab.ccsim.schedulers;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.container.containerProvisioners.ContainerPe;
import org.cloudbus.cloudsim.container.containerProvisioners.ContainerPeProvisioner;
import org.cloudbus.cloudsim.container.schedulers.ContainerSchedulerTimeSharedOverSubscription;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * VM内部调度供容器使用的Pe，负责分配Pe和更新Pe状态，允许容器超量预定Pe
 */
public class ContainerSchedulerTimeSharedOverSubscriptionZipped extends ContainerSchedulerTimeSharedOverSubscription {
    public ContainerSchedulerTimeSharedOverSubscriptionZipped(List<? extends ContainerPe> pelist) {
        super(pelist);
    }

    @Override
    protected void updatePeProvisioning() {
        Log.printLine("VmSchedulerTimeShared: update the pe provisioning......");
        getPeMap().clear();
        for (ContainerPe pe : getPeList()) {
            pe.getContainerPeProvisioner().deallocateMipsForAllContainers();
        }

        Iterator<ContainerPe> peIterator = getPeList().iterator();
        ContainerPe pe = peIterator.next();
        ContainerPeProvisioner containerPeProvisioner = pe.getContainerPeProvisioner();
        double availableMips = containerPeProvisioner.getAvailableMips();
        int idx = 0, peSize = getPeList().size();

        for (Map.Entry<String, List<Double>> entry : getMipsMap().entrySet()) {
            String containerUid = entry.getKey();
            getPeMap().put(containerUid, new LinkedList<>());

            for (double mips : entry.getValue()) {
                while (mips >= 0.1) {
                    if (availableMips >= mips) {
                        containerPeProvisioner.allocateMipsForContainer(containerUid, mips);
                        getPeMap().get(containerUid).add(pe);
                        availableMips -= mips;
                        break;
                    } else {
                        containerPeProvisioner.allocateMipsForContainer(containerUid, availableMips);
                        if (availableMips != 0) {
                            getPeMap().get(containerUid).add(pe);
                        }
                        mips -= availableMips;
                        if (mips <= 0.1) {
                            break;
                        }
                        if (!peIterator.hasNext()) {
                            ContainerPe zippedPe = getPeList().get(idx++ % peSize);
                            ContainerPeProvisioner zippedPeProvisioner = zippedPe.getContainerPeProvisioner();
                            zippedPeProvisioner.allocateMipsForContainer(containerUid, mips);
                            getPeMap().get(containerUid).add(pe);
                            continue;
                        }
                        pe = peIterator.next();
                        containerPeProvisioner = pe.getContainerPeProvisioner();
                        availableMips = containerPeProvisioner.getAvailableMips();
                    }
                }
            }
        }
//
    }
}
