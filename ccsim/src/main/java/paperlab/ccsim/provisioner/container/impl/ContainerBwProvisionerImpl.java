package paperlab.ccsim.provisioner.container.impl;

import org.cloudbus.cloudsim.container.containerProvisioners.ContainerBwProvisioner;
import org.cloudbus.cloudsim.container.core.Container;

import java.util.HashMap;
import java.util.Map;

public class ContainerBwProvisionerImpl extends ContainerBwProvisioner {
  private Map<String, Long> bwTable;

  public ContainerBwProvisionerImpl(long bw) {
    super(bw);
    bwTable = new HashMap<>();
  }


  @Override
  public boolean allocateBwForContainer(Container container, long bw) {
    long availableBw = getAvailableVmBw();
    long allocatedBwForContainerVm = getAllocatedBwForContainer(container);
    if (isSuitableForContainer(container, bw)) {
      availableBw -= (bw - allocatedBwForContainerVm);
      setAvailableVmBw(availableBw);
      bwTable.put(container.getUid(), bw);
      container.setCurrentAllocatedBw(bw);
      return true;
    }
    return false;
  }

  @Override
  public long getAllocatedBwForContainer(Container container) {
    long allocatedBw = 0;
    String uid = container.getUid();
    if (bwTable.containsKey(uid)) {
      allocatedBw = bwTable.get(uid);
    }
    return allocatedBw;
  }

  @Override
  public void deallocateBwForContainer(Container container) {
    long allocatedBw = getAllocatedBwForContainer(container);
    setAvailableVmBw(getAvailableVmBw() + allocatedBw);
    bwTable.remove(container.getUid());
    container.setCurrentAllocatedBw(0);
  }

  @Override
  public boolean isSuitableForContainer(Container container, long bw) {
    return getAvailableVmBw() + getAllocatedBwForContainer(container) >= bw;
  }

  @Override
  public void deallocateBwForAllContainers() {
    super.deallocateBwForAllContainers();
    bwTable.clear();
  }

  protected Map<String, Long> getBwTable() {
    return bwTable;
  }

  protected void setBwTable(Map<String, Long> bwTable) {
    this.bwTable = bwTable;
  }
}
