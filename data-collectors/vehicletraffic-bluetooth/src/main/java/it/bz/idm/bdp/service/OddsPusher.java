package it.bz.idm.bdp.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import it.bz.idm.bdp.dto.DataMapDto;
import it.bz.idm.bdp.dto.OddsRecordDto;
import it.bz.idm.bdp.dto.ProvenanceDto;
import it.bz.idm.bdp.dto.RecordDtoImpl;
import it.bz.idm.bdp.json.NonBlockingJSONPusher;
import it.bz.idm.bdp.util.EncryptUtil;
import it.bz.idm.bdp.web.RecordList;

@Lazy
@Component
@PropertySource({ "classpath:/META-INF/spring/application.properties" })
public class OddsPusher extends NonBlockingJSONPusher {

	@Autowired
	private Environment env;

	@Autowired
	private EncryptUtil cryptUtil;

	@Override
	public String initIntegreenTypology() {
		return "BluetoothStation";
	}

	/**
	 * maps received data from bluetoothbox to ODH and encrypts mac addresses
	 */
	@Override
	public <T> DataMapDto<RecordDtoImpl> mapData(T data) {
		return null;
	}

	@Override
	public ProvenanceDto defineProvenance() {
		return new ProvenanceDto(null, env.getProperty("provenance.name"), env.getProperty("provenance.version"),  env.getProperty("origin"));
	}

    public List<String> hash(RecordList records) {
        List<String> hashes = new ArrayList<String>();
        for (OddsRecordDto r : records) {
            String hash = cryptUtil.encrypt(r.getMac());
            hashes.add(hash);
        }
        return hashes;
    }
}