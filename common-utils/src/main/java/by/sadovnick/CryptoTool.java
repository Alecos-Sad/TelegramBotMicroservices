package by.sadovnick;

import org.hashids.Hashids;

/**
 * Для шифрования ids
 */
public class CryptoTool {
    private final Hashids hashids;

    public CryptoTool(String salt) {
        //min длина генерируемого хэша
        int minHashLength = 10;
        this.hashids = new Hashids(salt, minHashLength);
    }

    /**
     * Делает из id хэш
     */
    public String hashOf(Long value) {
        return hashids.encode(value);
    }

    /**
     * Делает из хэша id
     */
    public Long idOf(String value) {
        long[] res = hashids.decode(value);
        if (res != null && res.length > 0) {
            return res[0];
        }
        return null;
    }
}
