package paperlab.ccsim.provisioner.container.impl;

import org.cloudbus.cloudsim.container.containerProvisioners.ContainerPeProvisioner;
import org.cloudbus.cloudsim.container.core.Container;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContainerPeProvisionerImpl extends ContainerPeProvisioner {
  // value是分配给容器每个Pe的mips量，list的下标即为Pe的id
  private Map<String, List<Double>> mipsTable;

  public ContainerPeProvisionerImpl(double mips) {
    super(mips);
    mipsTable = new HashMap<>();
  }

  @Override
  public boolean allocateMipsForContainer(Container container, double mips) {
    return allocateMipsForContainer(container.getUid(), mips);
  }

  @Override
  public boolean allocateMipsForContainer(String containerUid, double mips) {
    if (getAvailableMips() < mips) {
      return false;
    }
    mipsTable.computeIfAbsent(containerUid, uid -> new ArrayList<>()).add(mips);
    setAvailableMips(getAvailableMips() - mips);
    return true;
  }

  @Override
  public boolean allocateMipsForContainer(Container container, List<Double> mips) {
    double totalMipsToAllocate = mips.stream().mapToDouble(d -> d).sum();
    if (getAvailableMips() + getTotalAllocatedMipsForContainer(container) < totalMipsToAllocate) {
      return false;
    }
    setAvailableMips(getAvailableMips() + getTotalAllocatedMipsForContainer(container) - totalMipsToAllocate);
    mipsTable.put(container.getUid(), mips);
    return true;
  }

  @Override
  public List<Double> getAllocatedMipsForContainer(Container container) {
    return mipsTable.get(container.getUid());
  }

  @Override
  public double getTotalAllocatedMipsForContainer(Container container) {
    List<Double> mips = getAllocatedMipsForContainer(container);
    double totalAllocatedMips = 0;
    if (mips != null) {
      totalAllocatedMips = mips.stream().mapToDouble(d -> d).sum();
    }
    return totalAllocatedMips;
  }

  @Override
  public double getAllocatedMipsForContainerByVirtualPeId(Container container, int peId) {
    List<Double> mips = getAllocatedMipsForContainer(container);
    if (mips != null && peId >= 0 && peId < mips.size()) {
      return mips.get(peId);
    }
    return 0;
  }

  @Override
  public void deallocateMipsForContainer(Container container) {
    double totalAllocateMipsForContainer = getTotalAllocatedMipsForContainer(container);
    setAvailableMips(getAvailableMips() + totalAllocateMipsForContainer);
    mipsTable.remove(container.getUid());
  }

  @Override
  public void deallocateMipsForAllContainers() {
    super.deallocateMipsForAllContainers();
    mipsTable.clear();
  }

  protected Map<String, List<Double>> getMipsTable() {
    return mipsTable;
  }

  protected void setMipsTable(Map<String, List<Double>> mipsTable) {
    this.mipsTable = mipsTable;
  }
}
