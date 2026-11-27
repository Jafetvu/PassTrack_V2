package com.passtrack.api.service;

import com.passtrack.api.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void storeFile_PdfValido_GuardaConUuidYRetornaNombre() {
        FileStorageService storage = new FileStorageService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile(
            "archivo", "receta.pdf", "application/pdf", "%PDF-1.4 contenido".getBytes()
        );

        String stored = storage.storeFile(file);

        assertThat(stored).endsWith(".pdf");
        assertThat(Files.exists(tempDir.resolve(stored))).isTrue();

        Resource resource = storage.loadAsResource(stored);
        assertThat(resource.exists()).isTrue();
        assertThat(storage.detectMediaType(stored)).isEqualTo("application/pdf");
    }

    @Test
    void storeFile_Excede2MB_LanzaBadRequest() {
        FileStorageService storage = new FileStorageService(tempDir.toString());
        byte[] oversized = new byte[(int) FileStorageService.MAX_FILE_SIZE_BYTES + 1];
        MockMultipartFile file = new MockMultipartFile("archivo", "grande.pdf", "application/pdf", oversized);

        assertThatThrownBy(() -> storage.storeFile(file))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    void storeFile_MimeInvalido_LanzaBadRequest() {
        FileStorageService storage = new FileStorageService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile(
            "archivo", "script.exe", "application/octet-stream", "MZ".getBytes()
        );

        assertThatThrownBy(() -> storage.storeFile(file))
            .isInstanceOf(BadRequestException.class);
    }
}
