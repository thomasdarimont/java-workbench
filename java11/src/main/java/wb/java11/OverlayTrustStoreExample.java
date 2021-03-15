package wb.java11;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

class OverlayTrustStoreExample {

    public static void main(String[] args) throws Exception {

//        demo1();
        demo2();
    }

    private static void demo2() throws Exception{


        TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        tmf.init((KeyStore) null);

        X509TrustManager tm = (X509TrustManager) tmf.getTrustManagers()[0];

        KeyStore acmeKeystore = KeyStore.getInstance(new File("/home/tom/dev/tmp/acme1.local.p12"), "changeit".toCharArray());

        X509Certificate[] acceptedIssuers = tm.getAcceptedIssuers();
        System.out.printf("acceptedIssuers: %s%n", acceptedIssuers.length);

        X509Certificate acmeCert = (X509Certificate) acmeKeystore.getCertificate("1");
        tm.checkServerTrusted(new X509Certificate[]{acmeCert}, "RSA");
        System.out.printf("Cert %s is trusted.%n", acmeCert);
    }

    private static void demo1() throws Exception {
        TrustManagerFactory tmfDefault = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmfDefault.init((KeyStore) null);

        KeyStore acmeKeystore = KeyStore.getInstance(new File("/home/tom/dev/tmp/acme1.local.p12"), "changeit".toCharArray());
        TrustManagerFactory tmfCustom = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmfCustom.init(acmeKeystore);

        DelegatingX509TrustManager dtm = new DelegatingX509TrustManager((X509TrustManager) tmfDefault.getTrustManagers()[0], (X509TrustManager) tmfCustom.getTrustManagers()[0]);

        X509Certificate[] acceptedIssuers = dtm.getAcceptedIssuers();
        System.out.printf("acceptedIssuers: %s%n", acceptedIssuers.length);

        X509Certificate acmeCert = (X509Certificate) acmeKeystore.getCertificate("1");
        dtm.checkServerTrusted(new X509Certificate[]{acmeCert}, "RSA");
        System.out.printf("Cert %s is trusted.%n", acmeCert);
    }

    static class DelegatingX509TrustManager implements X509TrustManager {

        private final X509TrustManager[] trustManagers;
        private final X509Certificate[] acceptedIssuers;


        public DelegatingX509TrustManager(X509TrustManager... trustManagers) {
            X509TrustManager[] local = trustManagers.clone();

            this.trustManagers = local;
            this.acceptedIssuers = Arrays.stream(local).map(X509TrustManager::getAcceptedIssuers).flatMap(Arrays::stream).toArray(X509Certificate[]::new);
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            checkAll(chain, authType, X509TrustManager::checkClientTrusted);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            checkAll(chain, authType, X509TrustManager::checkServerTrusted);
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return acceptedIssuers.clone();
        }

        private void checkAll(X509Certificate[] chain, String authType, CertificateCheck certificateCheck) throws CertificateException {

            CertificateException last = null;
            for (X509TrustManager tm : trustManagers) {
                try {
                    certificateCheck.check(tm, chain, authType);
                    return;
                } catch (CertificateException cex) {
                    last = cex;
                }
            }
            if (last != null) {
                throw last;
            }
        }
    }


    interface CertificateCheck {
        void check(X509TrustManager tm, X509Certificate[] chain, String authType) throws CertificateException;
    }
}

