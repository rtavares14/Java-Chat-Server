package shared.messages.file_transfer.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.nio.file.Paths;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FileTransfer(String sender, String fileName, Double size, String checkSum, String uuid) {

    @JsonIgnore
    public String getFilePath() {
        return fileName;
    }

    @JsonIgnore
    public String getFileNameFromPath() {
        String filePath = getFilePath();
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        return Paths.get(filePath).getFileName().toString();
    }
}
