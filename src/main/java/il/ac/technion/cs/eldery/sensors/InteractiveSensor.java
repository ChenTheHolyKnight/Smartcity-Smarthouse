package il.ac.technion.cs.eldery.sensors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;
import java.util.TimerTask;

import il.ac.technion.cs.eldery.networking.messages.AnswerMessage;
import il.ac.technion.cs.eldery.networking.messages.MessageFactory;
import il.ac.technion.cs.eldery.networking.messages.RegisterMessage;
import il.ac.technion.cs.eldery.networking.messages.UpdateMessage;
import il.ac.technion.cs.eldery.networking.messages.AnswerMessage.Answer;

/** This class represents a sensor that can get instructions and operate
 * accordingly.
 * @author Yarden
 * @since 31.3.17 */
public abstract class InteractiveSensor extends Sensor {
    public class InstructionChecker extends TimerTask {
        @Override public void run() {
            operate();
        }
    }

    protected int instPort;
    protected Socket instSocket;
    protected PrintWriter instOut;
    protected BufferedReader instIn;
    protected InstructionHandler handler;

    public InteractiveSensor(final String id, final String commName, final String systemIP, final int systemPort, final int instPort) {
        super(id, commName, systemIP, systemPort);
        this.instPort = instPort;
        this.handler = null;
    }

    /** Registers the sensor its instructions TCP connection with the system.
     * This method must be called before any instructions are sent.
     * @return <code>true</code> if registration was successful,
     *         <code>false</code> otherwise */
    public boolean registerInstructions() {
        try {
            instSocket = new Socket(InetAddress.getByName(systemIP), instPort);
            instOut = new PrintWriter(instSocket.getOutputStream(), true);
            instIn = new BufferedReader(new InputStreamReader(instSocket.getInputStream()));
        } catch (final IOException ¢) {
            ¢.printStackTrace();
        }
        final String $ = new RegisterMessage(id, commName).send(instOut, instIn);
        return $ != null && ((AnswerMessage) MessageFactory.create($)).getAnswer() == Answer.SUCCESS;
    }

    /** Sets the operation to be made when instruction is received. This method
     * must be called before sending instructions. */
    public void setInstructionHandler(InstructionHandler h) {
        this.handler = h;
    }

    /** If there is an incoming instruction, extracts it and executes it.
     * @return <code>true</code> if the instruction was completed successfully
     *         (or if there is no waiting instruction), <code>false</code>
     *         otherwise */
    public boolean operate() {
        try {
            return !instIn.ready() || handler.applyInstruction(((UpdateMessage) MessageFactory.create(instIn.readLine())).getData());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}

interface InstructionHandler {
    /** Acts due to an instruction.
     * @return <code>true</code> if the action was successful,
     *         <code>false</code> otherwise */
    boolean applyInstruction(Map<String, String> instructionData);
}
