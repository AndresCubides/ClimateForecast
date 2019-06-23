package utils;

import java.util.Date;


public interface Constante 
{
	public static final Parameter parameter  = Parameter.getInstance();

	/*********************
	 * 
	 * Distributed config
	 *
	 *********************/
	//Server IP CHECK
	public static final String CLIENT_SERVER_ADDRESS = parameter.getString("CLIENT_SERVER_ADDRESS");
	//Time before contacting the server after a crash
	public static final int CLIENT_WAITING_TIME = parameter.getInteger("CLIENT_WAITING_TIME");
	//Reload situation
	public final static boolean LOAD_FOR_SAVE_POINT = false;
	public static final String CLIENT_SECURITY_POLICY  = parameter.getString("CLIENT_SECURITY_POLICY");

	/*********************
	 * 
	 * PATHS
	 *
	 *********************/

	public static final String PATH_ROOT = parameter.getString("PATH_ROOT");
	public static final String PATH_CONFIG  = parameter.getString("PATH_CONFIG");
    public static final String PATH_MODELS  = parameter.getString("PATH_MODELS");
    public static final String PATH_INPUTS  = parameter.getString("PATH_INPUTS");
    public static final String PATH_OUTPUTS  = parameter.getString("PATH_OUTPUTS");
    public static final String PATH_SST = parameter.getString("PATH_SST");
    public static final String PATH_DEPT_LIST = parameter.getString("PATH_DEPT_LIST");
	public static final String PATH_DEPT_DATA = parameter.getString("PATH_DEPT_DATA");

	/*********************
	 * 
	 * Modeling
	 *
	 *********************/
	
	//Modeling start date
	public static final Date START_MODELING = parameter.getDate("START_MODELING");
	//Modeling end date
	public static final Date END_MODELING = parameter.getDate("END_MODELING");
	//Register the models locally 1:Yes 0:No
	public static final int SAVE_LOCALLY = parameter.getInteger("SAVE_LOCALLY");
	public static final int BATCH_SIZE = parameter.getInteger("BATCH_SIZE");
	//Number of months (or trimester in case TRIMESTER_MODEL_XTRA = 2) as input to train the models
	public static final int MODELING_MONTHS_INPUT = parameter.getInteger("MODELING_MONTHS_INPUT");
	//Set to 0 to model with pooling layers
	public static final int MODELING_TYPE = parameter.getInteger("MODELING_TYPE");
	//Number of neurons in the layer 1
	public static final int MODELING_NB_HIDDEN_LAYER1 = parameter.getInteger("MODELING_NB_HIDDEN_LAYER1");
	//Number of neurons in the layer 2
	public static final int MODELING_NB_HIDDEN_LAYER2 = parameter.getInteger("MODELING_NB_HIDDEN_LAYER2");
	//Number of neurons in the layer 3
	public static final int MODELING_NB_HIDDEN_LAYER3 = parameter.getInteger("MODELING_NB_HIDDEN_LAYER3");
	//Number of neurons in the output layer
	public static final int MODELING_NB_OUTPUTS = parameter.getInteger("MODELING_NB_OUTPUTS");
	//Max. number of epochs to run without improvement before stopping the training process
	public static final int NO_IMPROVE_EPOCHS = parameter.getInteger("NO_IMPROVE_EPOCHS");
    public static final int MINUTES_PER_MODEL = parameter.getInteger("MINUTES_PER_MODEL");
	public static final double MODELING_LEARNING_RATE = parameter.getDouble("MODELING_LEARNING_RATE");
	public static final double MODELING_EPSYLON = parameter.getDouble("MODELING_EPSYLON");
	
	/*********************
	 * 
	 * Forecasting
	 *
	 *********************/
	//Forecasting starting date
	public static final Date START_DETECTION = parameter.getDate("START_DETECTION");
    //Forecasting end date
    public static final Date END_DETECTION = parameter.getDate("END_DETECTION");
    //Number of months between the inputs and the desired trimester to predict
	public static final int LEAD_TIME = parameter.getInteger("LEAD_TIME");
	//Set to 0 to train models with monthly data and set to 2 to train with previous trimesters data
	public static final int TRIMESTER_MODEL_XTRA = parameter.getInteger("TRIMESTER_MODEL_XTRA");
	//Automatically set by R script, value used to normalize data and to reconstruct the original scale
    public static final int PRECIP_MAX = parameter.getInteger("PRECIP_MAX");

    //Quit
	public final static int QUITTER = -1;
	
}
