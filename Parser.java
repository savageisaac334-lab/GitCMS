import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    public static void main(String[] args) {
        File contentDir = new File("Content");
        File outputDir = new File("Output");

        // Create Output directory if it doesn't exist
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }

        // 1. Scan Content folder for all .txt files to build the navigation links
        File[] files = contentDir.listFiles((dir, name) -> name.endsWith(".txt"));
        if (files == null || files.length == 0) {
            System.out.println("No content files found in 'Content' folder!");
            return;
        }

        List<String> pageNames = new ArrayList<>();
        for (File file : files) {
            // Strip the .txt extension to get the page name (e.g., "index", "about")
            String nameWithoutExtension = file.getName().substring(0, file.getName().lastIndexOf('.'));
            pageNames.add(nameWithoutExtension);
        }

        // 2. Generate an HTML file for each text file
        for (File file : files) {
            String pageName = file.getName().substring(0, file.getName().lastIndexOf('.'));
            File outputFile = new File(outputDir, pageName + ".html");

            try (BufferedReader reader = new BufferedReader(new FileReader(file));
                 FileWriter writer = new FileWriter(outputFile)) {

                String title = reader.readLine(); // First line is the Title
                String content = reader.readLine(); // Second line is the body Content

                // Generate HTML with a shared navigation bar
                writer.write("<!DOCTYPE html>\n<html>\n<head>\n");
                writer.write("    <title>" + title + "</title>\n");
                writer.write("    <style>\n");
                writer.write("        body { font-family: Arial, sans-serif; margin: 40px; line-height: 1.6; background-color: #f4f4f9; color: #333; }\n");
                writer.write("        nav { background: #333; padding: 15px; border-radius: 5px; margin-bottom: 20px; }\n");
                writer.write("        nav a { color: white; margin-right: 20px; text-decoration: none; font-weight: bold; text-transform: capitalize; }\n");
                writer.write("        nav a:hover { text-decoration: underline; }\n");
                writer.write("        .container { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }\n");
                writer.write("        h1 { color: #111; }\n");
                writer.write("    </style>\n</head>\n<body>\n");

                // Generate Navigation Bar dynamically based on all pages found
                writer.write("    <nav>\n");
                for (String p : pageNames) {
                    writer.write("        <a href=\"" + p + ".html\">" + p + "</a>\n");
                }
                writer.write("    </nav>\n");

                // Page Content
                writer.write("    <div class=\"container\">\n");
                writer.write("        <h1>" + title + "</h1>\n");
                writer.write("        <p>" + content + "</p>\n");
                writer.write("    </div>\n");

                writer.write("</body>\n</html>");

                System.out.println("Successfully generated: " + outputFile.getName());

            } catch (IOException e) {
                System.out.println("Error processing file: " + file.getName());
                e.printStackTrace();
            }
        }
    }
}