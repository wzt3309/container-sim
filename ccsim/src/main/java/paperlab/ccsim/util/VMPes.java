package paperlab.ccsim.util;

import paperlab.ccsim.core.VMPe;

import java.util.List;

public final class VMPes {
  private VMPes() {}

  public static <T extends VMPe> double getTotalMips(List<T> peList) {
    if (peList == null) {
      return 0;
    }

    double totalMips = 0;
    for (VMPe pe: peList) {
      totalMips += pe.getMips();
    }
    return totalMips;
  }
}
