package dev.kaly7.model;

import org.bouncycastle.asn1.DERUTF8String;

/**
 * Represents an NCA Identifier as a DER UTF8 String.
 * <p>
 * This class extends {@link DERUTF8String} to specifically handle NCA (National Competent Authority)
 * identifiers, which are encoded as UTF-8 strings.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 *     NcaId ncaId = new NcaId("exampleIdentifier");
 * </pre>
 * </p>
 */
public class NcaId extends DERUTF8String {

    /**
     * Constructs an {@code NcaId} with the specified string value.
     *
     * @param string the string value to be used as the NCA Identifier
     */
    public NcaId(String string) {
        super(string);
    }
}