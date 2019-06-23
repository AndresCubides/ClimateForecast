package utils;

import org.datavec.image.loader.ImageLoader;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class DataFormatter implements Constante, Serializable{

    private INDArray SST;
    private String[] stations;
    private double [][] precip;
    private ArrayList<Date> header;

    public void loadSST(Date start, Date end)
    {
        String[] data;
        String splitBy = ",";
        String line;
        BufferedReader br;
        int dateCnt = 0;
        int startIndex = -1;
        int endIndex = -1;
        Date date;
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM");

        try{
            br = new BufferedReader(new FileReader(PATH_INPUTS+"header.csv/"));
            if((line = br.readLine()) !=null){
                data = line.split(splitBy);

                while(dateCnt < data.length && endIndex == -1)
                {
                    date = dateFormat.parse(data[dateCnt]);

                    if(date.equals(start)){
                        startIndex = dateCnt;
                    }else if(date.equals(end)){
                        endIndex = dateCnt;
                    }
                    dateCnt++;
                }
            }

            if(startIndex == -1 || endIndex == -1){
                System.out.println("ERROR: Not enough data to forecast from "+START_DETECTION+" until "+END_DETECTION+". Press ENTER to quit");
                try {
                    System.in.read();
                } catch (Exception e) {}
                System.exit(-1);
            }

            ImageLoader imageLoader = new ImageLoader(); //Needed not sure why, it doesn't run without this line.

            BufferedImage tifData = ImageIO.read(new File(PATH_SST));

            Raster raster = tifData.getRaster();
            DataBuffer buffer = raster.getDataBuffer();
            int totalBands = raster.getNumBands(); //# of bands
            int w = tifData.getWidth();
            SST = Nd4j.zeros(endIndex-startIndex+1, tifData.getHeight(), tifData.getWidth());

            for(int b = 0; b < SST.size(0); b++){
                for (int y = 0; y < SST.size(1); y++){
                    for (int x = 0; x < SST.size(2); x++) {
                        SST.putScalar(new int[]{b,y,x}, buffer.getElemDouble(((y * w + x)*totalBands)+(b+startIndex)));
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public ArrayList<String> loadDeptList()
    {
        ArrayList<String> deptList = new ArrayList<String>();
        String splitBy = ",";
        String line;
        BufferedReader br;

        try {
            br = new BufferedReader(new FileReader(PATH_DEPT_LIST));
            while((line = br.readLine()) !=null){
                deptList.add(line.split(splitBy)[0]); //This case is position 0 because there is a single department name per line
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deptList;
    }

    public void loadPrec(String dept, Date start, Date end)
    {
        String[] data;
        String splitBy = ",";
        String line;
        BufferedReader br;
        int dateCnt = 1;//Starts in 1 because postion 0 have the number of stations
        int statCnt = 0;
        int startIndex = -1;
        int endIndex = -1;
        Date date;
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM");
        header = new ArrayList<>();
        stations = null;
        precip = null;

        try {
            br = new BufferedReader(new FileReader(PATH_DEPT_DATA+dept+".csv/"));
            if((line = br.readLine()) !=null){
                data = line.split(splitBy);

                while(dateCnt < data.length && endIndex == -1){
                    date = dateFormat.parse(data[dateCnt]);

                    if(date.equals(start)){
                        startIndex = dateCnt;
                    }else if(date.equals(end)){
                        endIndex = dateCnt;
                    }

                    if(startIndex != -1){
                        header.add(date);
                    }
                    dateCnt++;
                }

                stations = new String[Integer.parseInt(data[0])];//The first value in the first line always have the number of stations
                precip = new double[Integer.parseInt(data[0])][header.size()];

                while((line = br.readLine()) !=null){
                    data = line.split(splitBy);
                    stations[statCnt] = data[0];

                    for(int i = startIndex; i <= endIndex; i++){
                        precip[statCnt][i-startIndex]= Double.parseDouble(data[i]);
                    }
                    statCnt++;
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeCSV(double[][] observed, double[][] forecast, int selection, String dept){
        try{
            String path =PATH_OUTPUTS;
            Formatter fmt;
            Calendar cal = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            final DateFormat formater = new SimpleDateFormat("yyyy_MM");
            final DateFormat monthFormat = new SimpleDateFormat("-MM");
            int selectedTrimester = 0;

            if(selection >= 1 && selection <= 12){
                cal.setTime(START_DETECTION);
                cal2.setTime(END_DETECTION);
                cal.add(Calendar.MONTH, (selection-1));
                fmt = new Formatter();
                fmt.format("%tB_%tY-%tY", cal, cal, cal2);
                path += fmt;
            }else{
                cal.setTime(START_DETECTION);
                fmt = new Formatter();
                fmt.format("%tB_%tY", cal, cal);
                path += fmt;
            }

            if(selection == 0){
                cal.setTime(END_DETECTION);
                fmt = new Formatter();
                fmt.format("-%tB_%tY", cal, cal);
                path += fmt;
            }

            path+="/";

            new File(path).mkdirs();
            PrintWriter forWriter = new PrintWriter(new BufferedWriter(new FileWriter(path+dept+"_forecast.csv")));
            forWriter.print("Station");

            if(selection >= 1 && selection <= 12){
                selectedTrimester = (selection-1);
            }

            if(observed != null){//Evaluation Mode
                PrintWriter obsWriter = new PrintWriter(new BufferedWriter(new FileWriter(path+dept+"_observed.csv")));
                obsWriter.print("Station");

                for (int date = MODELING_MONTHS_INPUT+LEAD_TIME+TRIMESTER_MODEL_XTRA+selectedTrimester; date <= precip[0].length - (MODELING_NB_OUTPUTS*3); date++){
                    obsWriter.print(","+formater.format(header.get(date))+monthFormat.format(header.get(date+1))+monthFormat.format(header.get(date+2)));
                    forWriter.print(","+formater.format(header.get(date))+monthFormat.format(header.get(date+1))+monthFormat.format(header.get(date+2)));

                    if(selection >= 1 && selection <= 12)
                    {//In case it is an specific trimester per year
                        date += 11;
                    }
                }
                obsWriter.println();
                forWriter.println();

                for (int x = 0; x < observed.length; x++)
                {
                    obsWriter.print(stations[x]);
                    forWriter.print(stations[x]);
                    for (int y = 0; y < observed[x].length; y++){
                        obsWriter.print(","+observed[x][y]);
                        forWriter.print(","+forecast[x][y]);
                    }
                    obsWriter.println();
                    forWriter.println();
                }

                System.gc();

                obsWriter.flush();
                forWriter.flush();
                obsWriter.close();
                forWriter.close();

            }else{//Beyond available data mode
                int months = 1;
                for (int date = MODELING_MONTHS_INPUT+LEAD_TIME+TRIMESTER_MODEL_XTRA+selectedTrimester; date <= precip[0].length - (MODELING_NB_OUTPUTS*3); date++){
                    forWriter.print(","+formater.format(header.get(date))+monthFormat.format(header.get(date+1))+monthFormat.format(header.get(date+2)));

                    if(selection >= 1 && selection <= 12){
                        date += 11;
                        months = 12;
                    }
                }

                cal.setTime(header.get(precip[0].length - (MODELING_NB_OUTPUTS*3)));
                cal.add(Calendar.MONTH, months);
                Date index = cal.getTime();

                while (!index.after(END_DETECTION))
                {//Adds extra dates not available in the input data and header
                    forWriter.print(","+formater.format(index));
                    cal.add(Calendar.MONTH, 1);
                    forWriter.print(monthFormat.format(cal.getTime()));
                    cal.add(Calendar.MONTH, 1);
                    forWriter.print(monthFormat.format(cal.getTime()));

                    cal.setTime(index);
                    cal.add(Calendar.MONTH, months);
                    index = cal.getTime();
                }

                forWriter.println();

                for (int x = 0; x < forecast.length; x++)
                {
                    forWriter.print(stations[x]);
                    for (int y = 0; y < forecast[x].length; y++){
                        forWriter.print(","+forecast[x][y]);
                    }
                    forWriter.println();
                }

                System.gc();

                forWriter.flush();
                forWriter.close();
            }

        }catch(Exception e)
        {
            System.out.println("Save crash..."+e);
        }
    }


    public INDArray CGInput1(int date, int MODELING_MONTHS_INPUT)
    {//Creates inputs going backwards from the selected date
        INDArray input = Nd4j.zeros(MODELING_MONTHS_INPUT, SST.size(1), SST.size(2)); //change for constants

        for(int dec = 1; dec <= MODELING_MONTHS_INPUT; dec++)
        {
            input.putSlice(dec-1, SST.get(NDArrayIndex.point(date-dec), NDArrayIndex.all(), NDArrayIndex.all()));
        }
        return input;
    }

    public INDArray CGInput2(int date, int index, int MODELING_MONTHS_INPUT)
    {//Creates inputs going backwards from the selected date
        double trimester;
        INDArray input = Nd4j.zeros(MODELING_MONTHS_INPUT);

        for(int dec = 1; dec <= MODELING_MONTHS_INPUT; dec++)
        {
            if(TRIMESTER_MODEL_XTRA == 0){
                input.putScalar(dec-1, precip[index][date-dec]);
            }else{
                trimester = 0;
                for (int j = 0; j < 3; j++)
                {
                    trimester += precip[index][date-dec-j];
                }
                trimester /= 3;
                input.putScalar(dec-1, trimester);
            }
        }
        return input;
    }

    public double[] Output(int date, int index, int LEAD_TIME)
    {
        double[] output = new double[MODELING_NB_OUTPUTS];
        for(int i = 0; i < MODELING_NB_OUTPUTS; i++)
        {
            for (int j = 0; j < 3; j++)
            {//Computes the average for the desired trimester
                output[i] += precip[index][date+LEAD_TIME+j];
            }
            output[i] /= 3;
        }
        return output;
    }

    public String[] getStations(){
        return stations;
    }

    public INDArray getSST(){
        return SST;
    }

    public double[][] getPrecip(){
        return precip;
    }

    public ArrayList<Date> getHeader(){
        return header;
    }
}
