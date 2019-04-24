package paperlab.ccsim.scheduler;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Consts;
import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于CPU时间共享的任务调度器
 */
public class ContainerCloudletSchedulerTimeShared extends ContainerCloudletScheduler {
    private int currentCPUs;

    public ContainerCloudletSchedulerTimeShared() {
        super();
    }

    @Override
    public double updateContainerProcessing(double currentTime, List<Double> mipsShare) {
        setPreviousTime(currentTime);
        setCurrentMipsShare(mipsShare);

        if (getCloudletExecList().size() == 0) {
            return 0.0;
        }

        double capacity = getCapacity(mipsShare);

        double timeSpent = currentTime - getPreviousTime();
        for (ResCloudlet rcl : getCloudletExecList()) {
            // 更新这段时间内，任务已经完成的长度
            // = 时间 * 每个pe平均的mips * 任务使用的pe数量
            rcl.updateCloudletFinishedSoFar((long)
                    (timeSpent * capacity * rcl.getNumberOfPes() * Consts.MILLION));
        }

        // 检测已经完成的任务
        List<ResCloudlet> toRemove = new ArrayList<>();
        for (ResCloudlet rcl: getCloudletExecList()) {
            if (rcl.getRemainingCloudletLength() == 0) {
                toRemove.add(rcl);
                cloudletFinish(rcl);
            }
        }
        getCloudletExecList().removeAll(toRemove);

        if (getCloudletExecList().size() == 0) {
            return 0;
        }

        // 下一个最快完成的任务
        double nextEvent = Double.MAX_VALUE;
        for (ResCloudlet rcl: getCloudletExecList()) {
            double estimatedFinishTime = currentTime +
                    (rcl.getRemainingCloudletLength() / (capacity * rcl.getNumberOfPes()));
            if (estimatedFinishTime - currentTime < CloudSim.getMinTimeBetweenEvents()) {
                estimatedFinishTime = currentTime + CloudSim.getMinTimeBetweenEvents();
            }

            if (estimatedFinishTime < nextEvent) {
                nextEvent = estimatedFinishTime;
            }
        }

        return nextEvent;
    }

    @Override
    public double cloudletSubmit(Cloudlet cloudlet, double transTime) {
        ResCloudlet rcl = new ResCloudlet(cloudlet);
        rcl.setCloudletStatus(Cloudlet.INEXEC);
        for (int i = 0; i < cloudlet.getNumberOfPes(); i++) {
            rcl.setMachineAndPeId(0, i);
        }

        getCloudletExecList().add(rcl);

        // 文件传输时间也需要被加入到任务长度内
        double capacity = getCapacity(getCurrentMipsShare());
        double extraSize = capacity * transTime;
        long length = (long) (cloudlet.getCloudletLength() + extraSize);
        cloudlet.setCloudletLength(length);

        return cloudlet.getCloudletLength() / capacity;
    }

    @Override
    public double cloudletSubmit(Cloudlet cloudlet) {
        return cloudletSubmit(cloudlet, 0.0);
    }

    @Override
    public Cloudlet cloudletCancel(int cldId) {
        boolean found;
        int position;

        found = false;
        position = 0;
        for (ResCloudlet rcl : getCloudletFinishedList()) {
            if (rcl.getCloudletId() == cldId) {
                found = true;
                break;
            }
            position++;
        }

        if (found) {
            return getCloudletFinishedList().remove(position).getCloudlet();
        }

        found = false;
        position = 0;
        for (ResCloudlet rcl : getCloudletExecList()) {
            if (rcl.getCloudletId() == cldId) {
                found = true;
                break;
            }
            position++;
        }

        if (found) {
            ResCloudlet rcl = getCloudletExecList().remove(position);
            if (rcl.getRemainingCloudletLength() == 0) {
                cloudletFinish(rcl);
            } else {
                rcl.setCloudletStatus(Cloudlet.CANCELED);
            }
            return rcl.getCloudlet();
        }

        found = false;
        position = 0;
        for (ResCloudlet rcl : getCloudletPausedList()) {
            if (rcl.getCloudletId() == cldId) {
                found = true;
                rcl.setCloudletStatus(Cloudlet.CANCELED);
                break;
            }
            position++;
        }

        if (found) {
            return getCloudletPausedList().remove(position).getCloudlet();
        }

        return null;
    }

    @Override
    public boolean cloudletPause(int cldId) {
        boolean found = false;
        int position = 0;

        for (ResCloudlet rcl : getCloudletExecList()) {
            if (rcl.getCloudletId() == cldId) {
                found = true;
                break;
            }
            position++;
        }

        if (found) {
            ResCloudlet rcl = getCloudletExecList().remove(position);
            if (rcl.getRemainingCloudletLength() == 0) {
                cloudletFinish(rcl);
            } else {
                rcl.setCloudletStatus(Cloudlet.PAUSED);
                getCloudletPausedList().add(rcl);
            }
            return true;
        }
        return false;
    }

    @Override
    public double cloudletResume(int cldId) {
        boolean found = false;
        int position = 0;

        for (ResCloudlet rcl : getCloudletPausedList()) {
            if (rcl.getCloudletId() == cldId) {
                found = true;
                break;
            }
            position++;
        }

        if (found) {
            ResCloudlet rcl = getCloudletPausedList().remove(position);
            rcl.setCloudletStatus(Cloudlet.INEXEC);
            getCloudletExecList().add(rcl);

            double remainingLength = rcl.getRemainingCloudletLength();
            double estimatedFinishTime = CloudSim.clock()
                    + (remainingLength / (getCapacity(getCurrentMipsShare()) * rcl.getNumberOfPes()));

            return estimatedFinishTime;
        }

        return 0.0;
    }

    @Override
    public void cloudletFinish(ResCloudlet rcl) {
        rcl.setCloudletStatus(Cloudlet.SUCCESS);
        rcl.finalizeCloudlet();
        getCloudletFinishedList().add(rcl);
    }

    @Override
    public int getCloudletStatus(int clId) {
        for (ResCloudlet rcl : getCloudletExecList()) {
            if (rcl.getCloudletId() == clId) {
                return rcl.getCloudletStatus();
            }
        }
        for (ResCloudlet rcl : getCloudletPausedList()) {
            if (rcl.getCloudletId() == clId) {
                return rcl.getCloudletStatus();
            }
        }
        return -1;
    }

    @Override
    public boolean isFinishedCloudlets() {
        return getCloudletFinishedList().size() > 0;
    }

    @Override
    public Cloudlet getNextFinishedCloudlet() {
        if (getCloudletFinishedList().size() > 0) {
            return getCloudletFinishedList().remove(0).getCloudlet();
        }
        return null;
    }

    @Override
    public int runningCloudlets() {
        return getCloudletExecList().size();
    }

    @Override
    public Cloudlet migrateCloudlet() {
        ResCloudlet rgl = getCloudletExecList().remove(0);
        rgl.finalizeCloudlet();
        return rgl.getCloudlet();
    }

    @Override
    public double getTotalUtilizationOfCpu(double time) {
        double totalUtilization = 0;
        for (ResCloudlet gl : getCloudletExecList()) {
            totalUtilization += gl.getCloudlet().getUtilizationOfCpu(time);
        }
        return totalUtilization;
    }

    @Override
    public List<Double> getCurrentRequestedMips() {
        return new ArrayList<>();
    }

    @Override
    public double getTotalCurrentAvailableMipsForCloudlet(ResCloudlet cloudlet, List<Double> mipsShare) {
        return getCapacity(getCurrentMipsShare());
    }

    @Override
    public double getTotalCurrentRequestedMipsForCloudlet(ResCloudlet cloudlet, double time) {
        return 0;
    }

    @Override
    public double getTotalCurrentAllocatedMipsForCloudlet(ResCloudlet cloudlet, double time) {
        return 0;
    }

    @Override
    public double getCurrentRequestedUtilizationOfRam() {
        double ram = 0;
        for (ResCloudlet cloudlet: getCloudletExecList()) {
            ram += cloudlet.getCloudlet().getUtilizationOfRam(CloudSim.clock());
        }
        return ram;
    }

    @Override
    public double getCurrentRequestedUtilizationOfBw() {
        double bw = 0;
        for (ResCloudlet cloudlet : getCloudletExecList()) {
            bw += cloudlet.getCloudlet().getUtilizationOfBw(CloudSim.clock());
        }
        return bw;
    }

    /**
     * 每个处理器的平均mips
     * @param mipsShare
     * @return
     */
    protected double getCapacity(List<Double> mipsShare) {
        double capacity = 0.0;
        int cpus = 0;
        for (Double mips : mipsShare) {
            capacity += mips;
            if (mips > 0.0) {
                cpus++;
            }
        }
        currentCPUs = cpus;

        int pesInUse = 0;
        for (ResCloudlet rcl : getCloudletExecList()) {
            pesInUse += rcl.getNumberOfPes();
        }

        if (pesInUse > currentCPUs) {
            capacity /= pesInUse;
        } else {
            capacity /= currentCPUs;
        }
        return capacity;
    }

}
