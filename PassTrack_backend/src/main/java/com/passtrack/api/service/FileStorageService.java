package com.passtrack.api.service;

import com.passtrack.api.exception.BadRequestException;
import com.passtrack.api.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Service
public class FileStorageService {

    public static final long MAX_FILE_SIZE_BYTES = 2L * 1024L * 1024L;

    private static final Map<String, String> ALLOWED_MIME_EXTENSIONS = Map.of(
        "application/pdf", ".pdf",
        "image/jpeg", ".jpg",
        "image/png", ".png"
    );

    private final Path rootLocation;

    public FileStorageService(
            @Value("${passtrack.storage.upload-dir:/app/uploads}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Debe adjuntar un archivo de evidencia válido");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("El archivo excede el tamaño máximo permitido de 2 MB");
        }

        String contentType = file.getContentType();
        String extension = contentType == null ? null : ALLOWED_MIME_EXTENSIONS.get(contentType.toLowerCase());
        if (extension == null) {
            throw new BadRequestException("Formato de archivo no permitido. Solo se aceptan PDF, JPEG o PNG");
        }

        String safeFilename = UUID.randomUUID() + extension;
        try {
            Files.createDirectories(rootLocation);
            Path destination = rootLocation.resolve(safeFilename).normalize();
            if (!destination.startsWith(rootLocation)) {
                throw new BadRequestException("Ruta de almacenamiento inválida");
            }
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
            }
            return safeFilename;
        } catch (IOException ex) {
            throw new IllegalStateException("No fue posible almacenar la evidencia en disco", ex);
        }
    }

    public Resource loadAsResource(String storedFilename) {
        if (storedFilename == null || storedFilename.isBlank()
                || storedFilename.contains("..")
                || storedFilename.contains("/")
                || storedFilename.contains("\\")) {
            throw new BadRequestException("Nombre de archivo inválido");
        }

        try {
            Path filePath = rootLocation.resolve(storedFilename).normalize();
            if (!filePath.startsWith(rootLocation)) {
                throw new BadRequestException("Acceso denegado a la ruta solicitada");
            }
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("Archivo de evidencia no encontrado");
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("Archivo de evidencia no encontrado");
        }
    }

    public String detectMediaType(String storedFilename) {
        if (storedFilename == null) {
            return "application/octet-stream";
        }
        String lower = storedFilename.toLowerCase();
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        return "application/octet-stream";
    }
}
