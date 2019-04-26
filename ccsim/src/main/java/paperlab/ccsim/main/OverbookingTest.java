package paperlab.ccsim.main;

import java.io.IOException;

public class OverbookingTest {

    public static void main(String[] args) throws IOException {
        /**
         * 设置实验重复次数，和重复实验的标号
         */
        int runtime = Integer.parseInt(args[0]);
        int repeat = Integer.parseInt(args[1]);
        for (int i = 10; i < repeat; i += 10) {
            boolean enableOutput = true;
            boolean outputToFile = true;
            /**
             * 获得实验中用到的容器负载数据
             */
            String inputFolder = OverbookingTest.class.getClassLoader().getResource("workload/planetlab").getPath();
            /**
             * 实验输出结果所在目录
             */
            String outputFolder = "/tmp/Results";
            /**
             * vm 分配策略，0.8、0.7分别表示过载和欠载阈值
             */
            String vmAllocationPolicy = "MSThreshold-Under_0.80_0.70";
            /**
             * 选择迁移容器的规则，此处使用负载相似度规则
             */
            String containerSelectionPolicy = "Cor";
            /**
             * 容器分配规则
             */
            String containerAllocationPolicy = "MostFull";
//            String containerAllocationPolicy= "FirstFit";
//            String containerAllocationPolicy= "LeastFull";
//            String containerAllocationPolicy= "Simple";
//            String containerAllocationPolicy = "Random";

            /**
             * 为迁移容器选择目的主机的规则
             */
            String hostSelectionPolicy = "FirstFit";
            /**
             * 当主机被标记为overload时，选择进行迁移的虚拟机规则
             */
            String vmSelectionPolicy = "VmMaxC";

            new ContainerSimulationImpl(
                    enableOutput,
                    outputToFile,
                    inputFolder,
                    outputFolder,
                    vmAllocationPolicy,
                    containerAllocationPolicy,
                    vmSelectionPolicy,
                    containerSelectionPolicy,
                    hostSelectionPolicy,
                    i, Integer.toString(runtime), outputFolder);
        }

    }
}
