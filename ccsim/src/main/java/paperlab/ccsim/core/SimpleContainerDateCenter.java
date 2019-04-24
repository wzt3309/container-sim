package paperlab.ccsim.core;

import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.container.core.ContainerDatacenter;
import org.cloudbus.cloudsim.container.core.ContainerDatacenterCharacteristics;
import org.cloudbus.cloudsim.container.resourceAllocators.ContainerAllocationPolicy;
import org.cloudbus.cloudsim.container.resourceAllocators.ContainerVmAllocationPolicy;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

import java.util.List;

/**
 * 拥有一组host，每个host使用虚拟机作为第一层虚拟化，然后在虚拟机上跑容器
 * ContainerDateCenter 继承{@link org.cloudbus.cloudsim.core.SimEntity}
 * 可以向broker发送{@link org.cloudbus.cloudsim.core.SimEvent}来相互通信
 * 1. 接收broker创建vm的请求，使用某种策略将vm分配给相应的host
 * 2. 接收broker创建container的请求，使用某种策略在vm上创建相应的container
 */
public class SimpleContainerDateCenter extends ContainerDatacenter {

    public SimpleContainerDateCenter(String name, ContainerDatacenterCharacteristics characteristics, ContainerVmAllocationPolicy vmAllocationPolicy, ContainerAllocationPolicy containerAllocationPolicy, List<Storage> storageList, double schedulingInterval, String experimentName, String logAddress) throws Exception {
        super(name, characteristics, vmAllocationPolicy, containerAllocationPolicy, storageList, schedulingInterval, experimentName, logAddress);
    }
}
