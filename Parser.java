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
        File outputDir = new File(".");

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

                // Generate HTML with a premium, modern design layout
                writer.write("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
                writer.write("    <meta charset=\"UTF-8\">\n");
                writer.write("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
                writer.write("    <title>" + title + "</title>\n");
                writer.write("    <style>\n");
                writer.write("        * { box-sizing: border-box; margin: 0; padding: 0; }\n");
                writer.write("        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f0f2f5; color: #333; line-height: 1.6; padding-bottom: 60px; }\n");
                writer.write("        \n");
                writer.write("        /* Navigation Bar Styling */\n");
                writer.write("        nav { background: #1a1a1a; padding: 0 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.15); display: flex; justify-content: center; position: sticky; top: 0; z-index: 100; }\n");
                writer.write("        .nav-container { display: flex; width: 100%; max-width: 800px; justify-content: flex-start; align-items: center; height: 60px; }\n");
                writer.write("        nav a { color: #cccccc; margin-right: 30px; text-decoration: none; font-weight: 600; font-size: 1rem; text-transform: capitalize; transition: color 0.3s ease, border-bottom 0.3s ease; padding: 18px 0; border-bottom: 3px solid transparent; }\n");
                writer.write("        nav a:hover { color: #ffffff; border-bottom: 3px solid #00bcd4; }\n");
                writer.write("        \n");
                writer.write("        /* Active link indicator simple match */\n");
                writer.write("        nav a.active { color: #ffffff; border-bottom: 3px solid #00bcd4; }\n");
                writer.write("        \n");
                writer.write("        /* Main Page Container */\n");
                writer.write("        .main-wrapper { display: flex; justify-content: center; padding: 40px 20px; }\n");
                writer.write("        .container { background: #ffffff; width: 100%; max-width: 800px; padding: 40px; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.05); border-top: 5px solid #00bcd4; }\n");
                writer.write("        \n");
                writer.write("        /* Typography */\n");
                writer.write("        h1 { color: #1a1a1a; font-size: 2.2rem; margin-bottom: 20px; font-weight: 700; border-bottom: 1px solid #eaeaea; padding-bottom: 15px; }\n");
                writer.write("        p { color: #555555; font-size: 1.1rem; line-height: 1.8; }\n");
                writer.write("        footer {text-align: center; padding: 20px; color: #888888; font-size: 0.9; margin-top: 20px; border-top: 1px solid #eaeaea; }\n");
                writer.write("    </style>\n</head>\n<body>\n");

                // Generate Navigation Bar dynamically
                writer.write("    <nav>\n");
                writer.write("        <div class=\"nav-container\">\n");
                for (String p : pageNames) {
                    // Check if current page is the active page to apply styles
                    String activeClass = p.equals(pageName) ? " class=\"active\"" : "";
                    writer.write("            <a href=\"" + p + ".html\"" + activeClass + ">" + p + "</a>\n");
                }
                writer.write("        </div>\n");
                writer.write("    </nav>\n");

                // Page Content wrapped in a beautiful card
                writer.write("    <div class=\"main-wrapper\">\n");
                writer.write("        <div class=\"container\">\n");
                writer.write("            <h1>" + title + "</h1>\n");
                writer.write("            <p>" + content + "</p>\n");
                writer.write("        </div>\n");
                writer.write("    </div>\n");

                //Footer
                writer.write("    <footer>\n");
                writer.write("         &#169; 2026 GitCMS. Generated automatically using Java.\n");
                writer.write("     <footer>\n");

                writer.write("</body>\n</html>");

                System.out.println("Successfully generated beautifully styled: " + outputFile.getName());

            } catch (IOException e) {
                System.out.println("Error processing file: " + file.getName());
                e.printStackTrace();
            }
        }
    }
}