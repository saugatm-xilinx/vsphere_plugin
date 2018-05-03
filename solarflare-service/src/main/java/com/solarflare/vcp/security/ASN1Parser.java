package com.solarflare.vcp.security;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.solarflare.vcp.cim.SfCIMService;
import com.solarflare.vcp.security.model.ASN1Cursor;
import com.solarflare.vcp.security.model.PKCS7_Data;
import com.solarflare.vcp.security.model.PKCS7_SignedData;
import com.solarflare.vcp.security.model.SignedData;
import com.solarflare.vcp.security.model.SignedData_Version;
import com.solarflare.vcp.security.model.Tag;

/**
 * This class is written with reference to ef10_image.c file from solarflare
 * Utility routines to support limited parsing of ASN.1 tags. This is not a
 * general purpose ASN.1 parser, but is sufficient to locate the required
 * objects in a signed image with CMS headers.
 * 
 * @author
 *
 */
public class ASN1Parser {

	private static final Log logger = LogFactory.getLog(ASN1Parser.class);

	/**
	 * Parse header of DER encoded ASN.1 TLV and match tag
	 * 
	 * @param cursor
	 * @param tag
	 * @return
	 */
	private boolean parseHeaderAndMatchTag(ASN1Cursor cursor, int tag) {
		logger.info("Solarflare::  parseHeaderAndMatchTag");
		if (cursor == null || cursor.getBuffer() == null || cursor.getLength() < 2) {
			logger.error("Input params is null");
			return false;
		}

		int index = cursor.getBufferIndex();

		cursor.setTag(cursor.getBuffer()[index]);
		if (cursor.getTag() != tag) {
			logger.error("Tag not matched");
			return false;
		}
		if ((cursor.getTag() & 0x1F) == 0x1F) {
			logger.error("Long tag format not used in CMS syntax");
			return false;
		}
		if (0 <= cursor.getBuffer()[index + 1] && cursor.getBuffer()[index + 1] < 128) {
			/*
			 * If the value of the first byte is between 0 and 128 (exclusive)
			 * then that value is the length
			 */
			cursor.setHeaderSize(2);
			cursor.setValueSize(cursor.getBuffer()[index + 1]);
		} else {
			/* Long form: length encoded as [0x80+nbytes][length bytes] */
			int nbytes = cursor.getBuffer()[index + 1] & 0x7F;
			int offset;

			if (nbytes == 0) {
				logger.error("Indefinite length not allowed in DER encoding");
				return false;
			}
			if (2 + nbytes > cursor.getLength()) {
				logger.error("Header length overflows image buffer");
				return false;
			}
			if (nbytes > 4) {
				logger.error("Length encoding too big");
				return false;
			}
			cursor.setHeaderSize(2 + nbytes);
			int length = 0;
			for (offset = 2; offset < cursor.getHeaderSize(); offset++) {

				length = length * 256 + cursor.getBuffer()[offset];
			}

			cursor.setValueSize(length);

		}

		return true;
	}

	/**
	 * Enter nested ASN.1 TLV (contained in value of current TLV)
	 * 
	 * @param cursor
	 * @param tag
	 * @return
	 */
	private boolean enterTag(ASN1Cursor cursor, int tag) {
		logger.info("Solarflare:: enterTag");
		if (cursor == null) {
			logger.error("Input cursor is null");
			return false;
		}

		if (isTagPrim(tag)) {
			logger.error("Cannot enter a primitive tag");
			return false;
		}

		boolean isSuccess = parseHeaderAndMatchTag(cursor, tag);
		if (!isSuccess) {
			logger.error("Invalid TLV or wrong tag");
			return false;
		}

		/* Limit cursor range to nested TLV */
		// set the new buffer index and length
		cursor.setBufferIndex(cursor.getBufferIndex() + cursor.getHeaderSize());
		cursor.setLength(cursor.getValueSize());
		return true;
	}

	private boolean isTagPrim(int tag) {
		return (tag & 0x20) == 0;
	}

	/**
	 * Check that the current ASN.1 TLV matches the given tag and value. Advance
	 * cursor to next TLV on a successful match.
	 * 
	 * @param cursor
	 * @param tag
	 * @param signedData
	 * @return
	 */
	private boolean matchTagValue(ASN1Cursor cursor, int tag, SignedData signedData) {
		logger.info("Solarflare:: matchTagValue");
		if (cursor == null) {
			logger.error("Input cursor is null");
			return false;
		}
		boolean isSuccess = parseHeaderAndMatchTag(cursor, tag);
		if (!isSuccess) {
			logger.error("Invalid TLV or wrong tag");
			return false;
		}
		if (cursor.getValueSize() != signedData.getValueSize()) {
			logger.error("Value size is different");
			return false;
		}

		boolean isMatch = matchSignedData(cursor, signedData);
		if (!isMatch) {
			logger.error("Value content is different");
			return false;
		}
		// set new buffer index and length
		cursor.setBufferIndex(cursor.getBufferIndex() + cursor.getHeaderSize() + cursor.getValueSize());
		int length = cursor.getLength() - cursor.getHeaderSize() + cursor.getValueSize();
		cursor.setLength(length);

		return true;
	}

	private boolean matchSignedData(ASN1Cursor cursor, SignedData signedData) {
		int from = cursor.getBufferIndex() + cursor.getHeaderSize();
		int to = from + cursor.getValueSize();
		short[] tempBuffer = Arrays.copyOfRange(cursor.getBuffer(), from, to);
		if (!Arrays.equals(tempBuffer, signedData.getData())) {
			/* Value content is different */
			return false;
		}
		return true;
	}

	/**
	 * Get unsigned data for given byte data
	 * 
	 * @param fileData
	 * @return
	 */
	private short[] getUnsignedShort(byte[] fileData) {
		short[] unsignedByteArray = new short[fileData.length];
		int i = 0;
		for (byte data : fileData) {
			int firstByte = (0x000000FF & ((int) data));
			short anUnsignedByte = (short) firstByte;
			unsignedByteArray[i++] = anUnsignedByte;
		}
		return unsignedByteArray;
	}

	/**
	 * Advance cursor to next TLV
	 * 
	 * @param cursor
	 * @param tag
	 * @return
	 */
	private boolean skipTag(ASN1Cursor cursor, int tag) {
		logger.info("Solarflare:: skipTag");
		if (cursor == null) {
			logger.error("Input cursor is null");
			return false;
		}

		boolean isSuccess = parseHeaderAndMatchTag(cursor, tag);
		if (!isSuccess) {
			logger.error("Invalid TLV or wrong tag");
			return false;
		}

		// set new Buffer index and length
		cursor.setBufferIndex(cursor.getBufferIndex() + cursor.getHeaderSize() + cursor.getValueSize());
		int length = cursor.getLength() - cursor.getHeaderSize() + cursor.getValueSize();
		cursor.setLength(length);
		return true;
	}

	/**
	 * Return pointer to value octets and value size from current TLV
	 * 
	 * @param cursor
	 * @param tag
	 * @return
	 */
	private boolean getTagValue(ASN1Cursor cursor, int tag) {
		logger.info("Solarflare:: getTagValue");
		if (cursor == null) {
			logger.error("Input cursor is null");
			return false;
		}
		boolean isSuccess = parseHeaderAndMatchTag(cursor, tag);
		if (!isSuccess) {
			logger.error("Invalid TLV or wrong tag");
			return false;
		}

		// set new buffer index
		cursor.setBufferIndex(cursor.getBufferIndex() + cursor.getHeaderSize());

		return true;
	}

	/**
	 * Check for a valid image in signed image format. This uses CMS syntax (see
	 * RFC2315, PKCS#7) to provide signatures, and certificates required to
	 * validate the signatures. The encapsulated content is in unsigned image
	 * format (reflash header, image code, trailer checksum).
	 * 
	 * @param fileData
	 * @return
	 */
	private byte[] getSignedImageHeader(byte[] fileData) {
		logger.info("Solarflare:: getSignedImageHeader");
		ASN1Cursor cursor = new ASN1Cursor();
		cursor.setBuffer(getUnsignedShort(fileData));
		cursor.setBufferIndex(0);
		cursor.setLength(fileData.length);
		boolean isSuccess;

		/* ContextInfo */
		isSuccess = enterTag(cursor, Tag.ASN1_TAG_SEQUENCE);
		if (!isSuccess) {
			logger.error("Error at ContextInfo");
			return null;
		}

		/* ContextInfo.contentType */
		isSuccess = matchTagValue(cursor, Tag.ASN1_TAG_OBJ_ID, new PKCS7_SignedData());
		if (!isSuccess) {
			logger.error("Error at ContextInfo.contentType");
			return null;
		}

		/* ContextInfo.content */
		isSuccess = enterTag(cursor, tagConsContext(0));
		if (!isSuccess) {
			logger.error("Error at ContextInfo.content");
			return null;
		}

		/* SignedData */
		isSuccess = enterTag(cursor, Tag.ASN1_TAG_SEQUENCE);
		if (!isSuccess) {
			logger.error("Error at SignedData");
			return null;
		}

		/* SignedData.version */
		isSuccess = matchTagValue(cursor, Tag.ASN1_TAG_INTEGER, new SignedData_Version());
		if (!isSuccess) {
			logger.error("Error at SignedData.version");
			return null;
		}

		/* SignedData.digestAlgorithms */
		isSuccess = skipTag(cursor, Tag.ASN1_TAG_SET);
		if (!isSuccess) {
			logger.error("Error at SignedData.digestAlgorithms");
			return null;
		}

		/* SignedData.encapContentInfo */
		isSuccess = enterTag(cursor, Tag.ASN1_TAG_SEQUENCE);
		if (!isSuccess) {
			logger.error("Error at SignedData.encapContentInfo");
			return null;
		}

		/* SignedData.encapContentInfo.econtentType */
		isSuccess = matchTagValue(cursor, Tag.ASN1_TAG_OBJ_ID, new PKCS7_Data());
		if (!isSuccess) {
			logger.error("Error at SignedData.encapContentInfo.econtentType");
			return null;
		}

		/* SignedData.encapContentInfo.econtent */
		isSuccess = enterTag(cursor, tagConsContext(0));
		if (!isSuccess) {
			logger.error("Error at SignedData.encapContentInfo.econtent");
			return null;
		}

		/*
		 * The octet string contains the image header, image code bytes and
		 * image trailer CRC (same as unsigned image layout).
		 */
		isSuccess = getTagValue(cursor, Tag.ASN1_TAG_OCTET_STRING);
		if (!isSuccess) {
			logger.error("Error at get octet");
			return null;
		}

		short[] header = Arrays.copyOfRange(cursor.getBuffer(), cursor.getBufferIndex(), cursor.getValueSize());
		byte[] headerInByte = convertToByteArray(header);

		return headerInByte;
	}

	private int tagConsContext(int n) {
		return 0xA0 + n;
	}

	private static byte[] convertToByteArray(short[] input) {
		byte[] ret = new byte[input.length];
		for (int i = 0; i < input.length; i++) {
			ret[i] = (byte) input[i];
		}
		return ret;
	}

	public byte[] getFileHeaderBytes(byte[] fileData) {
		logger.info("Solarflare:: getFileHeaderBytes");
		byte[] headerDataBytes = null;
		// check if file is signed binary
		headerDataBytes = getSignedImageHeader(fileData);

		if (headerDataBytes == null) {
			// Unsigned binary file. return first 40 bytes
			logger.info("Getting header bytes for Unsigned Binary");
			headerDataBytes = Arrays.copyOf(fileData, 40);
		} else {
			logger.info("Getting header bytes for Signed Binary");
		}
		return headerDataBytes;
	}
}
