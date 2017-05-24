package il.ac.technion.cs.smarthouse.system.sensors;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class functions as a temporary server while waiting for an interactive
 * sensor to connect via its instructions connection.
 * 
 * @author Yarden
 * @author Inbal Zukerman
 * @since 6.4.17
 */
public class InteractiveSensorServer extends Thread {
    private static Logger log = LoggerFactory.getLogger(InteractiveSensorServer.class);

    private final OutputMapper mapper;

    public InteractiveSensorServer(final OutputMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void run() {
        try (ServerSocket router1 = new ServerSocket(40002)) {
            // TODO: ELIA if you still need this, add a private field here and
            // delete the previous one at SensorsHandler
            // this.router = router1;
            try {
                new InstructionsSenderThread(router1.accept(), (id, out) -> mapper.store(id, out)).start();
            } catch (final SocketException e) {
                log.warn("socket closed, InteractiveSensorServer is shutting down", e);
                return; // if we closed the sockets we want to shutoff the
                        // server
            } catch (final IOException e) {
                log.error("I/O error occurred while waiting for a connection", e);
            }
        } catch (final IOException e) {
            log.error("I/O error occurred when the socket was opened", e);
        }
    }
}
