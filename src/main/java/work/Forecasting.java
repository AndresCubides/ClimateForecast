package work;

import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import utils.Constante;
import utils.DataFormatter;
import utils.Signature;

import java.io.*;
import java.util.*;

public class Forecasting extends Work implements Constante {

    private ComputationGraph model;
    private int type;
    private int selection;
    private DataFormatter formatter;
    private String dept;

    public Forecasting(Signature signature, DataFormatter formatter, String dept, int type, int selection) {
        super(signature);
        this.formatter = formatter;
        this.dept = dept;
        this.type = type;
        this.selection = selection;
    }

    @Override
    public void loadData()
    {
        try
        {
            Date endDate;
            Calendar cal = Calendar.getInstance();

            if(type == 0){//Evaluation Mode
                if(selection == 13) {//Single value prediction
                    cal.setTime(START_DETECTION);
                    cal.add(Calendar.MONTH, 2); // Selects 2 months after to have the full trimester values to compare
                    endDate = cal.getTime();
                }else{
                    cal.setTime(END_DETECTION);
                    cal.add(Calendar.MONTH, 2); // Selects data 2 months after the beginning of trimester to have enough data to compare
                    endDate = cal.getTime();
                }
            }else{//Beyond available data mode
                if(selection == 13) {//Single value prediction
                    cal.setTime(START_DETECTION);
                    cal.add(Calendar.MONTH, - (LEAD_TIME+MODELING_NB_OUTPUTS)); // Selects data until the months needed to predict only the START_DETECTION date
                    endDate = cal.getTime();
                }else{
                    cal.setTime(END_DETECTION);
                    cal.add(Calendar.MONTH, - (LEAD_TIME+MODELING_NB_OUTPUTS)); // Selects data until the months needed to predict END_DETECTION date
                    endDate = cal.getTime();
                }
            }

            cal.setTime(START_DETECTION);
            cal.add(Calendar.MONTH, -(LEAD_TIME+TRIMESTER_MODEL_XTRA+MODELING_MONTHS_INPUT)); // Selects data months before to have the input and lead time for the START date

            formatter.loadPrec(dept, cal.getTime(), endDate);

            File name = new File(PATH_MODELS+"/"+dept+".mod");
            model = ModelSerializer.restoreComputationGraph(name, true); //Loads model

        } catch (Exception e)
        {
            System.out.println("Model "+dept+" not accessible."); //No model added
            e.printStackTrace();
        }
    }

    @Override
    public Signature doJob() {

        try
        {
            int cnt;
            int lastDateIndex;
            int selectedTrimester = 0;
            int sstHeight = formatter.getSST().size(1);
            int sstWidth = formatter.getSST().size(2);
            int nbStations = formatter.getStations().length;
            double[][] outObserved;
            double[][] outForecast;

            if(type == 0){//Evaluation mode
                lastDateIndex = formatter.getPrecip()[0].length - LEAD_TIME - (MODELING_NB_OUTPUTS*3);
            }else{
                lastDateIndex = formatter.getPrecip()[0].length;
            }

            if(selection < 1 || selection > 12){
                outObserved = new double[nbStations][lastDateIndex-MODELING_MONTHS_INPUT-TRIMESTER_MODEL_XTRA+1];//In case MODELING_NB_OUTPUTS > 1 this line should be changed
                outForecast = new double[nbStations][lastDateIndex-MODELING_MONTHS_INPUT-TRIMESTER_MODEL_XTRA+1];
            }else{
                Calendar cal = Calendar.getInstance();
                cal.setTime(END_DETECTION);
                int years = cal.get(Calendar.YEAR);
                cal.setTime(START_DETECTION);
                years = years - cal.get(Calendar.YEAR) + 1;
                outObserved = new double[nbStations][years];
                outForecast = new double[nbStations][years];
            }

            long time = System.currentTimeMillis();

            for (int i = 0; i < nbStations; i++)
            {
                cnt = 0;
                if(selection >= 1 && selection <= 12){
                    selectedTrimester = (selection-1);
                }
                for (int date = MODELING_MONTHS_INPUT+TRIMESTER_MODEL_XTRA+selectedTrimester; date <= lastDateIndex; date++)
                {
                    if(model != null)
                    {
                        INDArray input = Nd4j.zeros(1, MODELING_MONTHS_INPUT, sstHeight, sstWidth);
                        input.putSlice(0, formatter.CGInput1(date, MODELING_MONTHS_INPUT));
                        INDArray input2 = Nd4j.zeros(1, MODELING_MONTHS_INPUT);
                        input2.putRow(0, formatter.CGInput2(date, i, MODELING_MONTHS_INPUT));

                        INDArray out = model.outputSingle(false, input, input2); //Generates forecast

                        //The data is scaled to generate real precipitation values, not the normalized values
                        if(type == 0){//Evaluation mode
                            double[] observed = formatter.Output(date, i, LEAD_TIME);

                            for(int j = 0; j < out.size(1); j++)
                            {
                                outObserved[i][cnt] = observed[j]*PRECIP_MAX;
                                outForecast[i][cnt++] = out.getDouble(0,j)*PRECIP_MAX;
                            }
                        }else{
                            outObserved = null;
                            for(int j = 0; j < out.size(1); j++)
                            {
                                outForecast[i][cnt++] = out.getDouble(0,j)*PRECIP_MAX;
                            }
                        }

                    }
                    if(selection >= 1 && selection <= 12){
                        date += 11;
                    }
                }
                System.out.print("\r\t- "+Math.round(((float)(i+1)/nbStations)*100.0)+"% of "+dept+" done, "+(System.currentTimeMillis() - time)+"[ms] from the last check");
                time = System.currentTimeMillis();
            }
            formatter.writeCSV(outObserved, outForecast, selection, dept); //Creates the output CSV files

        } catch (Exception e) {
            e.printStackTrace();
        }
        return getSignature();
    }
}
