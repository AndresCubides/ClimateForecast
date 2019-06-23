package work;

import org.deeplearning4j.eval.RegressionEvaluation;
import utils.DataFormatter;
import utils.Signature;
import Nets.CNN;
import Nets.Net;
import org.deeplearning4j.earlystopping.EarlyStoppingResult;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.memory.MemoryWorkspace;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.MultiDataSet;
import org.nd4j.linalg.factory.Nd4j;
import utils.Constante;
import utils.ListMultidataSetIterator;

import java.io.File;
import java.util.*;

public class Modeling extends Work implements Constante {

    private Net net;
    private DataFormatter formatter;
    private String dept;

    public Modeling(Signature signature, DataFormatter formatter, String dept) {
        super(signature);
        this.formatter = formatter;
        this.dept = dept;
    }

    @Override
    public void loadData() {

        try (MemoryWorkspace ws = Nd4j.getWorkspaceManager().getWorkspaceForCurrentThread())
        {
            formatter.loadPrec(dept, START_MODELING, END_MODELING);

            int nbStations = formatter.getPrecip().length;
            int totalMonths = formatter.getPrecip()[0].length;
            int sstHeight = formatter.getSST().size(1);
            int sstWidth = formatter.getSST().size(2);
            int trainingCounter = 0;
            int trainingEx;

            trainingEx = nbStations * (totalMonths - LEAD_TIME - MODELING_MONTHS_INPUT - TRIMESTER_MODEL_XTRA - (MODELING_NB_OUTPUTS*3)+ 1); //*3 because the output is the average of a trimester

            INDArray inputs = Nd4j.zeros(trainingEx, MODELING_MONTHS_INPUT, sstHeight, sstWidth);
            INDArray inputs2 = Nd4j.zeros(trainingEx, MODELING_MONTHS_INPUT);
            INDArray outputs = Nd4j.zeros(trainingEx, MODELING_NB_OUTPUTS);

            //Creating dataset arrays
            for (int i = 0; i < nbStations; i++)
            {
                for (int date = MODELING_MONTHS_INPUT + TRIMESTER_MODEL_XTRA; date <= totalMonths - LEAD_TIME - (MODELING_NB_OUTPUTS*3); date++)
                {
                    INDArray inputArray = formatter.CGInput1(date, MODELING_MONTHS_INPUT);
                    inputs.putSlice(trainingCounter, inputArray);
                    INDArray inputArray2 = formatter.CGInput2(date, i, MODELING_MONTHS_INPUT);
                    inputs2.putRow(trainingCounter, inputArray2);

                    INDArray outputArray = Nd4j.create(formatter.Output(date, i, LEAD_TIME));
                    outputs.putRow(trainingCounter++, outputArray);
                }
            }


            net = new CNN();

            //Setting training iterators
            MultiDataSet dataSet = new MultiDataSet(new INDArray[] {inputs, inputs2}, new INDArray[] {outputs});
            List<org.nd4j.linalg.dataset.api.MultiDataSet> listMD = dataSet.asList();
            Collections.shuffle(listMD);
            net.setIterator(new ListMultidataSetIterator(listMD, BATCH_SIZE));

            net.createTrainer(dept);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Signature doJob()
    {
        if(net != null && net.trainer != null){
            //totally disable GC only workspace
            Nd4j.getMemoryManager().togglePeriodicGc(false);

            //Conduct early stopping training:
            EarlyStoppingResult result = net.trainer.fit();

            //Print out the results:
            System.out.println(dept+" model is done:");
            System.out.println("Termination reason: " + result.getTerminationReason());
            System.out.println("Termination details: " + result.getTerminationDetails());
            System.out.println("Total epochs: " + result.getTotalEpochs());
            System.out.println("Best epoch number: " + result.getBestModelEpoch());
            System.out.println("Score at best epoch: " + result.getBestModelScore());

            //Get the best model:
            ComputationGraph nnet = (ComputationGraph) result.getBestModel();
            RegressionEvaluation eval = nnet.evaluateRegression(net.getIterator());
            System.out.println(eval.stats());

            manageResult(nnet);
            if(nnet != null){
                return getSignature();
            }

        }
        return null;
    }

    private void manageResult(ComputationGraph nnet){//Saves models
        try{
            new File(PATH_MODELS).mkdirs();
            File location = new File(PATH_MODELS+dept+".mod");
            ModelSerializer.writeModel(nnet, location, true);
        }catch(Exception e)
        {
            System.out.println("Save crash..."+e);
        }
    }
}
