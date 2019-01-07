package paperlab.ccsim.core;

import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

/**
 * 拥有一组host，每个host使用虚拟机作为第一层虚拟化，然后在虚拟机上跑容器
 * DateCenter 继承{@link org.cloudbus.cloudsim.core.SimEntity}
 * 可以向broker发送{@link org.cloudbus.cloudsim.core.SimEvent}来相互通信
 * 1. 接收broker创建vm的请求，使用某种策略将vm分配给相应的host
 * 2. 接收broker创建container的请求，使用某种策略在vm上创建相应的container
 */
public class DateCenter extends SimEntity {


  public DateCenter(String name) {
    super(name);
  }

  /**
   * This method is invoked by the {@link org.cloudbus.cloudsim.core.CloudSim} class when the simulation is started.
   * It should be responsible for starting the entity up.
   */
  @Override
  public void startEntity() {

  }

  /**
   * Processes events or services that are available for the entity.
   * This method is invoked by the {@link org.cloudbus.cloudsim.core.CloudSim} class whenever there is an event in the
   * deferred queue, which needs to be processed by the entity.
   *
   * @param ev information about the event just happened
   * @pre ev != null
   * @post $none
   */
  @Override
  public void processEvent(SimEvent ev) {

  }

  /**
   * Shuts down the entity.
   * This method is invoked by the {@link org.cloudbus.cloudsim.core.CloudSim} before the simulation finishes. If you want
   * to save data in log files this is the method in which the corresponding code would be placed.
   */
  @Override
  public void shutdownEntity() {

  }
}
