package store.file;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 功能: 文件链接层
 * @author unknown100name
 * @date 2021.10.06
 */
public class FileStoreRepository {

    public static final FileStoreRepository repository = new FileStoreRepository();

    private FileStoreRepository() { }

    public static FileStoreRepository getInstance(){
        return repository;
    }

    /**
     * 文件池
     * FileName -> File
     */
    private final Map<String, File> files = new ConcurrentHashMap<>();

    /**
     * 获取文件 (如果没有则创建)
     * @param fileName 文件名
     * @param filePath 文件路径
     * @return 文件
     */
    public File getOrCreateFileByName(String fileName, String filePath){
        if (!files.containsKey(fileName)){
            // 防止写的时候没有带 "\\"
            if (!filePath.endsWith("\\")){
                filePath +=filePath + "\\";
            }
            files.put(fileName, new File(filePath + fileName));
        }
        return files.get(fileName);
    }

}
