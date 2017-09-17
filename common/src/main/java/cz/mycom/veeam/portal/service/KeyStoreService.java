package cz.mycom.veeam.portal.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

/**
 * @author dursik
 */
@Slf4j
@Service
public final class KeyStoreService {
    private static final String KEYSTORE_TYPE = "JCEKS";
    private static final String ALGORITHM = "Blowfish";
    private static final Object lock = new Object();
    private char[] enumeration;
    @Value("file://${config.path}/passwords.jceks")
    private File keyStoreFile;

    public KeyStoreService() {
        char[][] e = new char[5][10];
        e[0] = new char[]{36, 66, 105, 36, 123, 120, 118, 67, 96, 67};
        e[1] = new char[]{61, 34, 38, 47, 54, 47, 106, 121, 74, 82};
        e[2] = new char[]{52, 104, 98, 67, 106, 34, 61, 87, 90, 80};
        e[3] = new char[]{84, 42, 90, 97, 36, 113, 58, 95, 79, 89};
        e[4] = new char[]{105, 51, 116, 33, 53, 101, 79, 35, 93, 33};

        enumeration = new char[50];

        int index = 0;
        for (int i = e.length - 1; i >= 0; i--) {
            for (int j = 0; j < e[i].length; j++) {
                enumeration[index++] = e[i][j];
            }
        }
    }

    @PostConstruct
    public void check() {
        if (!keyStoreFile.exists()) {
            throw new BeanInitializationException("Unable to find KeyStore");
        }
        if (!keyStoreFile.canRead()) {
            throw new BeanInitializationException("Unable to read KeyStore");
        }
        if (!keyStoreFile.canWrite()) {
            throw new BeanInitializationException("Unable to write KeyStore");
        }
    }

    public String readData(String alias) {
        synchronized (lock) {
            try {
                KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) loadKeyStore().getEntry(alias, new KeyStore.PasswordProtection(enumeration));
                if (entry == null) {
                    throw new AuthenticationCredentialsNotFoundException("Entry [" + alias + "] not found");
                }
                SecretKey secretKey = entry.getSecretKey();
                return new String(secretKey.getEncoded());
            } catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException e) {
                log.error(e.getMessage(), e);
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    public void storeData(String alias, String entry) {
        synchronized (lock) {
            try {
                SecretKey secretKey = new SecretKeySpec(entry.getBytes(), ALGORITHM);
                KeyStore.SecretKeyEntry skEntry = new KeyStore.SecretKeyEntry(secretKey);
                KeyStore keyStore = loadKeyStore();
                keyStore.setEntry(alias, skEntry, new KeyStore.PasswordProtection(enumeration));
                log.debug("Entry [" + alias + "] successfully stored to the key store.");
                FileOutputStream fos = new FileOutputStream(keyStoreFile);
                keyStore.store(fos, enumeration);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    private KeyStore loadKeyStore() {
        FileInputStream fis = null;
        try {
            KeyStore store = KeyStore.getInstance(KEYSTORE_TYPE);
            fis = new FileInputStream(keyStoreFile);
            store.load(fis, enumeration);
            return store;
        } catch (NoSuchAlgorithmException | KeyStoreException | IOException | CertificateException e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }
}
