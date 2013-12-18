package se.skltp.nt.svc;

/**
 * Missing string manipulations.
 *
 * @author mats.olsson@callistaenterprise.se
 */
public class StringUtil {

    public static String join(String[] parts, String delimiter) {
        return join(parts, 0, parts.length, delimiter);
    }

    public static String join(String[] parts, int startIndex, int endIndex, String delimiter) {
        StringBuilder sb = new StringBuilder();
        for ( int i = startIndex; i < endIndex; i++ ) {
            String part = parts[i];
            if ( i != startIndex ) {
                sb.append(delimiter);
            }
            sb.append(part);
        }
        return sb.toString();
    }
}
