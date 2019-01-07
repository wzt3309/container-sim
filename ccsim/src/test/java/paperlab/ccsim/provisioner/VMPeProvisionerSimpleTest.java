package paperlab.ccsim.provisioner;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import paperlab.ccsim.core.VM;
import paperlab.ccsim.provisioner.vm.VMPeProvisioner;
import paperlab.ccsim.provisioner.vm.impl.VMPeProvisionerSimple;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VMPeProvisionerSimpleTest {

  private VMPeProvisioner provisioner;

  @Before
  public void setup() {
    provisioner = new VMPeProvisionerSimple(1000.0);
  }

  @Test
  public void allocateMipsForContainerVm() {
    VM vm1 = mock(VM.class);
    when(vm1.getUid()).thenReturn("1-1");
    provisioner.allocateMipsForContainerVm(vm1, 200.0);
    assertThat(provisioner.getTotalAllocatedMips()).isEqualTo(200.0);
    assertThat(provisioner.getAvailableMips()).isEqualTo(1000.0 - 200.0);
    assertThat(provisioner.getAllocatedMipsForContainerVm(vm1)).containsOnly(200.0);
    assertThat(provisioner.getTotalAllocatedMipsForContainerVm(vm1)).isEqualTo(200.0);

    VM vm2 = mock(VM.class);
    when(vm2.getUid()).thenReturn("1-2");
    provisioner.allocateMipsForContainerVm(vm2, Arrays.asList(200.0, 100.0, 100.0));
    assertThat(provisioner.getTotalAllocatedMips()).isEqualTo(600.0);
    assertThat(provisioner.getAvailableMips()).isEqualTo(1000.0 - 600.0);
    assertThat(provisioner.getAllocatedMipsForContainerVm(vm2)).containsOnly(200.0, 100.0, 100.0);
    assertThat(provisioner.getTotalAllocatedMipsForContainerVm(vm2)).isEqualTo(400.0);

    VM vm3 = mock(VM.class);
    when(vm3.getUid()).thenReturn("1-3");
    assertThat(provisioner.allocateMipsForContainerVm(vm3, Arrays.asList(200.0, 100.0, 200.0))).isFalse();
    assertThat(provisioner.getTotalAllocatedMips()).isEqualTo(600.0);
    assertThat(provisioner.getAvailableMips()).isEqualTo(1000.0 - 600.0);
    assertThat(provisioner.getAllocatedMipsForContainerVm(vm3)).isNull();
    assertThat(provisioner.getTotalAllocatedMipsForContainerVm(vm3)).isZero();

    provisioner.allocateMipsForContainerVm(vm3, Arrays.asList(200.0, 100.0));
    // 必须是mutable的list，才可在后面增加mips
    provisioner.allocateMipsForContainerVm(vm3, Lists.newArrayList(100.0, 100.0));
    assertThat(provisioner.getAllocatedMipsForContainerVm(vm3)).containsOnly(100.0, 100.0);
    provisioner.allocateMipsForContainerVm(vm3, 100.0);
    assertThat(provisioner.getAllocatedMipsForContainerVm(vm3)).containsOnly(100.0, 100.0, 100.0);
    assertThat(provisioner.getTotalAllocatedMips()).isEqualTo(900.0);
    assertThat(provisioner.getAvailableMips()).isEqualTo(1000.0 - 900.0);
  }

  @Test
  public void getAllocatedMipsForContainerVmByVirtualPeId() {
    VM vm = mock(VM.class);
    when(vm.getUid()).thenReturn("1-1");
    provisioner.allocateMipsForContainerVm(vm, Arrays.asList(200.0, 100.0, 100.0));
    assertThat(provisioner.getAllocatedMipsForContainerVmByVirtualPeId(vm, 0)).isEqualTo(200.0);
    assertThat(provisioner.getAllocatedMipsForContainerVmByVirtualPeId(vm, 1)).isEqualTo(100.0);
    assertThat(provisioner.getAllocatedMipsForContainerVmByVirtualPeId(vm, 2)).isEqualTo(100.0);
  }

  @Test
  public void deallocateMipsForContainerVm() {
    VM vm = mock(VM.class);
    when(vm.getUid()).thenReturn("1-1");
    provisioner.allocateMipsForContainerVm(vm, Arrays.asList(200.0, 100.0, 100.0));
    assertThat(provisioner.getTotalAllocatedMips()).isEqualTo(400.0);
    assertThat(provisioner.getAvailableMips()).isEqualTo(1000.0 - 400.0);

    provisioner.deallocateMipsForContainerVm(vm);
    assertThat(provisioner.getTotalAllocatedMips()).isEqualTo(0.0);
    assertThat(provisioner.getAvailableMips()).isEqualTo(1000.0 - 0.0);
    assertThat(provisioner.getAllocatedMipsForContainerVm(vm)).isNull();
    assertThat(provisioner.getTotalAllocatedMipsForContainerVm(vm)).isZero();

    VM vm2 = mock(VM.class);
    when(vm2.getUid()).thenReturn("1-2");
    provisioner.allocateMipsForContainerVm(vm, Arrays.asList(200.0, 100.0, 100.0));
    provisioner.allocateMipsForContainerVm(vm2, Arrays.asList(200.0, 100.0, 100.0));
    assertThat(provisioner.getTotalAllocatedMips()).isEqualTo(800.0);
    assertThat(provisioner.getAvailableMips()).isEqualTo(1000.0 - 800.0);

    provisioner.deallocatedMipsForAllContainerVms();
    assertThat(provisioner.getTotalAllocatedMips()).isEqualTo(0.0);
    assertThat(provisioner.getAvailableMips()).isEqualTo(1000.0 - 0.0);
    assertThat(provisioner.getAllocatedMipsForContainerVm(vm)).isNull();
    assertThat(provisioner.getAllocatedMipsForContainerVm(vm2)).isNull();
    assertThat(provisioner.getTotalAllocatedMipsForContainerVm(vm)).isZero();
    assertThat(provisioner.getTotalAllocatedMipsForContainerVm(vm2)).isZero();
  }
}