package paperlab.ccsim.provisioner.container.impl;

import org.cloudbus.cloudsim.container.containerProvisioners.ContainerRamProvisioner;
import org.cloudbus.cloudsim.container.core.Container;

import java.util.HashMap;
import java.util.Map;

public class ContainerRamProvisionerImpl extends ContainerRamProvisioner {

  private Map<String, Float> ramTable;

  public ContainerRamProvisionerImpl(long ram) {
    super(ram);
    ramTable = new HashMap<>();
  }

  @Override
  public boolean allocateRamForContainer(Container container, float ram) {
    float availableRam = getAvailableVmRam();
    float allocatedRamForContainerVm = getAllocatedRamForContainer(container);
    if (isSuitableForContainer(container, ram)) {
      availableRam -= (ram - allocatedRamForContainerVm);
      setAvailableVmRam(availableRam);
      ramTable.put(container.getUid(), ram);
      container.setCurrentAllocatedRam(ram);
      return true;
    }
    return false;
  }

  @Override
  public float getAllocatedRamForContainer(Container container) {
    float allocatedRam = 0;
    String uid = container.getUid();
    if (ramTable.containsKey(uid)) {
      allocatedRam = ramTable.get(uid);
    }
    return allocatedRam;
  }

  @Override
  public void deallocateRamForContainer(Container container) {
    float allocatedRam = getAllocatedRamForContainer(container);
    setAvailableVmRam(getAvailableVmRam() + allocatedRam);
    ramTable.remove(container.getUid());
    container.setCurrentAllocatedRam(0);
  }

  @Override
  public void deallocateRamForAllContainers() {
    super.deallocateRamForAllContainers();
    ramTable.clear();
  }

  @Override
  public boolean isSuitableForContainer(Container container, float ram) {
    return getAvailableVmRam() + getAllocatedRamForContainer(container) >= ram;
  }

  protected Map<String, Float> getRamTable() {
    return ramTable;
  }

  protected void setRamTable(Map<String, Float> ramTable) {
    this.ramTable = ramTable;
  }
}
