import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    public static class BlogPost {
        private String title;
        private String date;
        private String author;
        private String content;
        private String fileName;
        private String excerpt;
        private int readingTimeMinutes;
        private List<String> tags = new ArrayList<>();

        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }

        public String getExcerpt() { return excerpt; }
        public void setExcerpt(String excerpt) { this.excerpt = excerpt; }

        public int getReadingTimeMinutes() { return readingTimeMinutes; }
        public void setReadingTimeMinutes(int readingTimeMinutes) { this.readingTimeMinutes = readingTimeMinutes; }

        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }
    }

    public static BlogPost parsePost(File file) throws IOException {
        BlogPost post = new BlogPost();
        post.setFileName(file.getName().replace(".txt", ".html"));

        List<String> lines = Files.readAllLines(file.toPath());
        boolean insideFrontMatter = false;
        StringBuilder contentBuilder = new StringBuilder();

        for (String line : lines) {
            if (line.trim().equals("---")) {
                insideFrontMatter = !insideFrontMatter;
                continue;
            }

            if (insideFrontMatter) {
                String lower = line.toLowerCase();
                if (lower.startsWith("title:")) {
                    post.setTitle(line.substring(line.indexOf(":") + 1).trim());
                } else if (lower.startsWith("date:")) {
                    post.setDate(line.substring(line.indexOf(":") + 1).trim());
                } else if (lower.startsWith("author:")) {
                    post.setAuthor(line.substring(line.indexOf(":") + 1).trim());
                } else if (lower.startsWith("tag:") || lower.startsWith("tags:")) {
                    String tagString = line.substring(line.indexOf(":") + 1).trim();
                    List<String> parsedTags = new ArrayList<>();
                    for (String t : tagString.split(",")) {
                        if (!t.trim().isEmpty()) {
                            parsedTags.add(t.trim());
                        }
                    }
                    post.setTags(parsedTags);
                }
            } else {
                contentBuilder.append(line).append("\n");
            }
        }

        String fullContent = contentBuilder.toString().trim();
        post.setContent(fullContent);

        // Excerpt calculation
        String[] words = fullContent.split("\\s+");
        StringBuilder excerptBuilder = new StringBuilder();
        int excerptWordCount = Math.min(words.length, 30);
        for (int i = 0; i < excerptWordCount; i++) {
            excerptBuilder.append(words[i]).append(" ");
        }
        post.setExcerpt(excerptBuilder.toString().trim() + "...");

        // Reading time calculation (average 200 words per minute)
        int calculatedTime = (int) Math.ceil((double) words.length / 200);
        post.setReadingTimeMinutes(Math.max(1, calculatedTime));

        return post;
    }

    public static String generateHtml(BlogPost post) {
        StringBuilder tagsBuilder = new StringBuilder("<div class=\"tags\">");
        for (String tag : post.getTags()) {
            tagsBuilder.append("<span class=\"tag-badge\">#").append(tag).append("</span> ");
        }
        tagsBuilder.append("</div>");

        return "<!DOCTYPE html>\n" +
               "<html lang=\"en\">\n" +
               "<head>\n" +
               "    <meta charset=\"UTF-8\">\n" +
               "    <title>" + post.getTitle() + "</title>\n" +
               "    <link rel=\"stylesheet\" href=\"style.css\">\n" +
               "</head>\n" +
               "<body>\n" +
               "    <article>\n" +
               "        <h1>" + post.getTitle() + "</h1>\n" +
               "        <p class=\"meta\">By " + post.getAuthor() + " | " + post.getDate() + " | " + post.getReadingTimeMinutes() + " min read</p>\n" +
               "        " + tagsBuilder.toString() + "\n" +
               "        <div class=\"content\">\n" +
               "            <p>" + post.getContent().replace("\n", "</p><p>") + "</p>\n" +
               "        </div>\n" +
               "    </article>\n" +
               "</body>\n" +
               "</html>";
    }

    public static void generateRssFeed(List<BlogPost> posts) throws IOException {
        StringBuilder rss = new StringBuilder();
        rss.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
        rss.append("<rss version=\"2.0\">\n");
        rss.append("<channel>\n");
        rss.append("  <title>GitCMS Blog</title>\n");
        rss.append("  <link>https://github.com/savageisaac334-lab/GitCMS</link>\n");
        rss.append("  <description>Latest blog posts</description>\n");

        for (BlogPost post : posts) {
            rss.append("  <item>\n");
            rss.append("    <title>").append(post.getTitle()).append("</title>\n");
            rss.append("    <description>").append(post.getExcerpt()).append("</description>\n");
            rss.append("  </item>\n");
        }

        rss.append("</channel>\n");
        rss.append("</rss>");

        Files.writeString(Paths.get("rss.xml"), rss.toString());
        System.out.println("Generated RSS Feed: rss.xml");
    }

    private static String capitalizeTitle(String str) {
        String[] words = str.split("\\s+");
        StringBuilder capitalizeWord = new StringBuilder();
        for (String w : words) {
            if (w.length() > 0) {
                capitalizeWord.append(Character.toUpperCase(w.charAt(0)))
                              .append(w.substring(1))
                              .append(" ");
            }
        }
        return capitalizeWord.toString().trim();
    }

    public static void main(String[] args) {
        System.out.println("Parser ready.");
    }
}