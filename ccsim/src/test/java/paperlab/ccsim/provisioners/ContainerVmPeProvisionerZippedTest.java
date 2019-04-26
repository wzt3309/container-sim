package paperlab.ccsim.provisioners;

import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmPeProvisioner;
import org.cloudbus.cloudsim.container.core.ContainerVm;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContainerVmPeProvisionerZippedTest {

    @Test
    public void allocateMipsForContainerVm() {
        ContainerVmPeProvisioner provisioner = new ContainerVmPeProvisionerZipped(700);
        ContainerVm vm1 = mock(ContainerVm.class);
        ContainerVm vm2 = mock(ContainerVm.class);
        when(vm1.getUid()).thenReturn("vm1");
        when(vm2.getUid()).thenReturn("vm2");

        provisioner.allocateMipsForContainerVm(vm1, 100);
        provisioner.allocateMipsForContainerVm(vm1, 100);
        provisioner.allocateMipsForContainerVm(vm2, 200);
        provisioner.allocateMipsForContainerVm(vm2, 200);
        provisioner.allocateMipsForContainerVm(vm2, 200);

        double factor = 600 /                   // total requested mips for vm2
                        (100.0 * 2 + 200.0 * 3) // requested mips for all
                        * 700                   // 压缩后应该分配的数量
                        / 600                   // 单位pe的占比
                ;
        List<Double> allocatedToVm2 = provisioner.getAllocatedMipsForContainerVm(vm2);
        assertThat(allocatedToVm2).containsExactly(factor * 200.0, factor * 200.0, factor * 200.0);

        ContainerVm vm3 = mock(ContainerVm.class);
        when(vm3.getUid()).thenReturn("vm3");
        provisioner.allocateMipsForContainerVm(vm3, 200);

        factor = 200 / (provisioner.getTotalAllocatedMips() + 200)
                * 700
                / 200;
        List<Double> allocatedToVm3 = provisioner.getAllocatedMipsForContainerVm(vm3);
        assertThat(allocatedToVm3).containsExactly(factor * 200.0);
    }

    @Test
    public void allocateMipsForContainerVm1() {
        ContainerVmPeProvisioner provisioner = new ContainerVmPeProvisionerZipped(700);
        ContainerVm vm1 = mock(ContainerVm.class);
        ContainerVm vm2 = mock(ContainerVm.class);
        when(vm1.getUid()).thenReturn("vm1");
        when(vm2.getUid()).thenReturn("vm2");

        provisioner.allocateMipsForContainerVm(vm1, Arrays.asList(100.0, 100.0));
        assertThat(provisioner.getAllocatedMipsForContainerVm(vm1))
                .containsExactly(100.0, 100.0);

        provisioner.allocateMipsForContainerVm(vm2, Arrays.asList(200.0, 200.0));
        assertThat(provisioner.getAllocatedMipsForContainerVm(vm2))
                .containsExactly(200.0, 200.0);

        provisioner.allocateMipsForContainerVm(vm2, Arrays.asList(200.0, 200.0, 200.0));

        double factor = 600 /                   // total mips
                (100.0 * 2 + 200.0 * 3) // request mips
                * 700                   // 压缩后应该分配的数量
                / 600
                ;
        List<Double> allocatedToVm2 = provisioner.getAllocatedMipsForContainerVm(vm2);
        assertThat(allocatedToVm2).containsExactly(factor * 200.0, factor * 200.0, factor * 200.0);


        ContainerVm vm3 = mock(ContainerVm.class);
        when(vm3.getUid()).thenReturn("vm3");
        provisioner.allocateMipsForContainerVm(vm3, Collections.singletonList(200.0));

        factor = 200 / (provisioner.getTotalAllocatedMips() + 200)
                * 700
                / 200;
        List<Double> allocatedToVm3 = provisioner.getAllocatedMipsForContainerVm(vm3);
        assertThat(allocatedToVm3).containsExactly(factor * 200.0);

        provisioner.allocateMipsForContainerVm("vm4", 200.0);
        assertThat(provisioner.getAvailableMips()).isEqualTo(0.0);
    }

    @Test
    public void getAllocatedMipsForContainerVmByVirtualPeId() {
    }

    @Test
    public void deallocateMipsForContainerVm() {
    }

    @Test
    public void deallocateMipsForAllContainerVms() {
    }
}