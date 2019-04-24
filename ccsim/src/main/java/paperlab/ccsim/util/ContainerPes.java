package paperlab.ccsim.util;

import org.cloudbus.cloudsim.container.containerProvisioners.ContainerPe;

import java.util.List;

public final class ContainerPes {
  private ContainerPes() {}

  public static <T extends ContainerPe> double getTotalMips(List<T> peList) {
    if (peList == null || peList.isEmpty()) {
      return 0;
    }

    return peList.stream().mapToDouble(ContainerPe::getMips).sum();
  }
}
