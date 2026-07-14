import java.io.*;
import java.util.*;

public class Parser {
    public static void main(String[] args) {
        // Define paths for input and output folders
        File contentDir = new File("Content");
        File outputDir = new File("Output");

        // Create Output directory if it doesn't exist
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }

        // 1. Scan Content folder for all .txt files
        File[] files = contentDir.listFiles((dir, name) -> name.endsWith(".txt"));
        if (files == null || files.length == 0) {
            System.out.println("No content files found in 'Content' folder!");
            return;
        }

        // Create lists to hold parsed pages and blog posts
        List<BlogPost> posts = new ArrayList<>();
        List<String> pageNames = new ArrayList<>();

        // 2. Parse date, title, and content from each text file
        for (File file : files) {
            String pageName = file.getName().substring(0, file.getName().lastIndexOf("."));
            pageNames.add(pageName);

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                // Line 1: Date (e.g., 2026-07-14)
                String date = reader.readLine();
                
                // Line 2: Title
                String title = reader.readLine();
                
                // The rest: Body content
                StringBuilder contentBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    contentBuilder.append(line).append("\n");
                }
                String content = contentBuilder.toString();

                // Store this post in our list
                posts.add(new BlogPost(title, date, pageName, content));

            } catch (IOException e) {
                System.out.println("Error reading file: " + file.getName());
            }
        }

        // Sort posts automatically by date (newest first)
        Collections.sort(posts);

        // 3. Generate structured HTML files for each parsed post
        for (BlogPost post : posts) {
            File outputFile = new File(outputDir, post.filename + ".html");

            try (FileWriter writer = new FileWriter(outputFile)) {
                // Start HTML Document
                writer.write("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
                writer.write("    <meta charset=\"UTF-8\">\n");
                writer.write("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n");
                writer.write("    <title>" + post.title + "</title>\n");
                writer.write("    <link rel='stylesheet' href='style.css'>\n");
                writer.write("</head>\n<body>\n");

                // Generate Dynamic Navigation Header
                writer.write("<nav>\n");
                writer.write("    <div class='nav-container'>\n");
                for (String page : pageNames) {
                    String activeClass = page.equals(post.filename) ? "class='active'" : "";
                    writer.write("        <a href='" + page + ".html' " + activeClass + ">" + page.toUpperCase() + "</a>\n");
                }
                writer.write("    </div>\n");
                writer.write("</nav>\n<hr>\n");

                // Main Content Area
                writer.write("<main class='container'>\n");
                writer.write("    <h1>" + post.title + "</h1>\n");
                writer.write("    <p style='color: gray; font-style: italic;'>Published on: " + post.date + "</p>\n");
                writer.write("    <article class='main-wrapper'>" + post.content.replace("\n", "<br>") + "</article>\n");

                // If this is the homepage (index.html), embed the feed list!
                if (post.filename.equals("index")) {
                    writer.write("    <hr>\n    <h2>Recent Posts Feed</h2>\n");
                    writer.write("    <ul class='blog-feed' style='list-style-type: none; padding: 0;'>\n");
                    for (BlogPost p : posts) {
                        // Keep the homepage out of its own content links list
                        if (!p.filename.equals("index")) {
                            writer.write("        <li style='margin-bottom: 10px;'>\n");
                            writer.write("            <span style='color: gray; margin-right: 15px; font-family: monospace;'>" + p.date + "</span>\n");
                            writer.write("            <a href='" + p.filename + ".html'><strong>" + p.title + "</strong></a>\n");
                            writer.write("        </li>\n");
                        }
                    }
                    writer.write("    </ul>\n");
                }

                writer.write("</main>\n");

                // Footer Section
                writer.write("<hr>\n<footer>\n");
                writer.write("    <p>&copy; 2026 GitCMS. Generated Dynamically with Java.</p>\n");
                writer.write("</footer>\n");
                
                // Close HTML Document
                writer.write("</body>\n</html>");
                
                System.out.println("Generated: " + outputFile.getName());

            } catch (IOException e) {
                System.out.println("Error writing file: " + outputFile.getName());
            }
        }
    }

    // Helper class to store and sort blog posts
    static class BlogPost implements Comparable<BlogPost> {
        String title;
        String date; // Format: YYYY-MM-DD
        String filename;
        String content;

        public BlogPost(String title, String date, String filename, String content) {
            this.title = title;
            this.date = date;
            this.filename = filename;
            this.content = content;
        }

        // Compares dates to sort newest posts first
        @Override
        public int compareTo(BlogPost other) {
            return other.date.compareTo(this.date); 
        }
    }
}