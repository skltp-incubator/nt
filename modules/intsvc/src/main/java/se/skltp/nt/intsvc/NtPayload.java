package se.skltp.nt.intsvc;

import java.io.Serializable;

/**
 * @author mats.olsson@callistaenterprise.se
 */
public class NtPayload implements Serializable {

    private String serviceContractURI; // the namespace for the service contract
    private String serviceContractAction;// the name of the action (ie, the local name for the first node of the service contract)
    private String serviceContractBody;
    private String rivtaVersion;

    public NtPayload(String serviceContractURI, String serviceContractAction, String serviceContractBody, String rivtaVersion) {
        this.serviceContractURI = serviceContractURI;
        this.serviceContractAction = serviceContractAction;
        this.serviceContractBody = serviceContractBody;
        this.rivtaVersion = rivtaVersion;
    }

    public String getServiceContractURI() {
        return serviceContractURI;
    }

    public String getServiceContractBody() {
        return serviceContractBody;
    }


    /**
     * Calculate the trailing part of the url to use when calling VP.
     *
     * the uri is on the form "urn:riv[:superdomain]:domain:subdomain:name:version"
     * which translates to /superdomain/domain/subdomain/name/version/rivtabpXx
     * (note: early service contracts may be using just "/name/version/rivtabpXx")
     * @return trailing part of vp-endpoint url
     */
    public String getTrailingPartOfVpUrl() {
        String[] parts = serviceContractURI.split(":");
        StringBuilder sb = new StringBuilder();
        // skip the "urn:riv" part
        if (parts.length < 6) {
            throw new IllegalArgumentException("Illegal service contract URI" + serviceContractURI);
        }
        for (int i = 2; i < parts.length; i++) {
            sb.append("/").append(parts[i]);
        }
        sb.append("/").append(rivtaVersion);
        return sb.toString();
    }

    public String getRivtaVersion() {
        return rivtaVersion;
    }

    public Object getSoapAction() {
        return serviceContractURI + ":" + serviceContractAction;
    }
}
