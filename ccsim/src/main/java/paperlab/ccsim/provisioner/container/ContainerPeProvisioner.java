package paperlab.ccsim.provisioner.container;

import paperlab.ccsim.core.Container;

import java.util.List;

/**
 * 在虚拟机内给容器分配Pe
 * mips可以看成是一个时间片内的时间总量，在每个时间片内运行容器时，需要将这个时间片内的
 * mips分配给相应的容器
 */
public abstract class ContainerPeProvisioner {
  // 虚拟机内能分配给容器的mips总量
  private double mips;
  // 虚拟机内还可以继续分配给容器的mips量
  private double availableMips;


  public ContainerPeProvisioner(double mips) {
    this.mips = mips;
    this.availableMips = mips;
  }

  public abstract boolean allocateMipsForContainer(Container container, double mips);

  public abstract boolean allocateMipsForContainer(String containerUid, double mips);

  public abstract boolean allocateMipsForContainer(Container container, List<Double> mips);

  public abstract List<Double> getAllocatedMipsForContainer(Container container);

  public abstract double getTotalAllocatedMipsForContainer(Container container);

  public abstract double getAllocatedMipsForContainerByVirtualPeId(Container container, int peId);

  public abstract void deallocateMipsForContainer(Container container);

  public void deallocatedMipsForAllContainers() {
    availableMips = mips;
  }

  public double getMips() {
    return mips;
  }

  protected void setMips(double mips) {
    this.mips = mips;
  }

  public double getAvailableMips() {
    return availableMips;
  }

  protected void setAvailableMips(double availableMips) {
    this.availableMips = availableMips;
  }

  public double getTotalAllocatedMips() {
    return mips - availableMips;
  }

  public double getUtilization() {
    return getTotalAllocatedMips() / mips;
  }
}
