package de.isuret.polos.AetherOnePi.processing.communication;

import lombok.Getter;
import de.isuret.polos.AetherOnePi.domain.AetherOnePiStatus;
import de.isuret.polos.AetherOnePi.service.BroadCastService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.SocketException;
import java.util.*;

/**
 * Status services handling the notification of all clients
 */
@Service
public class StatusNotificationService {

    private Log logger = LogFactory.getLog(BroadCastService.class);

    private Map<String,SocketClient> clients = new HashMap<>();

    private AetherOnePiStatus status;

    @PostConstruct
    public void init() {
        status = new AetherOnePiStatus();
    }

    /**
     * Register a client
     * @param ipAddress
     * @throws IOException
     */
    public void registerClient(String ipAddress) throws IOException {

        if (clients.get(ipAddress) != null) return;

        SocketClient client = new SocketClient();
        client.startConnection(ipAddress, 5555);
        clients.put(ipAddress, client);
        logger.info("Client for " + ipAddress + " registered.");
    }

    public void setProgress(Integer progress) throws IOException {

        status.setProgress(progress);
        sendStatus();
    }

    public void setHotbitsPackages(Integer siteOfPackages) throws IOException {
        status.setHotbitsPackages(siteOfPackages);
        sendStatus();
    }

    /**
     * Sends the status to all registered clients
     * @throws IOException
     */
    public void sendStatus() throws IOException {

        logger.info(status);
        String obsoleteClientHost = null;

        for (SocketClient client : clients.values()) {
            try {
                client.sendStatus(status);
            } catch (SocketException e) {
                obsoleteClientHost = client.getAddress();
                break;
            }
        }

        if (obsoleteClientHost != null) {
            removeClient(obsoleteClientHost);
        }
    }

    private void removeClient(String obsoleteClientHost) {
        logger.warn("removing obsolete client " + obsoleteClientHost);
        clients.remove(obsoleteClientHost);
    }

    public AetherOnePiStatus getStatus() {
        return status;
    }
}
