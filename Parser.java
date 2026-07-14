import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Parser {
    public static void main(String[] args) {
        // Define paths for our CMS directories
        File contentDir = new File("content");
        File outputDir = new File("output");

        // Make sure the output folder exists
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }

        // Check if the content directory exists and look for files
        if (contentDir.exists() && contentDir.isDirectory()) {
            File[] files = contentDir.listFiles((dir, name) -> name.endsWith(".txt"));

            if (files == null || files.length == 0) {
                System.out.println("No content files found! Please add a .txt file inside the 'content' folder.");
                return;
            }

            for (File file : files) {
                parseFile(file, outputDir);
            }
        } else {
            System.out.println("The 'content' folder is missing. Please create it.");
        }
    }

    private static void parseFile(File inputFile, File outputDir) {
        String fileName = inputFile.getName().replace(".txt", ".html");
        File outputFile = new File(outputDir, fileName);

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            String title = reader.readLine(); // The very first line of our text file will be the title
            if (title == null) title = "Untitled Page";

            // Write the HTML and basic CSS styling to the output file
            writer.write("<!DOCTYPE html>\n<html>\n<head>\n");
            writer.write("<title>" + title + "</title>\n");
            writer.write("<style>\n");
            writer.write("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; padding: 40px; max-width: 800px; margin: auto; background-color: #f4f7f6; color: #333; }\n");
            writer.write("h1 { color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 10px; }\n");
            writer.write("p { font-size: 1.1em; color: #555; }\n");
            writer.write("footer { margin-top: 40px; padding-top: 20px; border-top: 1px solid #ccc; font-size: 0.9em; color: #777; }\n");
            writer.write("</style>\n</head>\n<body>\n");

            // Add the main title heading
            writer.write("<h1>" + title + "</h1>\n");

            // Read the remaining lines as paragraph text
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    writer.write("<p>" + line + "</p>\n");
                }
            }

            // Close the HTML tags
            writer.write("<footer>Generated automatically by GitCMS</footer>\n");
            writer.write("</body>\n</html>");

            System.out.println("Successfully generated: " + outputFile.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("Error processing file " + inputFile.getName() + ": " + e.getMessage());
        }
    }
}