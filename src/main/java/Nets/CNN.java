package Nets;

import org.deeplearning4j.earlystopping.EarlyStoppingConfiguration;
import org.deeplearning4j.earlystopping.listener.EarlyStoppingListener;
import org.deeplearning4j.earlystopping.saver.InMemoryModelSaver;
import org.deeplearning4j.earlystopping.saver.LocalFileGraphSaver;
import org.deeplearning4j.earlystopping.scorecalc.DataSetLossCalculator;
import org.deeplearning4j.earlystopping.termination.MaxTimeIterationTerminationCondition;
import org.deeplearning4j.earlystopping.termination.ScoreImprovementEpochTerminationCondition;
import org.deeplearning4j.earlystopping.trainer.EarlyStoppingGraphTrainer;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.WorkspaceMode;
import org.deeplearning4j.nn.conf.graph.MergeVertex;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.MultiDataSetIterator;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import utils.Constante;
import utils.ListMultidataSetIterator;
import utils.LoggingEarlyStoppingListener;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CNN extends Net implements Constante {

    private ListMultidataSetIterator iterator;
    private ListMultidataSetIterator valIterator;

    @Override
    public void setIterator(ListMultidataSetIterator iterator) {
        this.iterator = iterator;
        setValIterator(this.iterator);
    }

    @Override
    public void setValIterator(ListMultidataSetIterator valIterator) {
        this.valIterator = valIterator;
    }

    @Override
    public ListMultidataSetIterator getIterator() {
        return iterator;
    }

    @Override
    public void createTrainer(String dept){

        ComputationGraph cgNet = new ComputationGraph(getNetworkConfiguration());
        final EarlyStoppingConfiguration<ComputationGraph> esConf = getEarlyStopConfig(dept);
        EarlyStoppingListener<ComputationGraph> listener = new LoggingEarlyStoppingListener();

        File bestModel = new File(PATH_MODELS +dept+ "/bestGraph.bin");
        if (bestModel.exists()) {
            try {
                System.out.println("..............................Loading previous model............................");
                cgNet = ModelSerializer.restoreComputationGraph(PATH_MODELS + dept+ "/bestGraph.bin", true);
                cgNet.init();
                trainer = new EarlyStoppingGraphTrainer(esConf, cgNet, iterator, listener);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            cgNet.init();
            trainer = new EarlyStoppingGraphTrainer(esConf, cgNet, iterator, listener);
        }
    }

    @Override
    protected EarlyStoppingConfiguration<ComputationGraph> getEarlyStopConfig(String dept){

        if(SAVE_LOCALLY == 1){//Saves best model file to allow future training from this checkpoint
            return new EarlyStoppingConfiguration.Builder()
                    .epochTerminationConditions(new ScoreImprovementEpochTerminationCondition(NO_IMPROVE_EPOCHS))
                    .iterationTerminationConditions(new MaxTimeIterationTerminationCondition(MINUTES_PER_MODEL, TimeUnit.MINUTES))
                    .scoreCalculator( new DataSetLossCalculator(valIterator, true))
                    .evaluateEveryNEpochs(1)
                    .modelSaver(new LocalFileGraphSaver(PATH_MODELS+dept))//Local mode allows only one model (one l cluster)
                    .build();
        }else{//Doesn't save best model, only final model to evaluate
            return new EarlyStoppingConfiguration.Builder()
                    .epochTerminationConditions(new ScoreImprovementEpochTerminationCondition(NO_IMPROVE_EPOCHS))
                    .iterationTerminationConditions(new MaxTimeIterationTerminationCondition(MINUTES_PER_MODEL, TimeUnit.MINUTES))
                    .scoreCalculator(new DataSetLossCalculator(valIterator, true))
                    .evaluateEveryNEpochs(1)
                    .modelSaver(new InMemoryModelSaver())//Memory mode allows multiple models new InMemoryModelSaver()
                    .build();
        }
    }

    @Override
    protected ComputationGraphConfiguration getNetworkConfiguration() {
        int MODELING_NB_HIDDEN_LAYER4 = parameter.getInteger("MODELING_NB_HIDDEN_LAYER4");
        int MODELING_NB_HIDDEN_LAYER5 = parameter.getInteger("MODELING_NB_HIDDEN_LAYER5");

        switch (MODELING_TYPE) {
            case 0://Modeling with Pooling layers
                return new NeuralNetConfiguration.Builder()
                        .miniBatch(true)
                        .weightInit(WeightInit.XAVIER)
                        .trainingWorkspaceMode(WorkspaceMode.SEPARATE)
                        .inferenceWorkspaceMode(WorkspaceMode.SINGLE)
                        .updater(new Adam(MODELING_LEARNING_RATE, 0.9, 0.999, MODELING_EPSYLON))
                        .graphBuilder()
                        .addInputs("input", "prec")
                        .addLayer("cnn1", convInit("cnn1", MODELING_MONTHS_INPUT, MODELING_NB_HIDDEN_LAYER1,  new int[]{6, 12}, new int[]{5, 8}, new int[]{0, 0}, 0), "input")
                        .addLayer("max1", maxPool("maxpool1"),"cnn1")
                        .addLayer("cnn2", conv("cnn2", MODELING_NB_HIDDEN_LAYER2, new int[]{1, 3}, new int[]{1, 2}, new int[]{0, 0}, 0), "max1")
                        .addLayer("fc1",new DenseLayer.Builder().nOut(MODELING_NB_HIDDEN_LAYER3).updater(new Adam(MODELING_LEARNING_RATE, 0.9, 0.999, MODELING_EPSYLON))
                                .activation(Activation.TANH).build(), "cnn2")
                        .addVertex("merge", new MergeVertex(), "fc1", "prec")
                        .addLayer("fc2",new DenseLayer.Builder().nOut(MODELING_NB_HIDDEN_LAYER4).updater(new Adam(MODELING_LEARNING_RATE, 0.9, 0.999, MODELING_EPSYLON))
                                .activation(Activation.TANH).build(), "merge")
                        .addLayer("fc3",new DenseLayer.Builder().nOut(MODELING_NB_HIDDEN_LAYER5).updater(new Adam(MODELING_LEARNING_RATE, 0.9, 0.999, MODELING_EPSYLON))
                                .activation(Activation.TANH).build(), "fc2")
                        .addLayer("out",new OutputLayer.Builder(LossFunctions.LossFunction.MSE).activation(Activation.IDENTITY).nOut(MODELING_NB_OUTPUTS).updater(new Adam(MODELING_LEARNING_RATE, 0.9, 0.999, MODELING_EPSYLON)).build(), "fc3")
                        .setOutputs("out")
                        .pretrain(false).backprop(true)
                        .setInputTypes(InputType.convolutionalFlat(31,180, MODELING_MONTHS_INPUT), InputType.feedForward(MODELING_MONTHS_INPUT)) // 6 months of data
                        .build();

            default://Modeling without Pooling layers
                return new NeuralNetConfiguration.Builder()
                        .miniBatch(true)
                        .weightInit(WeightInit.XAVIER)
                        .trainingWorkspaceMode(WorkspaceMode.SEPARATE)
                        .inferenceWorkspaceMode(WorkspaceMode.SINGLE)
                        .updater(new Adam(MODELING_LEARNING_RATE, 0.9, 0.999, MODELING_EPSYLON))
                        .graphBuilder()
                        .addInputs("input", "recent")
                        .addLayer("cnn1", convInit("cnn1", MODELING_MONTHS_INPUT, MODELING_NB_HIDDEN_LAYER1, new int[]{1, 3}, new int[]{1, 6}, new int[]{0, 0}, 0), "input")
                        .addLayer("cnn2", conv("cnn2", MODELING_NB_HIDDEN_LAYER2, new int[]{1, 4}, new int[]{1, 2}, new int[]{0, 0}, 0), "cnn1")
                        .addLayer("fc1",new DenseLayer.Builder().nOut(MODELING_NB_HIDDEN_LAYER3).updater(new Adam(MODELING_LEARNING_RATE, 0.9, 0.999, MODELING_EPSYLON))
                                .activation(Activation.TANH).build(), "cnn2")
                        .addVertex("merge", new MergeVertex(), "fc1", "recent")
                        .addLayer("fc2",new DenseLayer.Builder().nOut(MODELING_NB_HIDDEN_LAYER4).updater(new Adam(MODELING_LEARNING_RATE, 0.9, 0.999, MODELING_EPSYLON))
                                .activation(Activation.TANH).build(), "merge")
                        .addLayer("fc3",new DenseLayer.Builder().nOut(MODELING_NB_HIDDEN_LAYER5).updater(new Adam(MODELING_LEARNING_RATE, 0.9, 0.999, MODELING_EPSYLON))
                                .activation(Activation.TANH).build(), "fc2")
                        .addLayer("out",new OutputLayer.Builder(LossFunctions.LossFunction.MSE).activation(Activation.IDENTITY).nOut(MODELING_NB_OUTPUTS).updater(new Adam(MODELING_LEARNING_RATE, 0.9, 0.999, MODELING_EPSYLON)).build(), "fc3")
                        .setOutputs("out")
                        .pretrain(false).backprop(true)
                        .setInputTypes(InputType.convolutionalFlat(31,180, MODELING_MONTHS_INPUT), InputType.feedForward(MODELING_MONTHS_INPUT)) // 6 months of data
                        .build();
        }
    }
}

