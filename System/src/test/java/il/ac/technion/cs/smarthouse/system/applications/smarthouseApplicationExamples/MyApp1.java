package il.ac.technion.cs.smarthouse.system.applications.smarthouseApplicationExamples;

import il.ac.technion.cs.smarthouse.developers_api.SmarthouseApplication;

/**
 * @author RON
 * @since 30-05-2017
 */
public class MyApp1 extends SmarthouseApplication {

    @Override
    public void onLoad() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public String getApplicationName() {
        return getClass().getName();
    }

}
