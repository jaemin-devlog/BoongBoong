package org.hanseo.boongboong.domain.upload.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/upload")
public class UploadController {

    private static final Path ROOT = Paths.get("uploads");
    private static final Path VEHICLE_DIR = ROOT.resolve("vehicle");
    private static final Set<String> ALLOWED = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            MediaType.IMAGE_GIF_VALUE
    );

    @PostMapping(path = "/vehicle-image")
    public ResponseEntity<UploadRes> uploadVehicleImage(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        String ct = file.getContentType();
        if (ct == null || !ALLOWED.contains(ct)) {
            return ResponseEntity.status(415).build(); // Unsupported Media Type
        }

        // Ensure directories
        if (Files.notExists(VEHICLE_DIR)) {
            Files.createDirectories(VEHICLE_DIR);
        }

        // Generate filename with original extension
        String ext = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + (ext.isBlank() ? "" : ("." + ext));
        Path dest = VEHICLE_DIR.resolve(filename);

        // Save file
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

        // Public URL via WebConfig mapping
        String url = "/files/vehicle/" + filename;
        return ResponseEntity.ok(new UploadRes(url));
    }

    private String getExtension(String original) {
        if (original == null) return "";
        String name = StringUtils.getFilename(original);
        if (name == null) return "";
        int i = name.lastIndexOf('.');
        if (i < 0 || i == name.length() - 1) return "";
        return name.substring(i + 1).toLowerCase();
    }

    public record UploadRes(String url) {}
}
