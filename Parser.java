import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Parser {

    // Blueprint for a Blog Post object
    static class BlogPost {
        String title;
        String fileName;
        String content;
        String date;
        String author;

        public BlogPost(String title, String fileName, String content, String date, String author) {
            this.title = title;
            this.fileName = fileName;
            this.content = content;
            this.date = date;
            this.author = author;
        }
    }

    public static void main(String[] args) {
        try {
            // 1. Setup paths
            File contentFolder = new File("Content");
            File postsFolder = new File("Content/Posts");
            List<BlogPost> blogPosts = new ArrayList<>();

            // 2. Scan the "Content/Posts" folder for blog articles
            if (postsFolder.exists() && postsFolder.isDirectory()) {
                File[] postFiles = postsFolder.listFiles((dir, name) -> name.endsWith(".txt"));
                if (postFiles != null) {
                    for (File file : postFiles) {
                        String nameWithoutExtension = file.getName().replace(".txt", "");
                        String rawContent = Files.readString(file.toPath());
                        
                        // Parse Front Matter Metadata
                        Map<String, String> metadata = new HashMap<>();
                        String cleanContent = parseFrontMatter(rawContent, metadata);

                        // Fallback values if metadata keys are missing in the file
                        String title = metadata.getOrDefault("title", capitalizeTitle(nameWithoutExtension.replace("-", " ")));
                        String date = metadata.getOrDefault("date", "2026-07-15");
                        String author = metadata.getOrDefault("author", "Unknown");

                        String htmlFileName = nameWithoutExtension + ".html";
                        blogPosts.add(new BlogPost(title, htmlFileName, cleanContent, date, author));
                    }
                }
            }

            // 3. Generate individual blog post HTML pages
            for (BlogPost post : blogPosts) {
                String htmlContent = generatePageHtml(post.title, post.content, post.date, post.author, "", true);
                Files.writeString(Paths.get(post.fileName), htmlContent);
                System.out.println("Generated Blog Post: " + post.fileName);
            }

            // 4. Generate standard Core Pages (About, Contact, & Online Projects)
            generateCorePage("about", "About Me", "2026-07-14", "Murungi Isaac");
            generateCorePage("contact", "Contact Me", "2026-07-14", "Murungi Isaac");
            generateCorePage("online-projects", "Online Projects", "2026-07-17", "Murungi Isaac");

            // 5. Build dynamic index feed links
            StringBuilder feedBuilder = new StringBuilder();
            for (BlogPost post : blogPosts) {
                feedBuilder.append("<li><span class='date'>")
                           .append(post.date)
                           .append("</span> - <a href='")
                           .append(post.fileName)
                           .append("'>")
                           .append(post.title)
                           .append("</a> <span class='author'>by ")
                           .append(post.author)
                           .append("</span></li>\n");
            }

            // 6. Generate the main Index Home Page with the feed embedded
            String indexIntro = Files.readString(Paths.get("Content/index.txt"));
            String indexHtml = generatePageHtml("Welcome to GitCMS", indexIntro, "2026-07-14", "Isaac", feedBuilder.toString(), false);
            Files.writeString(Paths.get("index.html"), indexHtml);
            System.out.println("Generated Index Page: index.html");

        } catch (Exception e) {
            System.out.println("Error running engine: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Front Matter Parsing Engine (Robust Version)
    private static String parseFrontMatter(String rawContent, Map<String, String> metadata) {
        rawContent = rawContent.trim();
        
        if (!rawContent.startsWith("---")) {
            return rawContent;
        }

        String[] lines = rawContent.split("\\r?\\n");
        StringBuilder bodyBuilder = new StringBuilder();
        boolean insideMetadata = false;
        int dashCount = 0;

        for (String line : lines) {
            String trimmedLine = line.trim();
            
            if (trimmedLine.equals("---")) {
                dashCount++;
                if (dashCount == 1) {
                    insideMetadata = true;
                    continue;
                } else if (dashCount == 2) {
                    insideMetadata = false;
                    continue;
                }
            }

            if (insideMetadata) {
                String[] parts = trimmedLine.split(":", 2);
                if (parts.length == 2) {
                    metadata.put(parts[0].trim().toLowerCase(), parts[1].trim());
                }
            } else if (dashCount >= 2) {
                bodyBuilder.append(line).append("\n");
            }
        }
        return bodyBuilder.toString().trim();
    }

    // Helper to generate a core text layout page
    private static void generateCorePage(String fileName, String title, String date, String author) throws IOException {
        Path textPath = Paths.get("Content/" + fileName + ".txt");
        if (Files.exists(textPath)) {
            String txtContent = Files.readString(textPath);
            String htmlOutput = generatePageHtml(title, txtContent, date, author, "", false);
            Files.writeString(Paths.get(fileName + ".html"), htmlOutput);
            System.out.println("Generated Core Page: " + fileName + ".html");
        }
    }

    // Central HTML Master Layout Template
    private static String generatePageHtml(String title, String bodyContent, String date, String author, String feedHtml, boolean isPost) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html lang='en'>\n<head>\n")
            .append("    <meta charset='UTF-8'>\n")
            .append("    <title>").append(title).append("</title>\n")
            .append("    <link rel='stylesheet' href='style.css'>\n")
            .append("</head>\n<body>\n")
            .append("<nav>\n    <div class='nav-container'>\n")
            .append("        <a href='about.html'>ABOUT</a>\n")
            .append("        <a href='online-projects.html'>ONLINE PROJECTS</a>\n")
            .append("        <a href='contact.html'>CONTACT</a>\n")
            .append("        <a href='index.html'>INDEX</a>\n")
            .append("    </div>\n</nav>\n")
            .append("<div class='main-wrapper'>\n")
            .append("    <div class='container'>\n")
            .append("        <h1>").append(title).append("</h1>\n")
            .append("        <p class='publish-date'><em>Published on: ").append(date).append(" | Author: ").append(author).append("</em></p>\n<hr><br>\n")
           .append("        <article>").append(bodyContent.replace("\n", "<br>\n")).append("</article>\n");

        if (feedHtml != null && !feedHtml.isEmpty()) {
            html.append("<br><h2>Recent Posts Feed</h2><br>\n")
                .append("<ul class='blog-feed'>\n").append(feedHtml).append("</ul>\n");
        }

        html.append("    </div>\n")
            .append("    <footer>&copy; 2026 GitCMS. Generated automatically using Java.</footer>\n")
            .append("</div>\n</body>\n</html>");
        return html.toString();
    }

    // Capitalizes clean titles nicely
    private static String capitalizeTitle(String str) {
        String[] words = str.split("\\s");
        StringBuilder capitalizeWord = new StringBuilder();
        for (String w : words) {
            if (w.length() > 0) {
                String first = w.substring(0, 1);
                String afterfirst = w.substring(1);
                capitalizeWord.append(first.toUpperCase()).append(afterfirst).append(" ");
            }
        }
        return capitalizeWord.toString().trim();
    }
}