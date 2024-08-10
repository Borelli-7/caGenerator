package dev.kaly7.model;

import org.bouncycastle.asn1.DERUTF8String;

/**
 * A class representing a UTF-8 encoded string for the NCA (Name Component Attribute).
 *
 * <p>This class extends the {@link DERUTF8String} class from the BouncyCastle library,
 * which provides a way to handle UTF-8 encoded strings in ASN.1 (Abstract Syntax Notation One) format.
 * It is used to encapsulate a string value in a format compatible with ASN.1 encoding.</p>
 *
 * <p>The constructor accepts a string value and passes it to the superclass constructor to
 * initialize the UTF-8 encoded string.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 *     NcaName ncaName = new NcaName("ExampleName");
 *     String value = ncaName.getString(); // retrieves the UTF-8 encoded string
 * </pre>
 */
public class NcaName extends DERUTF8String {

    /**
     * Constructs an {@code NcaName} instance with the specified UTF-8 encoded string.
     *
     * @param string the UTF-8 encoded string to be encapsulated in this {@code NcaName} object
     */
    public NcaName(String string) {
        super(string);
    }
}