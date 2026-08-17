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
        List<String> tags;

        public BlogPost(String title, String fileName, String content, String date, String author, List<String> tags) {
            this.title = title;
            this.fileName = fileName;
            this.content = content;
            this.date = date;
            this.author = author;
            this.tags = tags;
        }
    }

    private static final String SITE_URL = "https://savageisaac334-lab.github.io/GitCMS";

    public static void main(String[] args) {
        try {
            File postsFolder = new File("Content/Posts");
            List<BlogPost> blogPosts = new ArrayList<>();
            Set<String> allUniqueTags = new TreeSet<>();

            if (postsFolder.exists() && postsFolder.isDirectory()) {
                File[] postFiles = postsFolder.listFiles((dir, name) -> name.endsWith(".txt") || name.endsWith(".md"));
                if (postFiles != null) {
                    for (File file : postFiles) {
                        String nameWithoutExtension = file.getName().replace(".txt", "").replace(".md", "");
                        String rawContent = Files.readString(file.toPath());
                        
                        Map<String, String> metadata = new HashMap<>();
                        String cleanContent = parseFrontMatter(rawContent, metadata);

                        String title = metadata.getOrDefault("title", capitalizeTitle(nameWithoutExtension.replace("-", " ")));
                        String date = metadata.getOrDefault("date", "2026-07-15");
                        String author = metadata.getOrDefault("author", "Unknown");
                        
                        // Parse comma-separated tags
                        String rawTags = metadata.getOrDefault("tags", "general");
                        List<String> tags = new ArrayList<>();
                        for (String tag : rawTags.split(",")) {
                            String trimmedTag = tag.trim().toLowerCase();
                            if (!trimmedTag.isEmpty()) {
                                tags.add(trimmedTag);
                                allUniqueTags.add(trimmedTag);
                            }
                        }

                        String htmlFileName = nameWithoutExtension + ".html";
                        blogPosts.add(new BlogPost(title, htmlFileName, cleanContent, date, author, tags));
                    }
                }
            }

            // 1. Generate individual blog post HTML pages
            for (BlogPost post : blogPosts) {
                String htmlContent = generatePageHtml(post.title, post.content, post.date, post.author, "", "", true);
                Files.writeString(Paths.get(post.fileName), htmlContent);
                System.out.println("Generated Blog Post: " + post.fileName);
            }

            // 2. Generate standard Core Pages
            generateCorePage("about", "About Me", "2026-07-14", "Murungi Isaac");
            generateCorePage("contact", "Contact Me", "2026-07-14", "Murungi Isaac");
            generateCorePage("online-projects", "Online Projects", "2026-07-17", "Murungi Isaac");

            // 3. Build dynamic tag buttons HTML
            StringBuilder tagsHtmlBuilder = new StringBuilder("<div class='tag-buttons' style='margin-bottom: 15px;'><strong>Filter by Tag: </strong>");
            tagsHtmlBuilder.append("<button onclick='filterTag(\"all\")' style='margin-right: 5px; padding: 4px 8px; cursor: pointer;'>All</button>");
            for (String tag : allUniqueTags) {
                tagsHtmlBuilder.append("<button onclick='filterTag(\"").append(tag).append("\")' style='margin-right: 5px; padding: 4px 8px; cursor: pointer;'>#")
                               .append(tag).append("</button>");
            }
            tagsHtmlBuilder.append("</div>\n");

            // 4. Build dynamic index feed links with tag data attributes
            StringBuilder feedBuilder = new StringBuilder();
            for (BlogPost post : blogPosts) {
                String tagClasses = String.join(" ", post.tags);
                feedBuilder.append("<li class='post-item' data-tags='").append(tagClasses).append("'><span class='date'>")
                           .append(post.date)
                           .append("</span> - <a href='./")
                           .append(post.fileName)
                           .append("' class='post-title'>")
                           .append(post.title)
                           .append("</a> <span class='author'>by ")
                           .append(post.author)
                           .append("</span> <small style='color: #666;'>[")
                           .append(String.join(", ", post.tags))
                           .append("]</small></li>\n");
            }

            // 5. Generate Index Home Page
            String indexIntro = Files.exists(Paths.get("Content/index.txt")) 
                ? Files.readString(Paths.get("Content/index.txt")) 
                : "Welcome to GitCMS!";
            String indexHtml = generatePageHtml("Welcome to GitCMS", indexIntro, "2026-07-14", "Isaac", feedBuilder.toString(), tagsHtmlBuilder.toString(), false);
            Files.writeString(Paths.get("index.html"), indexHtml);
            System.out.println("Generated Index Page: index.html");

            // 6. Generate RSS Feed XML
            generateRssFeed(blogPosts);

        } catch (Exception e) {
            System.out.println("Error running engine: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String parseFrontMatter(String rawContent, Map<String, String> metadata) {
        rawContent = rawContent.trim();
        if (!rawContent.startsWith("---")) return rawContent;

        String[] lines = rawContent.split("\\r?\\n");
        StringBuilder bodyBuilder = new StringBuilder();
        boolean insideMetadata = false;
        int dashCount = 0;

        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.equals("---")) {
                dashCount++;
                if (dashCount == 1) { insideMetadata = true; continue; }
                else if (dashCount == 2) { insideMetadata = false; continue; }
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

    private static void generateCorePage(String fileName, String title, String date, String author) throws IOException {
        Path textPath = Paths.get("Content/" + fileName + ".txt");
        if (Files.exists(textPath)) {
            String txtContent = Files.readString(textPath);
            String htmlOutput = generatePageHtml(title, txtContent, date, author, "", "", false);
            Files.writeString(Paths.get(fileName + ".html"), htmlOutput);
            System.out.println("Generated Core Page: " + fileName + ".html");
        }
    }

    private static String convertMarkdownToHtml(String text) {
        if (text == null || text.isEmpty()) return "";
        StringBuilder parsed = new StringBuilder();
        String[] lines = text.split("\\r?\\n");

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("### ")) parsed.append("<h3>").append(trimmed.substring(4)).append("</h3>\n");
            else if (trimmed.startsWith("## ")) parsed.append("<h2>").append(trimmed.substring(3)).append("</h2>\n");
            else if (trimmed.startsWith("# ")) parsed.append("<h1>").append(trimmed.substring(2)).append("</h1>\n");
            else if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) parsed.append("<li>").append(trimmed.substring(2)).append("</li>\n");
            else parsed.append(line).append("<br>\n");
        }
        return parseInlineStyles(parsed.toString());
    }

    private static String parseInlineStyles(String text) {
        while (text.contains("**")) {
            text = text.replaceFirst("\\\\", "<strong>").replaceFirst("\\\\", "</strong>");
        }
        while (text.contains("*")) {
            text = text.replaceFirst("\\", "<em>").replaceFirst("\\", "</em>");
        }
        return text;
    }

    private static String generatePageHtml(String title, String bodyContent, String date, String author, String feedHtml, String tagsHtml, boolean isPost) {
        String formattedArticle = convertMarkdownToHtml(bodyContent);

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html lang='en'>\n<head>\n")
            .append("    <meta charset='UTF-8'>\n")
            .append("    <title>").append(title).append("</title>\n")
            .append("    <link rel='stylesheet' href='./style.css'>\n")
            .append("    <link rel='alternate' type='application/rss+xml' title='GitCMS RSS Feed' href='./rss.xml'>\n")
            .append("</head>\n<body>\n")
            .append("<nav>\n    <div class='nav-container'>\n")
            .append("        <a href='./about.html'>ABOUT</a>\n")
            .append("        <a href='./online-projects.html'>ONLINE PROJECTS</a>\n")
            .append("        <a href='./contact.html'>CONTACT</a>\n")
            .append("        <a href='./index.html'>INDEX</a>\n")
            .append("    </div>\n</nav>\n")
            .append("<div class='main-wrapper'>\n")
            .append("    <div class='container'>\n")
            .append("        <h1>").append(title).append("</h1>\n")
            .append("        <p class='publish-date'><em>Published on: ").append(date).append(" | Author: ").append(author).append("</em></p>\n<hr><br>\n")
            .append("        <article>").append(formattedArticle).append("</article>\n");

        if (feedHtml != null && !feedHtml.isEmpty()) {
            html.append("<br><h2>Recent Posts Feed</h2><br>\n")
                .append("        <input type='text' id='searchInput' onkeyup='filterPosts()' placeholder='Search posts...' style='width: 100%; padding: 8px; margin-bottom: 10px; border: 1px solid #ccc; border-radius: 4px;'>\n")
                .append(tagsHtml)
                .append("        <ul class='blog-feed' id='postList'>\n").append(feedHtml).append("        </ul>\n")
                .append("<script>\n")
                .append("let selectedTag = 'all';\n")
                .append("function filterTag(tag) {\n")
                .append("  selectedTag = tag;\n")
                .append("  filterPosts();\n")
                .append("}\n")
                .append("function filterPosts() {\n")
                .append("  let input = document.getElementById('searchInput').value.toLowerCase();\n")
                .append("  let posts = document.getElementsByClassName('post-item');\n")
                .append("  for (let i = 0; i < posts.length; i++) {\n")
                .append("    let text = posts[i].innerText.toLowerCase();\n")
                .append("    let tags = posts[i].getAttribute('data-tags').split(' ');\n")
                .append("    let matchesSearch = text.includes(input);\n")
                .append("    let matchesTag = (selectedTag === 'all') || tags.includes(selectedTag);\n")
                .append("    posts[i].style.display = (matchesSearch && matchesTag) ? '' : 'none';\n")
                .append("  }\n")
                .append("}\n")
                .append("</script>\n");
        }

        html.append("    </div>\n")
            .append("    <footer>&copy; 2026 GitCMS. Generated automatically using Java. | <a href='./rss.xml'>RSS Feed</a></footer>\n")
            .append("</div>\n</body>\n</html>");
        return html.toString();
    }

    private static void generateRssFeed(List<BlogPost> posts) throws IOException {
        StringBuilder rss = new StringBuilder();
        rss.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n")
           .append("<rss version=\"2.0\">\n")
           .append("<channel>\n")
           .append("  <title>GitCMS Blog Feed</title>\n")
           .append("  <link>").append(SITE_URL).append("/index.html</link>\n")
           .append("  <description>Automated blog feed generated by Java GitCMS engine.</description>\n")
           .append("  <language>en-us</language>\n");

        for (BlogPost post : posts) {
            rss.append("  <item>\n")
               .append("    <title>").append(post.title).append("</title>\n")
               .append("    <link>").append(SITE_URL).append("/").append(post.fileName).append("</link>\n")
               .append("    <description><![CDATA[").append(post.content).append("]]></description>\n")
               .append("    <author>").append(post.author).append("</author>\n")
               .append("    <pubDate>").append(post.date).append("</pubDate>\n")
               .append("  </item>\n");
        }

        rss.append("</channel>\n</rss>");
        Files.writeString(Paths.get("rss.xml"), rss.toString());
        System.out.println("Generated RSS Feed: rss.xml");
    }

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