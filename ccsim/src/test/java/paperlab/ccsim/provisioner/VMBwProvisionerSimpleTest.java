package paperlab.ccsim.provisioner;

import org.junit.Before;
import org.junit.Test;
import paperlab.ccsim.core.VM;
import paperlab.ccsim.provisioner.vm.VMBwProvisioner;
import paperlab.ccsim.provisioner.vm.impl.VMBwProvisionerSimple;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class VMBwProvisionerSimpleTest {
  private VMBwProvisioner provisioner;
  private final long bw = 1000L;

  @Before
  public void setup() {
    provisioner = new VMBwProvisionerSimple(bw);
  }

  @Test
  public void allocateBwForContainerVm() {
    long[] bwAllocs = {100L, 200L, 300L};
    VM[] vms = new VM[2];
    for (int i = 0; i < 2; i++) {
      vms[i] = mock(VM.class);
      when(vms[i].getUid()).thenReturn("1-" + i);
      when(vms[i].getCurrentAllocatedBw()).thenCallRealMethod();
      for (long bwAlloc: bwAllocs) {
        doCallRealMethod().when(vms[i]).setCurrentAllocatedBw(bwAlloc);
      }
    }

    provisioner.allocateBwForContainerVm(vms[0], bwAllocs[0]);
    assertThat(provisioner.getAllocatedBwForContainerVm(vms[0])).isEqualTo(bwAllocs[0]);
    assertThat(provisioner.getUsedBw()).isEqualTo(bwAllocs[0]);
    assertThat(provisioner.getAvailableBw()).isEqualTo(bw - bwAllocs[0]);
    assertThat(vms[0].getCurrentAllocatedBw()).isEqualTo(bwAllocs[0]);

    provisioner.allocateBwForContainerVm(vms[0], bwAllocs[1]);
    assertThat(provisioner.getAllocatedBwForContainerVm(vms[0])).isEqualTo(bwAllocs[1]);
    assertThat(provisioner.getUsedBw()).isEqualTo(bwAllocs[1]);
    assertThat(provisioner.getAvailableBw()).isEqualTo(bw - bwAllocs[1]);
    assertThat(vms[0].getCurrentAllocatedBw()).isEqualTo(bwAllocs[1]);

    provisioner.allocateBwForContainerVm(vms[1], bwAllocs[2]);
    assertThat(provisioner.getAllocatedBwForContainerVm(vms[1])).isEqualTo(bwAllocs[2]);
    assertThat(provisioner.getUsedBw()).isEqualTo(bwAllocs[1] + bwAllocs[2]);
    assertThat(provisioner.getAvailableBw()).isEqualTo(bw - bwAllocs[1] - bwAllocs[2]);
    assertThat(vms[1].getCurrentAllocatedBw()).isEqualTo(bwAllocs[2]);

    assertThat(provisioner.isSuitableForContainerVm(vms[1], bwAllocs[2] * 3)).isFalse();
    assertThat(provisioner.allocateBwForContainerVm(vms[1], bwAllocs[2] * 3)).isFalse();
    assertThat(provisioner.getAllocatedBwForContainerVm(vms[1])).isEqualTo(bwAllocs[2]);
    assertThat(provisioner.getUsedBw()).isEqualTo(bwAllocs[1] + bwAllocs[2]);
    assertThat(vms[1].getCurrentAllocatedBw()).isEqualTo(bwAllocs[2]);
  }

  @Test
  public void deallocateBwForContainerVm() {
    long bwAlloc = 200L;
    VM vm = mock(VM.class);
    when(vm.getUid()).thenReturn("1-1");
    when(vm.getCurrentAllocatedBw()).thenCallRealMethod();
    doCallRealMethod().when(vm).setCurrentAllocatedBw(bwAlloc);
    doCallRealMethod().when(vm).setCurrentAllocatedBw(0);

    assertThat(provisioner.allocateBwForContainerVm(vm, bwAlloc)).isTrue();
    provisioner.deallocateBwForContainerVm(vm);
    assertThat(provisioner.getAvailableBw()).isEqualTo(provisioner.getBw());
    assertThat(provisioner.getUsedBw()).isZero();
    assertThat(vm.getCurrentAllocatedBw()).isZero();

    VM vm2 = mock(VM.class);
    VM vm3 = mock(VM.class);
    when(vm.getUid()).thenReturn("1-2");
    when(vm.getUid()).thenReturn("1-3");

    assertThat(provisioner.allocateBwForContainerVm(vm2, bwAlloc)).isTrue();
    assertThat(provisioner.allocateBwForContainerVm(vm3, bwAlloc)).isTrue();
    provisioner.deallocatedBwForContainerVms();
    assertThat(provisioner.getAvailableBw()).isEqualTo(provisioner.getBw());
    assertThat(provisioner.getUsedBw()).isZero();
    assertThat(provisioner.getAllocatedBwForContainerVm(vm2)).isZero();
    assertThat(provisioner.getAllocatedBwForContainerVm(vm3)).isZero();

  }
}