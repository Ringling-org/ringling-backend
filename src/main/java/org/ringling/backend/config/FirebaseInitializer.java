package org.ringling.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.beans.factory.annotation.Value;
import froggy.winterframework.stereotype.Component;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Firebase SDK 초기화 클래스
 * - 서비스 계정 키 파일을 읽어 FirebaseApp을 전역으로 등록함
 */
@Component
public class FirebaseInitializer {
    private String privateKeyPath;

    @Autowired
    public FirebaseInitializer(@Value("firebase.private-Key") String privateKeyPath) throws IOException {
        this.privateKeyPath = privateKeyPath;

        InputStream in = getClass().getClassLoader().getResourceAsStream(privateKeyPath);
        if (in == null) {
            throw new FileNotFoundException(privateKeyPath + " not found in classpath");
        }

        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(in))
            .build();
        FirebaseApp.initializeApp(options);
    }
}
