package project.movie24.common;

import java.security.SecureRandom;

/**
 * 예매/구매 등에서 영화관 키오스크에 직접 입력할 코드를 만들 때 쓴다.
 * 헷갈리기 쉬운 0/O, 1/I/L을 뺀 32자 알파벳으로 "XXXX-XXXX-XXXX-XXXX" 형태를 생성한다.
 */
public final class RandomCodeGenerator {

    private static final String ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private RandomCodeGenerator() {
    }

    public static String generateGrouped(int groupCount, int groupLength) {
        StringBuilder sb = new StringBuilder();
        for (int g = 0; g < groupCount; g++) {
            if (g > 0) {
                sb.append('-');
            }
            for (int i = 0; i < groupLength; i++) {
                sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
            }
        }
        return sb.toString();
    }
}
