package top.flobby.admin.generator.writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 代码文件写入器
 */
public class CodeWriter {

    private static final Logger log = LoggerFactory.getLogger(CodeWriter.class);

    /**
     * 项目根目录
     */
    private final String projectRoot;

    /**
     * 是否覆盖已存在的文件
     */
    private boolean overwrite = false;

    /**
     * 是否为预览模式（不实际写入文件）
     */
    private boolean previewMode = false;

    /**
     * 已生成的文件列表
     */
    private final List<String> generatedFiles = new ArrayList<>();

    /**
     * 跳过的文件列表（已存在且不覆盖）
     */
    private final List<String> skippedFiles = new ArrayList<>();

    public CodeWriter(String projectRoot) {
        this.projectRoot = projectRoot;
    }

    /**
     * 写入文件
     *
     * @param relativePath 相对于项目根目录的路径
     * @param content      文件内容
     * @return 是否成功写入
     */
    public boolean write(String relativePath, String content) {
        Path filePath = Paths.get(projectRoot, relativePath);
        File file = filePath.toFile();

        // 检查文件是否存在
        if (file.exists() && !overwrite) {
            log.warn("文件已存在，跳过: {}", relativePath);
            skippedFiles.add(relativePath);
            return false;
        }

        // 预览模式不实际写入
        if (previewMode) {
            log.info("[预览] 将生成文件: {}", relativePath);
            generatedFiles.add(relativePath);
            return true;
        }

        try {
            // 创建父目录
            File parentDir = file.getParentFile();
            if (!parentDir.exists()) {
                boolean created = parentDir.mkdirs();
                if (!created) {
                    log.error("创建目录失败: {}", parentDir.getAbsolutePath());
                    return false;
                }
            }

            // 写入文件
            Files.writeString(filePath, content, StandardCharsets.UTF_8);
            log.info("生成文件: {}", relativePath);
            generatedFiles.add(relativePath);
            return true;
        } catch (IOException e) {
            log.error("写入文件失败: {} - {}", relativePath, e.getMessage());
            return false;
        }
    }

    /**
     * 检查文件是否存在
     *
     * @param relativePath 相对路径
     * @return 是否存在
     */
    public boolean exists(String relativePath) {
        Path filePath = Paths.get(projectRoot, relativePath);
        return Files.exists(filePath);
    }

    /**
     * 获取绝对路径
     *
     * @param relativePath 相对路径
     * @return 绝对路径
     */
    public String getAbsolutePath(String relativePath) {
        return Paths.get(projectRoot, relativePath).toAbsolutePath().toString();
    }

    /**
     * 设置是否覆盖已存在的文件
     */
    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    /**
     * 设置是否为预览模式
     */
    public void setPreviewMode(boolean previewMode) {
        this.previewMode = previewMode;
    }

    /**
     * 获取已生成的文件列表
     */
    public List<String> getGeneratedFiles() {
        return new ArrayList<>(generatedFiles);
    }

    /**
     * 获取跳过的文件列表
     */
    public List<String> getSkippedFiles() {
        return new ArrayList<>(skippedFiles);
    }

    /**
     * 重置统计
     */
    public void reset() {
        generatedFiles.clear();
        skippedFiles.clear();
    }

    /**
     * 打印生成报告
     */
    public void printReport() {
        log.info("========== 代码生成报告 ==========");
        log.info("生成文件数: {}", generatedFiles.size());
        log.info("跳过文件数: {}", skippedFiles.size());

        if (!generatedFiles.isEmpty()) {
            log.info("--- 已生成文件 ---");
            for (String file : generatedFiles) {
                log.info("  + {}", file);
            }
        }

        if (!skippedFiles.isEmpty()) {
            log.info("--- 跳过文件（已存在）---");
            for (String file : skippedFiles) {
                log.info("  - {}", file);
            }
        }
        log.info("==================================");
    }

    public String getProjectRoot() {
        return projectRoot;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public boolean isPreviewMode() {
        return previewMode;
    }
}
