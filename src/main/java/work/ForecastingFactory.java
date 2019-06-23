package work;

import managers.Manager;
import utils.DataFormatter;
import utils.Signature;

public class ForecastingFactory extends WorkFactory
{
    private DataFormatter formatter;
    private int type;
    private int selection;

    public ForecastingFactory(Manager manager, DataFormatter formatter, int type, int selection)
    {
        super(manager);
        this.formatter = formatter;
        this.type = type;
        this.selection = selection;
    }

    public Work creatWork(Signature signature){
        return new Forecasting(signature, formatter, signature.getDepartment(), type, selection);
    }
}
