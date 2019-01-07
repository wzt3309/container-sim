package paperlab.ccsim.provisioner.container;

import paperlab.ccsim.core.Container;

public abstract class ContainerBwProvisioner {
  private long bw;
  private long availableBw;

  public ContainerBwProvisioner(long bw) {
    this.bw = bw;
    this.availableBw = bw;
  }

  public abstract boolean allocateBwForContainer(Container container, long bw);

  public abstract long getAllocatedBwForContainer(Container container);

  public abstract void deallocatedBwForContainer(Container container);

  public void deallocatedBwForAllContainers() {
    availableBw = bw;
  }

  public abstract boolean isSuitableForContainer(Container container, long bw);

  public long getUsedBw() {
    return bw - availableBw;
  }

  public long getBw() {
    return bw;
  }

  protected void setBw(long bw) {
    this.bw = bw;
  }

  public long getAvailableBw() {
    return availableBw;
  }

  protected void setAvailableBw(long availableBw) {
    this.availableBw = availableBw;
  }
}
