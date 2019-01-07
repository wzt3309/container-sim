package paperlab.ccsim.provisioner.container.impl;

import paperlab.ccsim.core.Container;
import paperlab.ccsim.provisioner.container.ContainerRamProvisioner;

import java.util.HashMap;
import java.util.Map;

public class ContainerRamProvisionerSimple extends ContainerRamProvisioner {

  private Map<String, Long> ramTable;

  public ContainerRamProvisionerSimple(long ram) {
    super(ram);
    ramTable = new HashMap<>();
  }

  @Override
  public boolean allocateRamForContainer(Container container, long ram) {
    long availableRam = getAvailableRam();
    long allocatedRamForContainerVm = getAllocatedRamForContainer(container);
    if (isSuitableForContainer(container, ram)) {
      availableRam -= (ram - allocatedRamForContainerVm);
      setAvailableRam(availableRam);
      ramTable.put(container.getUid(), ram);
      container.setCurrentAllocatedRam(ram);
      return true;
    }
    return false;
  }

  @Override
  public long getAllocatedRamForContainer(Container container) {
    long allocatedRam = 0;
    String uid = container.getUid();
    if (ramTable.containsKey(uid)) {
      allocatedRam = ramTable.get(uid);
    }
    return allocatedRam;
  }

  @Override
  public void deallocateRamForContainer(Container container) {
    long allocatedRam = getAllocatedRamForContainer(container);
    setAvailableRam(getAvailableRam() + allocatedRam);
    ramTable.remove(container.getUid());
    container.setCurrentAllocatedRam(0);
  }

  @Override
  public void deallocateRamForAllConatiners() {
    super.deallocateRamForAllConatiners();
    ramTable.clear();
  }

  @Override
  public boolean isSuitableForContainer(Container container, long ram) {
    return getAvailableRam() + getAllocatedRamForContainer(container) >= ram;
  }

  protected Map<String, Long> getRamTable() {
    return ramTable;
  }

  protected void setRamTable(Map<String, Long> ramTable) {
    this.ramTable = ramTable;
  }
}
