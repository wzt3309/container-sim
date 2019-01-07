package paperlab.ccsim.core;

import paperlab.ccsim.provisioner.container.ContainerPeProvisioner;

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

  public ContainerPeProvisioner getContainerPeProvisioner() {
    return containerPeProvisioner;
  }

  public void setContainerPeProvisioner(ContainerPeProvisioner containerPeProvisioner) {
    this.containerPeProvisioner = containerPeProvisioner;
  }
}
