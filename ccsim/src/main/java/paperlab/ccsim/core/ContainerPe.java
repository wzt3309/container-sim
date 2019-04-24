package paperlab.ccsim.core;

/**
 * 代表vm中分配给container的处理器
 */
public class ContainerPe {
  private int id;
  private ContainerPeProvisioner containerPeProvisioner;

  public ContainerPe(int id, ContainerPeProvisioner containerPeProvisioner) {
    this.id = id;
    this.containerPeProvisioner = containerPeProvisioner;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public double getMips() {
    return containerPeProvisioner.getMips();
  }

  public void setMips(double mips) {
    containerPeProvisioner.setMips(mips);
  }

  public double getAvailableMips() {
    return containerPeProvisioner.getAvailableMips();
  }

  public void deallocatedMipsForAllContainers() {
    containerPeProvisioner.deallocatedMipsForAllContainers();
  }

  public boolean allocateMipsForContainer(String containerUid, double mips) {
    return containerPeProvisioner.allocateMipsForContainer(containerUid, mips);
  }

  public ContainerPeProvisioner getContainerPeProvisioner() {
    return containerPeProvisioner;
  }

  public void setContainerPeProvisioner(ContainerPeProvisioner containerPeProvisioner) {
    this.containerPeProvisioner = containerPeProvisioner;
  }
}
