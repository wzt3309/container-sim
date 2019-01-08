package paperlab.ccsim.scheduler;

import org.junit.Test;
import paperlab.ccsim.core.VM;
import paperlab.ccsim.core.VMPe;
import paperlab.ccsim.provisioner.vm.VMPeProvisioner;
import paperlab.ccsim.provisioner.vm.impl.VMPeProvisionerSimple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VMSchedulerTimeSharedOverAllocationTest {
  @Test
  public void allocatePesForVm() {
    VMPeProvisioner vmPeProvisioner0 = new VMPeProvisionerSimple(1000);
    VMPeProvisioner vmPeProvisioner1 = new VMPeProvisionerSimple(1000);

    List<VMPe> peList = new ArrayList<>();
    VMPe vmPe0 = new VMPe(0, vmPeProvisioner0);
    VMPe vmPe1 = new VMPe(1, vmPeProvisioner1);
    peList.add(vmPe0);
    peList.add(vmPe1);

    VMScheduler vmScheduler = new VMSchedulerTimeSharedOverAllocation(peList);
    assertThat(vmScheduler.getAvailableMips()).isEqualTo(getTotalAvailableMips(peList));

    VM vm0 = mock(VM.class);
    VM vm1 = mock(VM.class);
    when(vm0.getUid()).thenReturn("1-0");
    when(vm1.getUid()).thenReturn("1-1");

    List<Double> vm0ReqMips, vm1ReqMips;

    vm0ReqMips = Arrays.asList(300.0, 400.0);
    vmScheduler.allocatePesForVm(vm0, vm0ReqMips);
    assertThat(vmScheduler.getAvailableMips()).isEqualTo(getTotalAvailableMips(peList));
    assertThat(vmScheduler.getAllocatedMipsForContainerVm(vm0)).containsSequence(300.0, 400.0);

    // reallocate pe
    vm0ReqMips = Arrays.asList(300.0, 400.0, 300.0, 200.0);
    vmScheduler.allocatePesForVm(vm0, vm0ReqMips);
    assertThat(vmScheduler.getAvailableMips()).isEqualTo(getTotalAvailableMips(peList));
    assertThat(vmScheduler.getAllocatedMipsForContainerVm(vm0)).containsSequence(300.0, 400.0, 300.0, 200.0);
    // 第1，2，3次虚拟机的cpu虚拟化都使用vmPe0，第四次使用vmPe1
    assertThat(vmScheduler.getPeMap()).containsOnlyKeys("1-0").
        containsValues(Arrays.asList(vmPe0, vmPe0, vmPe0, vmPe1));

    vm1ReqMips = Arrays.asList(400.0, 400.0, 400.0);
    vmScheduler.allocatePesForVm(vm1, vm1ReqMips);
    assertThat(vmScheduler.getAvailableMips()).isEqualTo(0);
    double scalingFactor = (vmScheduler.getPeCapacity() * peList.size()) / sum(vm0ReqMips, vm1ReqMips);
    // 虚拟机请求的mips进行了压缩
    assertThat(vmScheduler.getTotalAllocatedMipsForContainerVm(vm1)).isEqualTo(
        vm1ReqMips.stream().mapToDouble(d -> Math.floor(d * scalingFactor)).sum());
    for (int i = 0; i < vmScheduler.getAllocatedMipsForContainerVm(vm0).size(); i++) {
      assertThat(vmScheduler.getAllocatedMipsForContainerVm(vm0).get(i))
          .isEqualTo(Math.floor(vm0ReqMips.get(i) * scalingFactor));
    }
  }

  @Test
  public void deallocatePersForVm() {
    VMPeProvisioner vmPeProvisioner0 = new VMPeProvisionerSimple(1000);
    VMPeProvisioner vmPeProvisioner1 = new VMPeProvisionerSimple(1000);

    List<VMPe> peList = new ArrayList<>();
    VMPe vmPe0 = new VMPe(0, vmPeProvisioner0);
    VMPe vmPe1 = new VMPe(1, vmPeProvisioner1);
    peList.add(vmPe0);
    peList.add(vmPe1);

    VMScheduler vmScheduler = new VMSchedulerTimeSharedOverAllocation(peList);
    assertThat(vmScheduler.getAvailableMips()).isEqualTo(getTotalAvailableMips(peList));

    VM vm0 = mock(VM.class);
    VM vm1 = mock(VM.class);
    when(vm0.getUid()).thenReturn("1-0");
    when(vm1.getUid()).thenReturn("1-1");

    List<Double> vm0ReqMips, vm1ReqMips;

    vm0ReqMips = Arrays.asList(300.0, 400.0);
    vmScheduler.allocatePesForVm(vm0, vm0ReqMips);
    assertThat(vmScheduler.getAvailableMips()).isEqualTo(getTotalAvailableMips(peList));
    assertThat(vmScheduler.getAllocatedMipsForContainerVm(vm0)).containsSequence(300.0, 400.0);

    vm1ReqMips = Arrays.asList(400.0, 400.0);
    vmScheduler.allocatePesForVm(vm1, vm1ReqMips);
    // 回收vm0
    vmScheduler.deallocatePersForVm(vm0);
    assertThat(vmScheduler.getAvailableMips()).isEqualTo(getTotalAvailableMips(peList));
    assertThat(vmScheduler.getAllocatedMipsForContainerVm(vm0)).isNull();
    assertThat(vmScheduler.getAvailableMips()).isEqualTo(2000 - sum(vm1ReqMips));

    // 回收全部
    vmScheduler.deallocatePesForAllContainerVms();
    assertThat(vmScheduler.getAvailableMips()).isEqualTo(2000);
  }

  private static double getTotalAvailableMips(List<VMPe> peList) {
    return peList.stream().mapToDouble(pe -> pe.getVmPeProvisioner().getAvailableMips()).sum();
  }

  @SafeVarargs
  private static double sum(List<Double>... lists) {
    double sum = 0;
    for (List<Double> list: lists) {
      sum += list.stream().mapToDouble(d -> d).sum();
    }
    return sum;
  }
}