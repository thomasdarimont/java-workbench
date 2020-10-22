package web.libtink;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KeyTemplate;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AesGcmKeyManager;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

public class HelloTink {

    public static void main(String[] args) throws GeneralSecurityException {
        AeadConfig.register();

        KeyTemplate keyTemplate = AesGcmKeyManager.aes128GcmTemplate();
        KeysetHandle keysetHandle = KeysetHandle.generateNew(keyTemplate);

        var plainText = "Hello World";

        String aad = "greeting";

        var aead = keysetHandle.getPrimitive(Aead.class);

        byte[] encryptedBytes = aead.encrypt(plainText.getBytes(StandardCharsets.UTF_8), aad.getBytes(StandardCharsets.UTF_8));
        String encrypted = Base64.getEncoder().encodeToString(encryptedBytes);
        System.out.println(encrypted);

        byte[] decryptedBytes = aead.decrypt(Base64.getDecoder().decode(encrypted), aad.getBytes(StandardCharsets.UTF_8));
        String recoveredPlainText = new String(decryptedBytes, StandardCharsets.UTF_8);
        System.out.println(recoveredPlainText);

    }
}
