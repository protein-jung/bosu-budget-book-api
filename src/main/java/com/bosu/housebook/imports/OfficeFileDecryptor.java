package com.bosu.housebook.imports;

import com.bosu.housebook.common.ApiException;
import com.bosu.housebook.user.User;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.stereotype.Component;

/**
 * 경기지역화폐 등 일부 명세서는 생년월일(YYYYMMDD)을 비밀번호로 건 암호화된 엑셀로 온다.
 * 파일이 실제로 암호화된 OLE2 래퍼(EncryptedPackage)일 때만 마이페이지에 저장된 생년월일로
 * 복호화를 시도하고, 그렇지 않은(평문 xlsx/전통 xls) 파일은 그대로 통과시킨다.
 */
@Component
public class OfficeFileDecryptor {

    private static final DateTimeFormatter PASSWORD_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public byte[] decryptIfNeeded(byte[] bytes, User uploader) {
        if (FileMagic.valueOf(bytes) != FileMagic.OLE2) {
            return bytes;
        }

        try (POIFSFileSystem fs = new POIFSFileSystem(new ByteArrayInputStream(bytes))) {
            if (!fs.getRoot().hasEntry("EncryptedPackage")) {
                return bytes;
            }

            LocalDate birthDate = uploader.getBirthDate();
            if (birthDate == null) {
                throw ApiException.badRequest(
                        "암호가 걸린 파일이에요. 마이페이지에서 생년월일을 먼저 등록해주세요.");
            }
            String password = birthDate.format(PASSWORD_FORMAT);

            EncryptionInfo info = new EncryptionInfo(fs);
            Decryptor decryptor = Decryptor.getInstance(info);
            if (!decryptor.verifyPassword(password)) {
                throw ApiException.badRequest("파일 비밀번호가 마이페이지에 등록된 생년월일과 일치하지 않아요.");
            }

            try (InputStream decrypted = decryptor.getDataStream(fs)) {
                return decrypted.readAllBytes();
            }
        } catch (IOException | GeneralSecurityException e) {
            throw ApiException.badRequest("암호화된 파일을 열 수 없습니다.");
        }
    }
}
