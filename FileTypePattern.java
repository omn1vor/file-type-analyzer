package analyzer;

public class FileTypePattern {
    public int priority;
    byte[] pattern;
    long hash;
    String type;

    public FileTypePattern(String pattern, String type, int priority) {
        this.pattern = pattern.getBytes();
        this.type = type;
        this.priority = priority;
        this.hash = Hashing.hash(this.pattern);
    }

    public int getPriority() {
        return priority;
    }

    public byte[] getPattern() {
        return pattern;
    }

    public String getType() {
        return type;
    }

    public long getHash() {
        return hash;
    }

    public static FileTypePattern parse(String input) {
        String[] parts = input.split(";");
        try {
            return new FileTypePattern(parts[1].replaceAll("\"", ""),
                    parts[2].replaceAll("\"", ""),
                    Integer.parseInt(parts[0]));
        } catch (Exception e) {
            throw new RuntimeException("Wrong format of pattern file", e);
        }
    }
}
