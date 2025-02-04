package it.bz.idm.bdp.augeg4.fun.push;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.bz.idm.bdp.augeg4.dto.toauge.AugeG4ProcessedDataToAugeDto;
import it.bz.idm.bdp.augeg4.face.DataPusherAugeFace;
import it.bz.idm.bdp.augeg4.util.AugeMqttClient;
import it.bz.idm.bdp.augeg4.util.AugeMqttConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;


public class DataPusherAuge implements DataPusherAugeFace {

    private static final Logger LOG = LogManager.getLogger(DataPusherAuge.class.getName());


    private AugeMqttConfiguration augeMqttConfiguration;

    private MqttClient client;

    public DataPusherAuge(AugeMqttConfiguration augeMqttConfiguration) {
        this.augeMqttConfiguration = augeMqttConfiguration;
        try {
            client = AugeMqttClient.build(augeMqttConfiguration);
            LOG.info("Mqtt Auge connected.");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pushData(List<AugeG4ProcessedDataToAugeDto> list) {
        int QUALITY_OF_SERVICE = 1;
        try {
            client = AugeMqttClient.build(augeMqttConfiguration);
            LOG.info("Mqtt Auge connected. ["+client.isConnected()+"]");
        } catch (MqttException e) {
            e.printStackTrace();
        }
        try {
            if (client != null && client.isConnected()) {
                for(AugeG4ProcessedDataToAugeDto augeDto: list) {
                    String content = jsonOf(augeDto);
                    LOG.debug(content);
                    publish(augeMqttConfiguration.getTopic(), content, QUALITY_OF_SERVICE, client);
                }
            } else {
                LOG.info("Can't push data: client not connected.");
            }
        } catch(MqttException me) {
            LOG.debug("reason "+me.getReasonCode());
            LOG.debug("msg "+me.getMessage());
            LOG.debug("loc "+me.getLocalizedMessage());
            LOG.debug("cause "+me.getCause());
            LOG.debug("excep "+me);
            me.printStackTrace();
        }
    }

    private String jsonOf(AugeG4ProcessedDataToAugeDto augeDto) {
        ObjectMapper mapper = new ObjectMapper();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        //dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
        mapper.setDateFormat(dateFormat);
        String json = null;
        if (augeDto== null) {
            throw new IllegalArgumentException();
        }
        try {
            json = mapper.writeValueAsString(augeDto);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }

    protected void publish(String topic, String content, int qos, MqttClient client) throws MqttException {
        LOG.debug("Publishing message: "+content);
        MqttMessage message = new MqttMessage(content.getBytes());
        message.setQos(qos);
        client.publish(topic, message);
        LOG.debug("topic ["+topic+"]");
        LOG.debug("message ["+message+"]");
        LOG.debug("Message published");
    }


}
