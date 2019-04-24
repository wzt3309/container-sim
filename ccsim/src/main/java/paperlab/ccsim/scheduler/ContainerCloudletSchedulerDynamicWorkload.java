package paperlab.ccsim.scheduler;

import lombok.Data;
import org.cloudbus.cloudsim.ResCloudlet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ContainerCloudletSchedulerDynamicWorkload extends ContainerCloudletSchedulerTimeShared {
    private double mips;

    private int numberOfPes;

    private double totalMips;

    private Map<String, Double> underAllocatedMips;

    private double cachePreviousTime;

    private List<Double> cacheCurrentRequestedMips;

    public ContainerCloudletSchedulerDynamicWorkload (double mips, int numberOfPes) {
        super();
        setMips(mips);
        setNumberOfPes(numberOfPes);
        setTotalMips(getNumberOfPes() * getMips());
        setUnderAllocatedMips(new HashMap<String, Double>());
        setCachePreviousTime(-1);
    }

    @Override
    public List<Double> getCurrentRequestedMips() {
        if (getCachePreviousTime() == getPreviousTime()) {
            return getCacheCurrentRequestedMips();
        }
        List<Double> currentMips = new ArrayList<>();
        double totalMips = getTotalUtilizationOfCpu(getPreviousTime()) * getTotalMips();
        double mipsForPe = totalMips / getNumberOfPes();

        for (int i = 0; i < getNumberOfPes(); i++) {
            currentMips.add(mipsForPe);
        }

        setCachePreviousTime(getPreviousTime());
        setCacheCurrentRequestedMips(currentMips);

        return currentMips;
    }

    @Override
    public double getTotalCurrentRequestedMipsForCloudlet(ResCloudlet rcl, double time) {
        return rcl.getCloudlet().getUtilizationOfCpu(time) * getTotalMips();
    }

    @Override
    public double getTotalCurrentAvailableMipsForCloudlet(ResCloudlet rcl, List<Double> mipsShare) {
        double totalCurrentMips = 0.0;
        if (mipsShare != null) {
            int neededPEs = rcl.getNumberOfPes();
            for (double mips : mipsShare) {
                totalCurrentMips += mips;
                neededPEs--;
                if (neededPEs <= 0) {
                    break;
                }
            }
        }
        return totalCurrentMips;
    }

    @Override
    public double getTotalCurrentAllocatedMipsForCloudlet(ResCloudlet rcl, double time) {
        double totalCurrentRequestedMips = getTotalCurrentRequestedMipsForCloudlet(rcl, time);
        double totalCurrentAvailableMips = getTotalCurrentAvailableMipsForCloudlet(rcl, getCurrentMipsShare());
        if (totalCurrentRequestedMips > totalCurrentAvailableMips) {
            return totalCurrentAvailableMips;
        }
        return totalCurrentRequestedMips;
    }

    /**
     * 更新分配给任务的mips
     * @param rcl
     * @param mips
     */
    public void updateUnderAllocatedMipsForCloudlet(ResCloudlet rcl, double mips) {
        if (getUnderAllocatedMips().containsKey(rcl.getUid())) {
            mips += getUnderAllocatedMips().get(rcl.getUid());
        }
        getUnderAllocatedMips().put(rcl.getUid(), mips);
    }

    /**
     * 获得任务预计完成时间
     * @param rcl
     * @param time
     * @return
     */
    public double getEstimatedFinishTime(ResCloudlet rcl, double time) {
        return time
                + ((rcl.getRemainingCloudletLength()) / getTotalCurrentAllocatedMipsForCloudlet(rcl, time));
    }

    /**
     * 获得当前总的mips
     * @return
     */
    public int getTotalCurrentMips() {
        int totalCurrentMips = 0;
        for (double mips : getCurrentMipsShare()) {
            totalCurrentMips += mips;
        }
        return totalCurrentMips;
    }


}
