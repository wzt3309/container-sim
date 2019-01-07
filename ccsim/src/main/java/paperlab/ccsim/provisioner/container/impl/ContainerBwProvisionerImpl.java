package paperlab.ccsim.provisioner.container.impl;

import paperlab.ccsim.core.Container;
import paperlab.ccsim.provisioner.container.ContainerBwProvisioner;

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
    long availableBw = getAvailableBw();
    long allocatedBwForContainerVm = getAllocatedBwForContainer(container);
    if (isSuitableForContainer(container, bw)) {
      availableBw -= (bw - allocatedBwForContainerVm);
      setAvailableBw(availableBw);
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
  public void deallocatedBwForContainer(Container container) {
    long allocatedBw = getAllocatedBwForContainer(container);
    setAvailableBw(getAvailableBw() + allocatedBw);
    bwTable.remove(container.getUid());
    container.setCurrentAllocatedBw(0);
  }

  @Override
  public void deallocatedBwForAllContainers() {
    super.deallocatedBwForAllContainers();
    bwTable.clear();
  }

  @Override
  public boolean isSuitableForContainer(Container container, long bw) {
    return getAvailableBw() + getAllocatedBwForContainer(container) >= bw;
  }

  protected Map<String, Long> getBwTable() {
    return bwTable;
  }

  protected void setBwTable(Map<String, Long> bwTable) {
    this.bwTable = bwTable;
  }
}
