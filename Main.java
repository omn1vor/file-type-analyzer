package analyzer;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Expecting 2 arguments: " +
                    "directory name (relative path), pattern file name");
        }
        Analyzer analyzer = new Analyzer(args[1]);
        try {
            analyzer.analyzeFiles(args[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
