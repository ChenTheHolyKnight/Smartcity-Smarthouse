/**
 * 
 */
package il.ac.technion.cs.smarthouse.sensors.simulator;

import java.util.function.Consumer;

import il.ac.technion.cs.smarthouse.sensors.InstructionHandler;
import il.ac.technion.cs.smarthouse.sensors.InteractiveSensor;
import il.ac.technion.cs.smarthouse.sensors.PathType;

/**
 * @author Elia Traore
 * @since Jun 17, 2017
 */
public class SensorBuilder {
    private String commname, sensorId, alias;
    private InstructionHandler iHandler;

    final private GenericSensor genericSensor = new GenericSensor();
    
	public GenericSensor build(){
		genericSensor.setSensor(new InteractiveSensor(commname, sensorId, alias, 
														genericSensor.getPaths(PathType.INFO_SENDING),
														genericSensor.getPaths(PathType.INSTRUCTION_RECEIVING)));
		genericSensor.getSensor().setInstructionHandler((path,inst)->{
			genericSensor.logInstruction(path,inst);
			return iHandler == null || iHandler.applyInstruction(path, inst);
		});
		return genericSensor;
	}
	
    public SensorBuilder setInstructionHandler(InstructionHandler h){
        iHandler = h;
        return this;
    }
    
    public SensorBuilder setCommname(String comm){
        commname = comm;
        return this;
    }
    
    public SensorBuilder setSensorId(String sensorId){
        this.sensorId = sensorId;
        return this;
    }
    
    public SensorBuilder setAlias(String alias){
        this.alias = alias;
        return this;
    }

    public SensorBuilder addPath(PathType type, String path, Class pathClass){
    	genericSensor.addPath(type, path, pathClass);
    	return this;
    }
    
    public SensorBuilder addLogger(PathType t, Consumer<String> logger){
    	genericSensor.addLogger(t, logger);
    	return this;
    }
    
    public SensorBuilder setPollingInterval(Long milliseconds){
    	genericSensor.setPollingInterval(milliseconds);
    	return this;
    }
}
