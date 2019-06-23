package Nets;

import org.deeplearning4j.earlystopping.EarlyStoppingConfiguration;
import org.deeplearning4j.earlystopping.trainer.BaseEarlyStoppingTrainer;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Adam;
import utils.Constante;
import utils.ListMultidataSetIterator;

import static utils.Constante.MODELING_EPSYLON;
import static utils.Constante.MODELING_LEARNING_RATE;

public abstract class Net implements Constante{

    public BaseEarlyStoppingTrainer trainer;

    public abstract void createTrainer(String dept);

    protected abstract EarlyStoppingConfiguration getEarlyStopConfig(String dept);

    protected abstract ComputationGraphConfiguration getNetworkConfiguration();

    protected static ConvolutionLayer convInit(String name, int in, int out, int[] kernel, int[] stride, int[] pad, double bias) {
        return new ConvolutionLayer.Builder(kernel, stride, pad)
                .name(name)
                .nIn(in) //Channels, number of planes.
                .nOut(out) //Number of feature maps, filters.
                .biasInit(bias)
                .activation(Activation.SIGMOID)
                .updater(new Adam(MODELING_LEARNING_RATE, 0.9, 0.999, MODELING_EPSYLON))
                .build();
    }

    protected static SubsamplingLayer averagePool(String name) {
        return new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.AVG).name(name).kernelSize(2,2).stride(2,2).build();
    }

    protected static SubsamplingLayer maxPool(String name) {
        return new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX).name(name).kernelSize(2,2).stride(2,2).build();
    }

    protected static ConvolutionLayer conv(String name, int out, int[] kernel, int[] stride, int[] pad, double bias) {
        return new ConvolutionLayer.Builder(kernel, stride, pad).name(name).nOut(out).biasInit(bias).activation(Activation.SIGMOID).build();
    }

    public abstract void setIterator(ListMultidataSetIterator iterator);

    public abstract void setValIterator(ListMultidataSetIterator iterator);

    public abstract ListMultidataSetIterator getIterator();

}
