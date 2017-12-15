package com.solarflare.vcp.helper;

public class SFBase64 {
	/// Base64 coding table
    static byte base64_code_table[] = {
                                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
                                'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
                                'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
                                'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
                                'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
                                'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
                                'w', 'x', 'y', 'z', '0', '1', '2', '3',
                                '4', '5', '6', '7', '8', '9', '+', '/' };

    ///
    /// Compute size of encoded string for a given binary data size
    ///
    /// @param src_size     Size of binary data to be encoded
    ///
    /// @return Number of bytes to be allocated for the encoded string
    ///
    public int base64_enc_size(int src_size)
    {
        return (src_size / 3 + (src_size % 3 > 0 ? 1 : 0)) * 4 +
               ((3 - src_size % 3) % 3) + 1;
    }
    
  ///
    /// Encode binary data in base64 string
    ///
    /// @param dst        Where to store encoded string
    /// @param src        Data to be encoded
    /// @param src_size   Size of data to be encoded
    ///
    public byte[] base64_encode(byte[] src,
                               int src_size)
    {
    	int destSize = base64_enc_size(src_size);
    	byte[] dst =  new byte[destSize];
    	int i;
        int j;
        int a;
        int b;
        int c;

        for (i = 0;
             i < ((src_size % 3 == 0) ?
                  (src_size / 3) : (src_size / 3 + 1));
             i++)
        {
            a = src[3 * i];

            if (3 * i + 1 < src_size)
                b = src[3 * i + 1];
            else
                b = 0;

            if (3 * i + 2 < src_size)
                c = src[3 * i + 2];
            else
                c = 0;

            dst[4 * i] = base64_code_table[(a >> 2) & 0x3f];
            dst[4 * i + 1] = base64_code_table[((a << 4) & 0x30)
                                               + ((b >> 4) & 0x0f)];
            dst[4 * i + 2] = base64_code_table[((b << 2) & 0x3c)
                                               + ((c >> 6) & 0x03)];
            dst[4 * i + 3] = base64_code_table[c & 0x3f];
        }

        for (j = 0; j < (3 - src_size % 3) % 3; j++)
            dst[4 * i + j] = '=';

        dst[4 * i + j] = '\0';
        return dst;
    }
}
