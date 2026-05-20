package com.wallpaper.server.util;

import org.springframework.util.StringUtils;

public class XssUtil {
    
    private static final String[] HTML_TAGS = {
        "<script>", "</script>", 
        "<iframe>", "</iframe>",
        "<object>", "</object>",
        "<embed>", "</embed>",
        "<form>", "</form>",
        "<input>", "</input>",
        "<button>", "</button>",
        "<link>", "</link>",
        "<meta>", "</meta>",
        "<style>", "</style>",
        "<img>", "</img>",
        "<div>", "</div>",
        "<span>", "</span>",
        "<p>", "</p>",
        "<a>", "</a>",
        "<table>", "</table>",
        "<tr>", "</tr>",
        "<td>", "</td>",
        "<th>", "</th>",
        "<tbody>", "</tbody>",
        "<thead>", "</thead>",
        "<tfoot>", "</tfoot>",
        "<ul>", "</ul>",
        "<ol>", "</ol>",
        "<li>", "</li>",
        "<h1>", "</h1>",
        "<h2>", "</h2>",
        "<h3>", "</h3>",
        "<h4>", "</h4>",
        "<h5>", "</h5>",
        "<h6>", "</h6>",
        "<strong>", "</strong>",
        "<b>", "</b>",
        "<em>", "</em>",
        "<i>", "</i>",
        "<u>", "</u>",
        "<br>", "<br/>",
        "<hr>", "<hr/>"
    };
    
    private static final String[] JS_KEYWORDS = {
        "javascript:", "vbscript:", "onload=", "onerror=", "onclick=", 
        "onmouseover=", "onmouseout=", "onfocus=", "onblur=",
        "onkeydown=", "onkeyup=", "onkeypress=", "onsubmit=",
        "onreset=", "onchange=", "onselect=", "ondblclick="
    };
    
    public static String clean(String input) {
        if (!StringUtils.hasText(input)) {
            return input;
        }
        
        String cleaned = input;
        
        for (String tag : HTML_TAGS) {
            cleaned = cleaned.replace(tag, "");
        }
        
        for (String keyword : JS_KEYWORDS) {
            cleaned = cleaned.replace(keyword, "");
        }
        
        cleaned = cleaned.replace("<", "&lt;").replace(">", "&gt;");
        cleaned = cleaned.replace("\"", "&quot;").replace("'", "&#39;");
        cleaned = cleaned.replace("&", "&amp;");
        
        return cleaned.trim();
    }
    
    public static String cleanAllowBasic(String input) {
        if (!StringUtils.hasText(input)) {
            return input;
        }
        
        String cleaned = input;
        
        for (String keyword : JS_KEYWORDS) {
            cleaned = cleaned.replace(keyword, "");
        }
        
        cleaned = cleaned.replace("<script>", "").replace("</script>", "");
        cleaned = cleaned.replace("<iframe>", "").replace("</iframe>", "");
        cleaned = cleaned.replace("<object>", "").replace("</object>", "");
        cleaned = cleaned.replace("<embed>", "").replace("</embed>", "");
        
        return cleaned.trim();
    }
}
