Prerequisites for the CaGrid plugin to be able to invoke secure CaGrid services

The trusted-certificates folder contains a list of .0 files that are certificates of various CaGrid CAs
in PEM format. These certificates were copied from the <HOME>/.globus/certificates folder as of 26th June 2009
of a local CaGrid installation that syncs them and the related CRLs periodically if CaGrid is running.  

To print/see the contents of a certificate file, do:
openssl x509 -in <CERTIFICATE_FILE_PATH> -noout -text

For the secure caGrid services to be invokable from Taverna, these certificates need to be installed in the user's
Java truststore. This is needed for establishing https (http over SSL) connections to such services and for Java 
to be able trust the service on the other end of the connection. This basically mean that when Taverna attempts 
to open an https connection to a secure CaGrid service, Java will find the certificate of the CA that issued the 
service's certificate among the trusted certificates in the truststore and implicitly will trust the service as well. 

Java truststore is a special keystore that contains trusted certificates. It comes preloaded with a number of
trusted CA's certificates, such as VeriSign, and is located in <JAVA_HOME>/lib/security/cacerts file. In order to 
import a trusted certificate into the truststore, do:

keytool -import -keystore <TRUSTSTORE_PATH> -alias <ALIAS_FOR_CERTIFICATE> -file <CERTIFICATE_FILE_PATH> -storepass <YOUR_KEYSTORE_PASSWORD>

where <ALIAS_FOR_CERTIFICATE> is the alias you want to refer to this certificate to and the default Java truststore 
password is 'changeit' if you haven't already changed it. To verify that a certificate has been imported in the truststore, do:

keytool -list -keystore <TRUSTSTORE_PATH> -alias <ALIAS_FOR_CERTIFICATE> -storepass <YOUR_KEYSTORE_PASSWORD>

This should be repeated for each certificate file in this folder after installing the plugin and prior to using it. It 
is always a good idea to back up your truststore before you start adding certificates to it.