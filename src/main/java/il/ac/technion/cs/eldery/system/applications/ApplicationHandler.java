package il.ac.technion.cs.eldery.system.applications;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.function.Predicate;

import il.ac.technion.cs.eldery.system.AppThread;
import il.ac.technion.cs.eldery.system.DatabaseHandler;
import il.ac.technion.cs.eldery.system.EmergencyLevel;
import il.ac.technion.cs.eldery.system.exceptions.ApplicationNotRegisteredToEvent;
import il.ac.technion.cs.eldery.utils.*;

/** API allowing smart house applications to register for information and notify
 * on emergencies
 * @author Elia Traore
 * @author Inbal Zukerman
 * @since Dec 13, 2016 */
public class ApplicationHandler {
    private class QueryTimerTask extends TimerTask {
        Boolean repeat;
        String sensorCommercialName;
        LocalTime t;
        Consumer<Table<String, String>> notifee;

        QueryTimerTask(final String sensorCommercialName, final LocalTime t, final Consumer<Table<String, String>> notifee, Boolean repeat) {
            this.repeat = repeat;
            this.notifee = notifee;
            this.t = t;
            this.sensorCommercialName = sensorCommercialName;
        }

        void setRepeat(Boolean ¢) {
            repeat = ¢;
        }

        /* (non-Javadoc)
         * 
         * @see java.util.TimerTask#run() */
        @SuppressWarnings({ "boxing" }) @Override public void run() {
            notifee.accept(querySensor(sensorCommercialName).orElse(new Table<String, String>()));
            if (repeat)
                new Timer().schedule(this, localTimeToDate(t));
        }

    }

    Map<String, Tuple<ApplicationManager, AppThread>> apps = new HashMap<>();// TODO: change to ApplicationManager only when the wanted behaviour there is implemented
    Map<String, QueryTimerTask> timedQuerires = new HashMap<>();
    DatabaseHandler databaseHandler;

    /** Initialize the applicationHandler with the database responsible of
     * managing the data in the current session */
    public ApplicationHandler(final DatabaseHandler databaseHandler) {
        this.databaseHandler = databaseHandler;
    }

    /** Adds a new application to the system.
     * @return The id of the application in the system */
    public String addApplication(final ApplicationManager appid, final SmartHouseApplication a) {
        // TODO: ELIA remove the app param, after ApplicationIdentifier is
        // completed
        apps.put(appid.getId(), new Tuple<>(appid, new AppThread(a)));
        return appid.getId();
    }
    
    /** Ask for the list of sensorIDs registered by a specific commercial name
     * @param sensorCommercialName the sensor in question
     * @return a list of IDs of those sensors in the system. They can be used in any "sensorID" field in any method
     * */
    @SuppressWarnings({"static-method"})
    public List<String> InqireAbout( String sensorCommercialName){
        return Collections.emptyList(); //TODO: implement in bunny
    }

    /** Allows registration to a sensor. on update, the data will be given to
     * the consumer for farther processing
     * @param id The id given to the application when added to the system
     * @param sensorID The ID of the sensor, returned from inquireAbout(sensorCommercialName)
     * @param notifyWhen A predicate that will be called every time the sensor
     *        updates the date. If it returns true the consumer will be called
     * @param notifee A consumer that will receive the new data from the sensor
     * @param numOfEntries The number of entries the application want to receive
     *        from the sensor upon update
     * @return The registration id if the action was successful, otherwise
     *         <code>null</code> */
    public String registerToSensor(final String id, final String sensorID, final Predicate<Table<String, String>> notifyWhen,
            final Consumer<Table<String, String>> notifee, int numOfEntries) {
        try {
            AppThread app = apps.get(id).right;
            final String eventId = app.registerEventConsumer(notifee);
            return databaseHandler.addListener(sensorID, t -> {
                if (notifyWhen.test(t))
                    try {
                        app.notifyOnEvent(eventId, t.receiveKLastEntries(numOfEntries));
                    } catch (final ApplicationNotRegisteredToEvent e) {
                        e.printStackTrace();
                    }
            });
        } catch (@SuppressWarnings("unused") final Exception __) {
            return null;
        }
    }

    static Date localTimeToDate(final LocalTime ¢) {
        return Date.from(¢.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant());
    }

    /** Allows registration to a sensor. on time, the sensor will be polled and
     * the data will be given to the consumer for farther processing
     * @param sensorID The ID of the sensor, returned from inquireAbout(sensorCommercialName)
     * @param t the time when a polling is requested
     * @param notifee A consumer that will receive the new data from the sensor,
     *        or an empty table if the was no new information.
     * @param repeat <code>false</code> if you want to query the sensor only
     *        once, <code>true</code> otherwise (query at this time FOREVER)
     * @return The registration id if the action was successful, otherwise
     *         <code>null</code> */
    public String registerToSensor(final String sensorID, final LocalTime t, final Consumer<Table<String, String>> notifee,
            Boolean repeat) {
        QueryTimerTask task = new QueryTimerTask(sensorID, t, notifee, repeat);
        String $ = Generator.GenerateUniqueIDstring();
        timedQuerires.put($, task);
        new Timer().schedule(task, localTimeToDate(t));
        return $;
    }

    /** Removes an existing registration. The consumer given at registration
     * will not be called again.
     * @param sensorID The ID of the sensor that is being listened to, returned from inquireAbout(sensorCommercialName)
     * @param id The id given at registration 
     * */
    public void cancelRegistration(final String sensorID, final String id) {
        if (timedQuerires.get(id) != null)
            timedQuerires.get(id).setRepeat(Boolean.FALSE);
        else
            try {
                databaseHandler.removeListener(sensorID, id);
            } catch (final Exception __) {
                __.printStackTrace();
            }
    }

    /** Request for the latest data received by a sensor
     * @param sensorID The ID of the sensor, returned from inquireAbout(sensorCommercialName)
     * @return the latest data (or Optional.empty() if the query failed in any
     *         point) */
    public Optional<Table<String, String>> querySensor(final String sensorID) {
        return databaseHandler.getLastEntryOf(sensorID);
    }

    /** Report an abnormality in the expected schedule. The system will contact
     * the needed personal, according to the urgency level
     * @param message Specify the abnormality, will be presented to the
     *        contacted personal
     * @param eLevel The level of personnel needed in the situation */
    public void alertOnAbnormalState(final String message, final EmergencyLevel eLevel) {
        // TODO: ELIA implement
    }
}