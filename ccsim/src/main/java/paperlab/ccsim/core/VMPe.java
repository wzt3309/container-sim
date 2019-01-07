package paperlab.ccsim.core;

import paperlab.ccsim.provisioner.vm.VMPeProvisioner;

/**
 * 代表主机中虚拟化给vm的处理器 vCPU
 */
public class VMPe {

  private int id;
  private VMPeProvisioner vmPeProvisioner;

  public VMPe(int id, VMPeProvisioner vmPeProvisioner) {
    this.id = id;
    this.vmPeProvisioner = vmPeProvisioner;
  }

  public double getMips() {
    return vmPeProvisioner.getMips();
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public VMPeProvisioner getVmPeProvisioner() {
    return vmPeProvisioner;
  }

  public void setVmPeProvisioner(VMPeProvisioner vmPeProvisioner) {
    this.vmPeProvisioner = vmPeProvisioner;
  }
}
