package analyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class Analyzer {
    List<FileTypePattern> patterns;

    public Analyzer(String patternsFileName) {
        loadPatterns(patternsFileName);
    }

    public void analyzeFiles(String dirName) throws IOException {
        File dir = new File(dirName);
        if (!dir.isDirectory()) {
            System.out.println("No such directory: " + dirName);
            return;
        }
        File[] filesArray = dir.listFiles(File::isFile);
        if (filesArray == null) {
            return;
        }
        List<File> files = Arrays.asList(filesArray);
        ExecutorService executor = Executors.newFixedThreadPool(files.size());

        List<Callable<String>> tasks = new ArrayList<>();

        for (File file : files) {
            byte[] data = Files.readAllBytes(file.toPath());
            tasks.add(() -> String.format("%s: %s", file.getName(), analyze(data)));
        }

        try {
            executor.invokeAll(tasks).forEach(future -> {
                try {
                    System.out.println(future.get(5, TimeUnit.SECONDS));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        executor.shutdown();
    }

    private String analyze(byte[] data) {
        for (FileTypePattern pattern : patterns) {
            if (findRabinKarp(data, pattern)) {
                return pattern.getType();
            }
        }
        return "Unknown file type";
    }

    @SuppressWarnings("unused")
    public boolean findKMP(byte[] data, byte[] pattern) {
        byte[] prefix = prefixFunction(pattern);

        int unmatchedIndex = 0;
        int start = 0;

        while (start + pattern.length <= data.length) {
            boolean isMatch = true;
            for (int i = unmatchedIndex; i < pattern.length; i++) {
                if (pattern[i] != data[start + i]) {
                    isMatch = false;
                    start += i == 0 ? 1 : (i - prefix[i - 1]);
                    unmatchedIndex = i == 0 ? 0 : prefix[i - 1];
                    break;
                }
            }
            if (isMatch) {
                return true;
            }
        }
        return false;
    }

    private byte[] prefixFunction(byte[] data) {
        byte[] prefix = new byte[data.length];
        prefix[0] = 0;
        for (int i = 1; i < prefix.length; i++) {
            int shift = prefix[i - 1];
            while (true) {
                if (data[i] == data[shift]) {
                    prefix[i] = (byte) (shift + 1);
                    break;
                } else {
                    if (shift == 0) {
                        break;
                    }
                }
                shift = prefix[shift - 1];
            }
        }
        return prefix;
    }

    private void loadPatterns(String patternsFileName) {
        patterns = new ArrayList<>();
        List<String> lines;

        try {
            lines = Files.readAllLines(Path.of(patternsFileName));
            lines.forEach(line -> patterns.add(FileTypePattern.parse(line)));
        } catch (Exception e) {
            throw new RuntimeException("Could not load patterns", e);
        }
        patterns.sort(Comparator.comparingInt(FileTypePattern::getPriority).reversed());
    }

    private boolean findRabinKarp(byte[] data, FileTypePattern patternObj) {
        byte[] pattern = patternObj.getPattern();
        if (pattern.length > data.length) {
            return false;
        }

        int len = pattern.length;
        int currentHash = Hashing.hash(Arrays.copyOfRange(data, 0, len));
        for (int i = 0; i <= data.length - len; i++) {
            if (i > 0) {
                currentHash = Hashing.rollingHash(currentHash, data[i - 1], data[i + len - 1], len);
            }
            if (patternObj.getHash() != currentHash) {
                continue;
            }
            boolean isMatch = true;
            for (int j = 0; j < len; j++) {
                if (pattern[j] != data[i + j]) {
                    isMatch = false;
                    break;
                }
            }
            if (isMatch) {
                return true;
            }
        }
        return false;
    }
}
