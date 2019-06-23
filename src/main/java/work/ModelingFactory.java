package work;

import managers.Manager;
import utils.DataFormatter;
import utils.Signature;

public class ModelingFactory extends WorkFactory
{
    private DataFormatter formatter;

    public ModelingFactory(Manager manager, DataFormatter formatter)
    {
        super(manager);
        this.formatter = formatter;
    }

    public Work creatWork(Signature signature){
        return new Modeling(signature, formatter, signature.getDepartment());
    }
}
