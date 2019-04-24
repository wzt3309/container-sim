package paperlab.ccsim.scheduler;

import lombok.Data;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.ResCloudlet;

import java.util.ArrayList;
import java.util.List;

/**
 * 在容器内调度执行cloudlet的策略，这里的模拟是容器作为
 * 资源隔离的虚拟化，cloudlet作为容器内的进程
 * 真实的情况一般是，一个容器内一个进程
 */
@Data
public abstract class ContainerCloudletScheduler {

    private double previousTime;

    private List<Double> currentMipsShare;

    private List<? extends ResCloudlet> cloudletWaitingList;
    private List<? extends ResCloudlet> cloudletExecList;
    private List<? extends ResCloudlet> cloudletPausedList;
    private List<? extends ResCloudlet> cloudletFinishedList;
    private List<? extends ResCloudlet> cloudletFailedList;

    /**
     * 创建一个Container内的任务（cloudlet）调度器
     */
    public ContainerCloudletScheduler() {
        setPreviousTime(0.0);
        cloudletWaitingList = new ArrayList<>();
        cloudletExecList = new ArrayList<>();
        cloudletPausedList = new ArrayList<>();
        cloudletFinishedList = new ArrayList<>();
        cloudletFailedList = new ArrayList<>();
    }

    /**
     * 模拟运行容器内的任务
     * @param currentTime 当前时间
     * @param mipsShare 容器中可用处理器的mips信息
     * @return 剩余任务中预计最快完成的任务所需要的完成时间（下一个事件完成时间），如果不存在需要执行的任务，则返回0
     */
    public abstract double updateContainerProcessing(double currentTime, List<Double> mipsShare);

    /**
     * 调度器接受提交到该容器的任务
     * @param cloudlet 提交的任务
     * @param transTime 提交任务时，任务代码或是任务资源传输时间
     * @return 任务完成的时间
     */
    public abstract double cloudletSubmit(Cloudlet cloudlet, double transTime);

    /**
     * 调度器接受提交到该容器的任务
     * @param cloudlet 提交的任务
     * @return 任务提交完成的时间，如果任务还在等待队列中，则返回0
     */
    public abstract double cloudletSubmit(Cloudlet cloudlet);

    /**
     * 取消任务
     * @param cldId 需要取消的任务id
     * @return 返回被取消的任务，如果任务不存在返回null
     */
    public abstract Cloudlet cloudletCancel(int cldId);

    /**
     * 暂停任务
     * @param cldId 需要暂停的任务id
     * @return 任务被暂停返回true，否则返回false
     */
    public abstract boolean cloudletPause(int cldId);

    /**
     * 恢复执行被暂停的任务
     * @param cldId 需要恢复执行的任务id
     * @return 任务执行完成的时间，找不到返回0
     */
    public abstract double cloudletResume(int cldId);

    /**
     * 处理已经完成的任务
     * @param cloudlet 已经完成的任务
     */
    public abstract void cloudletFinish(ResCloudlet cloudlet);

    /**
     * 任务状态，从运行队列和暂停队列中查找
     * @param clId 任务id
     * @return 任务的状态，如果任务没找到则返回-1
     */
    public abstract int getCloudletStatus(int clId);

    /**
     * 容器内的任务完成情况
     * @return 如果有一个任务完成则返回true，否则false
     */
    public abstract boolean isFinishedCloudlets();

    /**
     * 类似迭代器，返回下一个完成的任务，注意每次会删除迭代过的容器
     * @return 完成的任务
     */
    public abstract Cloudlet getNextFinishedCloudlet();

    /**
     * 返回容器内运行的任务数量
     * @return 运行的任务数量
     */
    public abstract int runningCloudlets();

    /**
     * 需要进行迁移的任务
     * @return 返回一个需要迁移的任务
     */
    public abstract Cloudlet migrateCloudlet();

    /**
     * 获得运行队列中所有任务的总体的cpu利用率
     * @param time 某一时刻
     * @return cpu利用率
     */
    public abstract double getTotalUtilizationOfCpu(double time);

    /**
     * 获得当前需要的mips
     * @return 需要的mips
     */
    public abstract List<Double> getCurrentRequestedMips();

    /**
     * 获得当前可用的mips（单个pe）
     * @param cloudlet 任务
     * @param mipsShare 用于任务共享的misp
     * @return 总的mips
     */
    public abstract double getTotalCurrentAvailableMipsForCloudlet(ResCloudlet cloudlet, List<Double> mipsShare);

    /**
     * 当前任务需要的mips
     * @param cloudlet 任务
     * @param time 时间
     * @return 任务需要的总的mips
     */
    public abstract double getTotalCurrentRequestedMipsForCloudlet(ResCloudlet cloudlet, double time);

    /**
     * 当前分配给任务的mips
     * @param cloudlet 任务
     * @param time 时间
     * @return 分配给任务的总的mips
     */
    public abstract double getTotalCurrentAllocatedMipsForCloudlet(ResCloudlet cloudlet, double time);

    /**
     * 请求的内存
     * @return 请求的内存
     */
    public abstract double getCurrentRequestedUtilizationOfRam();

    /**
     * 请求的带宽
     * @return 带宽
     */
    public abstract double getCurrentRequestedUtilizationOfBw();

    @SuppressWarnings("unchecked")
    public <T extends ResCloudlet> List<T> getCloudletWaitingList() {
        return (List<T>) cloudletWaitingList;
    }

    protected <T extends ResCloudlet> void setCloudletWaitingList(List<T> cloudletWaitingList) {
        this.cloudletWaitingList = cloudletWaitingList;
    }

    @SuppressWarnings("unchecked")
    public <T extends ResCloudlet> List<T> getCloudletExecList() {
        return (List<T>) cloudletExecList;
    }

    protected <T extends ResCloudlet> void setCloudletExecList(List<T> cloudletExecList) {
        this.cloudletExecList = cloudletExecList;
    }

    @SuppressWarnings("unchecked")
    public <T extends ResCloudlet> List<T> getCloudletPausedList() {
        return (List<T>) cloudletPausedList;
    }

    protected <T extends ResCloudlet> void setCloudletPausedList(List<T> cloudletPausedList) {
        this.cloudletPausedList = cloudletPausedList;
    }

    @SuppressWarnings("unchecked")
    public <T extends ResCloudlet> List<T> getCloudletFinishedList() {
        return (List<T>) cloudletFinishedList;
    }

    protected <T extends ResCloudlet> void setCloudletFinishedList(List<T> cloudletFinishedList) {
        this.cloudletFinishedList = cloudletFinishedList;
    }

    @SuppressWarnings("unchecked")
    public <T extends ResCloudlet> List<T>  getCloudletFailedList() {
        return (List<T>) cloudletFailedList;
    }

    protected <T extends ResCloudlet> void setCloudletFailedList(List<T> cloudletFailedList) {
        this.cloudletFailedList = cloudletFailedList;
    }
}
