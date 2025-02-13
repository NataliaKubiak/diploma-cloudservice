package org.example.diplomacloudservice;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    private static String storagePath = "./storage";

    public static void main(String[] args) throws IOException {
        uploadFile("test_file.txt");
    }

    public static String uploadFile(String filename) throws IOException {
        int userId = 3;

        //создаем папку пользователя
        Path userDir = Paths.get(storagePath, "user_" + userId); //создаем объект Path
        Files.createDirectories(userDir); //создаем все папки если их нет, если есть - ничего не произойдет?

        //сохраняем файл
        Path filePath2 = userDir.resolve(filename);
//        multipartFile.transferTo(filePath.toFile());

        return null;
    }
}
