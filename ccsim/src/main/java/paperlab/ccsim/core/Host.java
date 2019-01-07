package paperlab.ccsim.core;

/**
 * 作为运行容器的物理主机的模拟
 */
public class Host {

  // host id
  private int id;

  // 所在的 datacenter
  private DateCenter datacenter;

  public Host(int id) {
    this.id = id;
  }

}
