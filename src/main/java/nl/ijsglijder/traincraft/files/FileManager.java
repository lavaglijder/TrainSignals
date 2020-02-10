package nl.ijsglijder.traincraft.files;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileManager {

    HashMap<String, TrainFile> trainFileList;

    public FileManager() {
        this.trainFileList = new HashMap<>();
    }

    public TrainFile addFile(TrainFile file) {
        trainFileList.put(file.getPath(), file);
        return file;
    }

    public HashMap<String, TrainFile> getTrainFileList() {
        return trainFileList;
    }

    public TrainFile getFile(String path) {
        return trainFileList.get(path);
    }

    public void saveAll() {
        trainFileList.forEach((s, file) -> file.save());
    }
}
