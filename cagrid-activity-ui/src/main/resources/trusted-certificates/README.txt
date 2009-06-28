Prerequisites for the CaGrid plugin to be able to invoke secure CaGrid services

The trusted-certificates folder contains a list of .0 files that are certificates of various CaGrid CAs
in PEM format. These certificates were copied from the <HOME>/.globus/certificates folder of a local CaGrid 
installation (as of 26th June 2009) that syncs them and the related CRLs periodically if CaGrid is running.  

To print/see the contents of a certificate file, do:
openssl x509 -in <CERTIFICATE_FILE_PATH> -noout -text

For the secure caGrid services to be invokable from Taverna, these certificates need to be installed in the user's
Java truststore. This is needed for establishing https (http over SSL) connections to such services and for Java 
to be able trust the service on the other end of the connection. This basically means that when Taverna attempts 
to open an https connection to a secure CaGrid service, Java will find the certificate of the CA that issued the 
service's certificate among the trusted certificates in the truststore and implicitly will trust the service as well. 

Java truststore is a special keystore that contains trusted certificates. It comes preloaded with a number of
trusted CA's certificates, such as VeriSign, and is located in <JAVA_HOME>/lib/security/cacerts file. In order to 
import a trusted certificate into the truststore, do:

keytool -import -keystore <TRUSTSTORE_PATH> -alias <ALIAS_FOR_CERTIFICATE> -file <CERTIFICATE_FILE_PATH> -storepass <TRUSTSTORE_PASSWORD>

where <ALIAS_FOR_CERTIFICATE> is the alias you want to refer to this certificate to and the default Java truststore 
password is 'changeit' if you haven't already done so. To verify that a certificate has been successfully imported 
to the truststore, do:

keytool -list -keystore <TRUSTSTORE_PATH> -alias <ALIAS_FOR_CERTIFICATE> -storepass <TRUSTSTORE_PASSWORD>

This should be repeated for each certificate file in this folder after installing the plugin and prior to using it. It 
is always a good idea to back up your truststore before you start adding certificates to it.

File cagrid-truststore.jks contained in this folder is a keystore that is pre-loaded with the trusted certificates 
found in this folder. It is not used from inside Taverna and is there just for convenience reasons. You may access 
it with password 'changeit'.
