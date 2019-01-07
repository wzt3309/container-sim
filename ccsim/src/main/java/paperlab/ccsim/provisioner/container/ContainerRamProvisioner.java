package paperlab.ccsim.provisioner.container;

import paperlab.ccsim.core.Container;

public abstract class ContainerRamProvisioner {
  private long ram;
  private long availableRam;

  public ContainerRamProvisioner(long ram) {
    this.ram = ram;
    this.availableRam = ram;
  }

  public abstract boolean allocateRamForContainer(Container container, long ram);

  public abstract long getAllocatedRamForContainer(Container container);

  public abstract void deallocateRamForContainer(Container container);

  public void deallocateRamForAllConatiners() {
    availableRam = ram;
  }

  public abstract boolean isSuitableForContainer(Container container, long ram);

  public long getUsedRam() {
    return ram - availableRam;
  }

  public long getRam() {
    return ram;
  }

  protected void setRam(long ram) {
    this.ram = ram;
  }

  public long getAvailableRam() {
    return availableRam;
  }

  protected void setAvailableRam(long availableRam) {
    this.availableRam = availableRam;
  }
}
