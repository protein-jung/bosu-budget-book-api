package com.bosu.housebook.imports;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bosu.housebook.common.ApiException;
import com.bosu.housebook.user.User;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class OfficeFileDecryptorTest {

    private final OfficeFileDecryptor decryptor = new OfficeFileDecryptor();

    @Test
    void decryptsWithEightDigitBirthDatePassword() throws Exception {
        User uploader = new User("test@example.com", "password", "테스트", LocalDate.of(1999, 1, 5));
        byte[] encrypted = encrypt(buildPlainXlsx(), "19990105");

        byte[] decrypted = decryptor.decryptIfNeeded(encrypted, uploader);

        assertThat(readFirstCellValue(decrypted)).isEqualTo("hello");
    }

    @Test
    void decryptsWithSixDigitBirthDatePassword() throws Exception {
        User uploader = new User("test@example.com", "password", "테스트", LocalDate.of(1999, 1, 5));
        byte[] encrypted = encrypt(buildPlainXlsx(), "990105");

        byte[] decrypted = decryptor.decryptIfNeeded(encrypted, uploader);

        assertThat(readFirstCellValue(decrypted)).isEqualTo("hello");
    }

    @Test
    void rejectsWrongPassword() throws Exception {
        User uploader = new User("test@example.com", "password", "테스트", LocalDate.of(1999, 1, 5));
        byte[] encrypted = encrypt(buildPlainXlsx(), "20000101");

        assertThatThrownBy(() -> decryptor.decryptIfNeeded(encrypted, uploader))
                .isInstanceOf(ApiException.class);
    }

    private byte[] buildPlainXlsx() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            workbook.createSheet("시트1").createRow(0).createCell(0).setCellValue("hello");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private byte[] encrypt(byte[] plainXlsx, String password) throws Exception {
        try (POIFSFileSystem fs = new POIFSFileSystem()) {
            EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile);
            Encryptor encryptor = info.getEncryptor();
            encryptor.confirmPassword(password);
            try (OPCPackage opc = OPCPackage.open(new ByteArrayInputStream(plainXlsx));
                    OutputStream os = encryptor.getDataStream(fs)) {
                opc.save(os);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            fs.writeFilesystem(out);
            return out.toByteArray();
        }
    }

    private String readFirstCellValue(byte[] xlsx) throws Exception {
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(xlsx))) {
            return workbook.getSheetAt(0).getRow(0).getCell(0).getStringCellValue();
        }
    }
}
