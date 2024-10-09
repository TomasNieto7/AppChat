
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class CifradoAES {

    // Método para encriptar un texto dado usando una clave secreta
    public static String encriptar(String texto, SecretKey claveSecreta) throws Exception {
        Cipher cipher = Cipher.getInstance("AES"); // Obtener una instancia del cifrador AES
        cipher.init(Cipher.ENCRYPT_MODE, claveSecreta); // Inicializar el cifrador en modo de encriptación con la clave secreta
        byte[] textoEncriptado = cipher.doFinal(texto.getBytes("UTF-8")); // Encriptar el texto, obteniendo un arreglo de bytes
        String base64Encoded = Base64.getEncoder().encodeToString(textoEncriptado); // Codificar el arreglo de bytes en Base64 para convertirlo a una cadena

        return base64Encoded.replace("^", "~"); // Reemplazar '^' por '~' para evitar problemas con caracteres en el texto
    }

    // Método para desencriptar un texto encriptado usando una clave secreta
    public static String desencriptar(String textoEncriptado, SecretKey claveSecreta) throws Exception {
        String base64Decoded = textoEncriptado.replace("~", "^"); // Revertir la sustitución de caracteres antes de decodificar
        byte[] textoDesencriptado = Base64.getDecoder().decode(base64Decoded); // Decodificar el texto en Base64 a un arreglo de bytes
        Cipher cipher = Cipher.getInstance("AES"); // Obtener una instancia del cifrador AES
        cipher.init(Cipher.DECRYPT_MODE, claveSecreta); // Inicializar el cifrador en modo de desencriptación con la clave secreta
        return new String(cipher.doFinal(textoDesencriptado), "UTF-8"); // Desencriptar el texto y convertirlo de bytes a String
    }

    // Método para generar una nueva clave AES
    public static SecretKey keyGenerator() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES"); // Crear un generador de claves para AES
            keyGenerator.init(128); // Inicializar el generador con el tamaño de la clave (128, 192 o 256 bits)
            SecretKey claveSecreta = keyGenerator.generateKey(); // Generar y retornar la clave secreta
            return claveSecreta; // Retornar la clave generada
        } catch (Exception e) {
            e.printStackTrace(); // Manejar excepciones
        }
        return null; // Retornar null si hubo un error
    }

    // Método para convertir una clave secreta a String en formato Base64
    public static String toString(SecretKey secretKey) {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded()); // Codificar la clave secreta en Base64 y retornar la cadena resultante
    }

    // Método para convertir una cadena Base64 en un objeto SecretKey
    public static SecretKey toSecretKey(String secretKeyString) {
        byte[] decodedKey = Base64.getDecoder().decode(secretKeyString); // Decodificar la cadena Base64 a un arreglo de bytes
        SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES"); // Crear un nuevo objeto SecretKey a partir del arreglo de bytes
        return secretKey; // Retornar la clave secreta
    }
}
